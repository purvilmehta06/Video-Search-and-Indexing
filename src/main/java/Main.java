import java.awt.image.BufferedImage;
import java.io.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.Arrays;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.javacpp.indexer.UByteRawIndexer;

import static org.bytedeco.ffmpeg.global.avutil.AV_LOG_PANIC;
import static org.bytedeco.ffmpeg.global.avutil.av_log_set_level;

import java.io.FileWriter;

public class Main {

    public static final Integer WIDTH = 352;
    public static final Integer HEIGHT = 288;
    public static final String PREPROCESSED_FOLDER = "./Preprocessing/rgb_values";
    public static final String PREPROCESSED_FOLDER_LOW_PASS = "./Preprocessing/rgb_values_low_pass";
    public static final String VIDEO_FOLDER = "./Dataset/Videos";
    public static final String AUDIO_FOLDER = "./Dataset/Audios";
    public static final Integer NEIGHBORHOOD_SIZE = 3;
    public static final Double FRAME_DIFFERENCE_THRESHOLD = 1e7;
    public static final Integer FRAME_TO_CHECK = 90;
    public static final Integer FALSE_POSITIVE_IN_FRAME_TO_CHECK = 5;

    /**
     * Read the preprocessed files and return the rgb values of each frame
     *
     * @param fileName name of the file
     * @return sum of all r, g, b values in each frame
     */
    private static int[][] readPreProcessedFiles(String fileName) throws IOException {
        ArrayList<int[]> rgbSums = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] rbg = line.split(" ");
            rgbSums.add(new int[]{Integer.parseInt(rbg[0]), Integer.parseInt(rbg[1]), Integer.parseInt(rbg[2])});
        }
        return rgbSums.toArray(new int[rgbSums.size()][]);
    }

    /**
     * Compare two windows of rgb values
     *
     * @param preprocessedVideo sum of r, g, and b values of the preprocessed video in each frame
     * @param queryVideo        sum of r, g, and b values of the query video in each frame
     * @param start             starting frame of the window
     * @return absolute difference between the two windows
     */
    private static double compareTwoWindow(int[][] preprocessedVideo, int[][] queryVideo, int start) {
        double diff = 0;
        for (int i = 0; i < queryVideo.length; i++) {
            diff += Math.abs(preprocessedVideo[i + start][0] - queryVideo[i][0]);
            diff += Math.abs(preprocessedVideo[i + start][1] - queryVideo[i][1]);
            diff += Math.abs(preprocessedVideo[i + start][2] - queryVideo[i][2]);
        }
        return diff;
    }

    /**
     * Find the best match of the query video in the preprocessed videos
     *
     * @param rgbQueryVideo sum of r, g, and b values of the query video in each frame
     * @param topKMatches   top k best matches
     */
    private static int[] findBestMatchFromTopK(int[][][][] rgbQueryVideo, int[][] topKMatches, File[] files, String inputQueryAudio) throws IOException, UnsupportedAudioFileException {
        int[] ans = new int[2];
        Arrays.fill(ans, -1);
        double minDiff = Double.MAX_VALUE;
        for (int[] topKMatch : topKMatches) {
            // get the file name and starting frame of the dataset video
            String fileName = files[topKMatch[0]].getName().substring(0, files[topKMatch[0]].getName().length() - 4);
            int startFrame = topKMatch[1];

            // open video from the dataset and set the starting frame
            av_log_set_level(AV_LOG_PANIC);
            FrameGrabber grabber = new FFmpegFrameGrabber(VIDEO_FOLDER + "/" + fileName + ".mp4");
            grabber.start();
            grabber.setFrameNumber(startFrame - 1);
            OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();

            double diff = 0;
            int frameNumber = startFrame, counter = 0;
            Deque<Double> deque = new ArrayDeque<>();
            while (true) {
                Frame frame = grabber.grab();
                if (frame == null) {
                    break;
                }
                Mat mat = converter.convert(frame);
                if (mat == null) {
                    continue;
                }
                if (frameNumber >= startFrame + rgbQueryVideo[0].length) {
                    break;
                }

                // get R, G, B values of each frame
                UByteRawIndexer indexer = mat.createIndexer();
                int[][] r = new int[HEIGHT][WIDTH];
                int[][] g = new int[HEIGHT][WIDTH];
                int[][] b = new int[HEIGHT][WIDTH];
                for (int i = 0; i < HEIGHT; i++) {
                    for (int j = 0; j < WIDTH; j++) {
                        b[i][j] = indexer.get(i, j, 0);
                        g[i][j] = indexer.get(i, j, 1);
                        r[i][j] = indexer.get(i, j, 2);
                    }
                }

                // compare the two windows pixel by pixel
                double local_diff = 0;
                for (int j = 0; j < HEIGHT; j++) {
                    for (int k = 0; k < WIDTH; k++) {
                        local_diff += Math.abs(rgbQueryVideo[0][frameNumber - startFrame][j][k] - r[j][k]);
                        local_diff += Math.abs(rgbQueryVideo[1][frameNumber - startFrame][j][k] - g[j][k]);
                        local_diff += Math.abs(rgbQueryVideo[2][frameNumber - startFrame][j][k] - b[j][k]);
                    }
                }
                diff += local_diff;
                frameNumber++;

                // if we check frame by frame pixel difference, we will make sure that, at any point of time,
                // last FRAME_TO_CHECK - FALSE_POSITIVE_IN_FRAME_TO_CHECK out of FRAME_TO_CHECK frames have at most
                // FRAME_DIFFERENCE_THRESHOLD difference. If we have more than that, we can safely assume
                // that the video is not a match
                if (local_diff >= FRAME_DIFFERENCE_THRESHOLD) {
                    counter += 1;
                }
                deque.addLast(local_diff);
                if (deque.size() >= FRAME_TO_CHECK) {
                    double first = deque.pollFirst();
                    if (first >= FRAME_DIFFERENCE_THRESHOLD) {
                        counter -= 1;
                    }
                }
                if (counter > FRAME_TO_CHECK - FALSE_POSITIVE_IN_FRAME_TO_CHECK) {
                    diff = Double.MAX_VALUE;
                    break;
                }
            }
            grabber.stop();

            System.out.println("Video: " + fileName + " with diff " + diff + " at " + startFrame);
            if (diff < minDiff) {
                minDiff = diff;
                ans[0] = topKMatch[0];
                ans[1] = topKMatch[1];
            }
        }
        return ans;
    }

    /**
     * Find the top k best matches of the query video in the preprocessed videos
     *
     * @param queryVideo sum of r, g, and b values of the query video in each frame
     * @param files      list of preprocessed files
     * @param k          number of best matches to return
     */
    private static int[][] findBestMatch(int[][] queryVideo, File[] files, int k) throws IOException {
        PriorityQueue<double[]> pQueue = new PriorityQueue<>(Collections.reverseOrder((a, b) -> Double.compare(a[0], b[0])));
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            if (file.isFile() && file.getName().endsWith(".txt")) {
                int[][] preprocessedVideo = readPreProcessedFiles(file.getPath());
                for (int j = 0; j <= preprocessedVideo.length - queryVideo.length; j++) {
                    double diff = compareTwoWindow(preprocessedVideo, queryVideo, j);
                    if (pQueue.size() < k) {
                        pQueue.add(new double[]{diff, i, j});
                    } else if (!pQueue.isEmpty() && pQueue.peek()[0] > diff) {
                        pQueue.poll();
                        pQueue.add(new double[]{diff, i, j});
                    }
                }
            }
        }

        int[][] topKMatches = new int[k][2];
        while (!pQueue.isEmpty()) {
            double[] curr = pQueue.poll();
            System.out.println("Video: " + files[(int) curr[1]].getName() + " with diff " + curr[0] + " at " + curr[2]);
            topKMatches[pQueue.size()] = new int[]{(int) curr[1], (int) curr[2]};
        }
        return topKMatches;
    }

    /**
     * Pass through a low pass filter
     *
     * @param channel channel to pass through the low pass filter
     * @return channel after passing through the low pass filter
     */
    private static int[][] passThroughLowPass(int[][] channel) {
        int[][] newChannel = new int[channel.length][channel[0].length];
        for (int i = 0; i < HEIGHT; i++) {
            for (int j = 0; j < WIDTH; j++) {
                int sum = 0, count = 0, offset = NEIGHBORHOOD_SIZE / 2;
                for (int k = i - offset; k <= i + offset; k++) {
                    for (int l = j - offset; l <= j + offset; l++) {
                        if (k >= 0 && k < HEIGHT && l >= 0 && l < WIDTH) {
                            sum += channel[k][l];
                            count++;
                        }
                    }
                }
                newChannel[i][j] = sum / count;
            }
        }
        return newChannel;
    }

    public record Dimensions(int width, int height) {
    }

    private static final Dimensions originalImageDimensions = new Dimensions(352, 288);
    private final static int IMAGE_TYPE = BufferedImage.TYPE_INT_RGB;

    public static int[] getRGB(int pixel) {
        return new int[]{(pixel >> 16) & 0xFF, (pixel >> 8) & 0xFF, pixel & 0xFF};
    }

    private static BufferedImage readImageRGB(String imgPath, String filename, int frameGadedha) throws IOException {
        BufferedImage img=null;
        int frameLength = originalImageDimensions.width() * originalImageDimensions.height() * 3 * frameGadedha;
//        System.out.println(frameLength);
        File file = new File(imgPath);
        RandomAccessFile raf = new RandomAccessFile(file, "r");
        raf.seek(0);

        byte[] bytes = new byte[(int) (long) frameLength];

        raf.read(bytes);

        FileWriter fileWriter = new FileWriter(filename);
        PrintWriter printWriter = new PrintWriter(fileWriter);

        int count = 0;
        int sr = 0;
        int sb = 0;
        int sg = 0;

        try(FileInputStream fileInputStream = new FileInputStream(imgPath)) {
            // Read the file byte by byte
            int byteRead;
            int pixelCount = 0;

            while ((byteRead = fileInputStream.read()) != -1) {
                // Process the RGB values for each pixel
                int red = byteRead;
                int green = fileInputStream.read();
                int blue = fileInputStream.read();

                sr += red;
                sb += blue;
                sg += green;

                pixelCount++;
                if (pixelCount % (originalImageDimensions.height() * originalImageDimensions.width()) == 0) {
                    count++;
                    printWriter.printf("%d %d %d\n", sr, sg, sb);
                    sr = 0;
                    sb = 0;
                    sg = 0;
                    if (count > 9000) {
                        break;
                    } else if (count%100 == 0) {
                        System.out.println(count);
                    }
                }

            }
        } catch (Exception e) {

        }


//            int ind = 0;
//        for(int i=0;i < frameGadedha;i++) {
//            int sr =0;
//            int sb = 0;
//            int sg = 0;
//            for (int y = 0; y < originalImageDimensions.height(); y++) {
//                for (int x = 0; x < originalImageDimensions.width(); x++) {
//                    byte r = bytes[ind++];
//                    byte g = bytes[ind++];
//                    byte b = bytes[ind++];
//
//                    int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
//                    int rgb[] = getRGB(pix);
//                    sr += rgb[0];
//                    sg += rgb[1];
//                    sb += rgb[2];
//                }
//            }
//            printWriter.printf("%d %d %d\n", sr, sg, sb);
//        }

        printWriter.close();

        raf.close();
        return img;
    }

    /**
     * Get the rgb values of each frame of the video
     *
     * @param path    path to the video
     * @param lowPass whether to pass through a low pass filter
     * @return rgb values of each frame of the video
     */
    private static int[][][][] getVideoRGBPerFrame(String path, Boolean lowPass) throws FrameGrabber.Exception {
        av_log_set_level(AV_LOG_PANIC);
        FrameGrabber grabber = new FFmpegFrameGrabber(path);
        grabber.setOption("input_format_options", "hide_banner");
        grabber.start();
        OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();
        int[][][][] rgb = new int[3][grabber.getLengthInFrames()][HEIGHT][WIDTH];

        grabber.setFrameRate(30);
        int frameNumber = 0;
        while (true) {
            Frame frame = grabber.grab();
            if (frame == null) {
                break;
            }
            Mat mat = converter.convert(frame);
            if (mat == null) {
                continue;
            }

            // get the rgb values of each frame
            UByteRawIndexer indexer = mat.createIndexer();
            int[][] r = new int[HEIGHT][WIDTH];
            int[][] g = new int[HEIGHT][WIDTH];
            int[][] b = new int[HEIGHT][WIDTH];
            for (int i = 0; i < HEIGHT; i++) {
                for (int j = 0; j < WIDTH; j++) {
                    b[i][j] = indexer.get(i, j, 0);
                    g[i][j] = indexer.get(i, j, 1);
                    r[i][j] = indexer.get(i, j, 2);
                }
            }

            // pass through a low pass filter
            if (lowPass) {
                b = passThroughLowPass(b);
                g = passThroughLowPass(g);
                r = passThroughLowPass(r);
            }

            // store the rgb values for future use
            rgb[0][frameNumber] = r;
            rgb[1][frameNumber] = g;
            rgb[2][frameNumber] = b;
            frameNumber++;
        }
        grabber.stop();
        return rgb;
    }

    private static int[][] getSumRGB(int[][][][] rgbVideo) {
        int[][] rgbSums = new int[rgbVideo[0].length][3];
        for (int i = 0; i < rgbVideo[0].length; i++) {
            for (int j = 0; j < HEIGHT; j++) {
                for (int k = 0; k < WIDTH; k++) {
                    rgbSums[i][0] += rgbVideo[0][i][j][k];
                    rgbSums[i][1] += rgbVideo[1][i][j][k];
                    rgbSums[i][2] += rgbVideo[2][i][j][k];
                }
            }
        }
        return rgbSums;
    }

    public static void main(String[] args) throws IOException, UnsupportedAudioFileException, InterruptedException {
        try {
//            BufferedImage queryRGB = readImageRGB("Dataset/Queries/RGB/video11_1.rgb");
//            BufferedImage a = readImageRGB("Dataset/Videos/video11.rgb", "convert_original_through_script.txt", 17300);
//            BufferedImage b = readImageRGB("Dataset/Queries/video11_1.rgb", "convert_query_through_script.txt", 600);
            BufferedImage c = readImageRGB("Dataset/Videos/RGB/video11.rgb", "log_original.txt", 17300);
//            BufferedImage d = readImageRGB("Dataset/Queries/RGB/video11_1.rgb", "log_query.txt", 600);

        } catch (Exception e) {

        }
//        if (args.length < 3) {
//            System.err.println("java VideoPlayer <mp4File> <smoothing> <k>");
//            return;
//        }
//
//        // get the input arguments
//        String inputQueryVideo = args[0];
//        String inputQueryAudio = args[1];
//        Boolean lowPass = args[2].equals("1");
//        int k = Integer.parseInt(args[3]);
//        Boolean shouldWriteAnalysis = args[4].equals("1");
//        String fileName = inputQueryVideo.substring(inputQueryVideo.lastIndexOf("/") + 1);
//
//        // begin processing
//        long startTime = System.currentTimeMillis();
//        System.out.println("Processing: " + fileName);
//        File folder = new File(PREPROCESSED_FOLDER);
//        if (lowPass) {
//            folder = new File(PREPROCESSED_FOLDER_LOW_PASS);
//        }
//        File[] files = folder.listFiles();
//        int[][][][] rgbQueryVideo = getVideoRGBPerFrame(inputQueryVideo, lowPass);
//        int[][] rgbSums = getSumRGB(rgbQueryVideo);
//        int[][] topKMatches = findBestMatch(rgbSums, files, k);
//        if (lowPass) {
//            rgbQueryVideo = getVideoRGBPerFrame(inputQueryVideo, false);
//        }
//        int[] ans = findBestMatchFromTopK(rgbQueryVideo, topKMatches, files, inputQueryAudio);
//        long endTime = System.currentTimeMillis();
//
//        // end processing
//        if (ans[0] == -1) {
//            System.out.println("No match found");
//            return;
//        }
//        String matchVideoName = files[ans[0]].getName().substring(0, files[ans[0]].getName().length() - 4);
//        System.out.println("Best match: " + matchVideoName + " at frame " + ans[1]);
//        System.out.println("Finished processing " + fileName + " in " + (endTime - startTime) + " ms");
//
//        // Comment this if you don't wanna test for test dataset
//        if (shouldWriteAnalysis) {
//            String dfWriterCommand = "python3 write_to_csv.py " + fileName + " " + ans[1] + " " + args[2] + " " + args[3];
//            Process writer = Runtime.getRuntime().exec(dfWriterCommand);
//            writer.waitFor();
//        }
//
//        // play the video
//        String command = "python3 video_player.py " + VIDEO_FOLDER + "/" + matchVideoName + ".mp4 " + ans[1];
//        Process p = Runtime.getRuntime().exec(command);
//        p.waitFor();
    }
}
