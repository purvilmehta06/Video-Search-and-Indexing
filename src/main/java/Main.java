import java.io.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.javacpp.indexer.UByteRawIndexer;

import static org.bytedeco.ffmpeg.global.avutil.AV_LOG_PANIC;
import static org.bytedeco.ffmpeg.global.avutil.av_log_set_level;

public class Main {

    public static final Integer WIDTH = 352;
    public static final Integer HEIGHT = 288;
    public static final String PREPROCESSED_FOLDER = "./Preprocessing/rgb_values";
    public static final String VIDEO_FOLDER = "./Dataset/Videos";

    /**
     * Read the preprocessed files and return the rgb values
     * @param fileName name of the file
     * @return sum of all r, g, b values in each frame 
     * @throws IOException
     */
    private static int[][] readPreProcessedFiles(String fileName) throws IOException {
        ArrayList<int[]> rgbSums = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] rbg = line.split(", ");
            rgbSums.add(new int[]{Integer.parseInt(rbg[0]), Integer.parseInt(rbg[1]), Integer.parseInt(rbg[2])});
        }
        return rgbSums.toArray(new int[rgbSums.size()][]);
    }

    /**
     * Calculate PSNR between two frames
     */
    private static double calculatePSNR(int[][][] original, int[][][] compressed) {
        double mse = 0;
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                for (int channel = 0; channel < 3; channel++) {
                    int diff = original[x][y][channel] - compressed[x][y][channel];
                    mse += diff * diff;
                }
            }
        }
        mse /= (3.0 * WIDTH * HEIGHT); // 3 channels (RGB)
        double maxPixelValue = 255.0;
        return 20 * Math.log10(maxPixelValue / Math.sqrt(mse));
    }

    /**
     * Compare two windows of rgb values
     * @param preprocessedVideo sum of r, g, and b values of the preprocessed video in each frame
     * @param queryVideo sum of r, g, and b values of the query video in each frame
     * @param start starting frame of the window
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
     * @param queryVideo sum of r, g, and b values of the query video in each frame
     * @throws IOException
     */
    private static int[] findBestMatch(int[][] queryVideo, File[] files) throws IOException {
        double minDiff = Double.MAX_VALUE;
        int minI = -1, minJ = -1;
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            if (file.isFile() && file.getName().endsWith(".txt")) {
                int[][] preprocessedVideo = readPreProcessedFiles(file.getPath());
                for (int j = 0; j <= preprocessedVideo.length - queryVideo.length; j++) {
                    double diff = compareTwoWindow(preprocessedVideo, queryVideo, j);
                    if (diff < minDiff) {
                        minDiff = diff;
                        minI = i;
                        minJ = j;
                    }
                }
            }
        }
        return new int[]{minI, minJ};
    }

    /**
     * Get the sum of r, g, and b values of each frame in the query video
     * @param path path to the video
     * @return sum of r, g, and b values of each frame in the query video
     * @throws FrameGrabber.Exception
     */
    private static int[][][][] getRGBFrames(String path) throws FrameGrabber.Exception {
        av_log_set_level(AV_LOG_PANIC);
        FrameGrabber grabber = new FFmpegFrameGrabber(path);
        grabber.setOption("input_format_options", "hide_banner");
        grabber.start();
        OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();
        int [][][][] rgbMatrices = new int[grabber.getLengthInFrames()][HEIGHT][WIDTH][3];
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
            UByteRawIndexer indexer = mat.createIndexer();
            for (int i = 0; i < HEIGHT; i++) {
                for (int j = 0; j < WIDTH; j++) {
                    for (int k = 0; k < 3; k++) {
                        // b, g, r
                        rgbMatrices[frameNumber][i][j][k] = indexer.get(i, j, k);
                    }
                }
            }
            frameNumber++;
        }
        grabber.stop();
        return rgbMatrices;
    }

    /**
     * Get the sum of r, g, and b values of each frame in the query video
     * @param path path to the video
     * @return sum of r, g, and b values of each frame in the query video
     * @throws FrameGrabber.Exception
     */
    private static int[][] getSumRGB(String path) throws FrameGrabber.Exception {
        av_log_set_level(AV_LOG_PANIC);
        FrameGrabber grabber = new FFmpegFrameGrabber(path);
        grabber.setOption("input_format_options", "hide_banner");
        grabber.start();
        OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();
        int [][] rgbSums = new int[grabber.getLengthInFrames()][3];
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
            UByteRawIndexer indexer = mat.createIndexer();
            int sumR = 0, sumG = 0, sumB = 0;
            for (int i = 0; i < HEIGHT; i++) {
                for (int j = 0; j < WIDTH; j++) {
                    int b = indexer.get(i, j, 0);
                    int g = indexer.get(i, j, 1);
                    int r = indexer.get(i, j, 2);
                    sumR += r;
                    sumG += g;
                    sumB += b;
                }
            }
            rgbSums[frameNumber][0] = sumR;
            rgbSums[frameNumber][1] = sumG;
            rgbSums[frameNumber][2] = sumB;
            frameNumber++;
        }
        grabber.stop();
        return rgbSums;
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        if (args.length > 2 || args.length < 1) {
            System.err.println("java VideoPlayer <rgbFile> <wavFile> or java VideoPlayer <mp4File>");
            return;
        }

        // begin processing
        String fileName = args[0].substring(args[0].lastIndexOf("/") + 1);
        System.out.println("Processing: " + fileName);

        // main driver code
        long startTime = System.currentTimeMillis();
        File folder = new File(PREPROCESSED_FOLDER);
        File[] files = folder.listFiles();

//        int[][][][] rgbMatrices = getRGBFrames(args[0]);
//        int[] ans = findBestMatchWithPSNR(rgbMatrices);

        int[][] rgbSums = getSumRGB(args[0]);
        int[] ans = findBestMatch(rgbSums, files);
        long endTime = System.currentTimeMillis();

        // end processing
        String matchVideoName = files[ans[0]].getName().substring(0, files[ans[0]].getName().length() - 4);
        System.out.println("Best match: " + matchVideoName + " at frame " + ans[1]);
        System.out.println("Finished processing " + fileName + " in " + (endTime - startTime) + " ms");

        // play the video
        String command = "python3 video_player.py " + VIDEO_FOLDER + "/" + matchVideoName  + ".mp4 " + ans[1];
        Process p = Runtime.getRuntime().exec(command);
        int exitCode = p.waitFor();
    }
}