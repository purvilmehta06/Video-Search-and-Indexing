import java.io.*;
import java.io.IOException;
import java.util.Objects;

import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.javacpp.indexer.UByteRawIndexer;

import static org.bytedeco.ffmpeg.global.avutil.AV_LOG_PANIC;
import static org.bytedeco.ffmpeg.global.avutil.av_log_set_level;

public class Preprocessor {

    public static String VIDEO_PATH = "Dataset/Videos/";
    public static String PREPROCESSED_PATH = "Preprocessing/rgb_values/";
    public static String PREPROCESSED_LOW_PASS_PATH = "Preprocessing/rgb_values_low_pass/";
    public static final Integer WIDTH = 352;
    public static final Integer HEIGHT = 288;
    public static final Integer NEIGHBORHOOD_SIZE = 3;

    /**
     * Writes the rgb values to a file
     * @param fileName the name of the file to write to
     * @param rgb the rgb values to write to the file
     */
    private static void writeToFile(String fileName, int[][] rgb) throws IOException {
        FileWriter fileWriter = new FileWriter(fileName);
        PrintWriter printWriter = new PrintWriter(fileWriter);
        for (int[] ints : rgb) {
            printWriter.printf("%d %d %d\n", ints[0], ints[1], ints[2]);
        }
        printWriter.close();
    }

    /**
     * Passes through a low pass filter
     * @param channel the channel to pass through the low pass filter
     * @return the channel after passing through the low pass filter
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

    /**
     * Processes the video and writes the rgb values to a file
     * @param videoPath the path to the video
     * @param videoName the name of the video
     * @param lowPass whether to pass through a low pass filter
     */
    private static void processVideo(String videoPath, String videoName, Boolean lowPass) throws IOException {
        av_log_set_level(AV_LOG_PANIC);
        FrameGrabber grabber = new FFmpegFrameGrabber(videoPath);
        grabber.start();
        OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();
        int[][] rgb = new int[grabber.getLengthInFrames()][3];

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

            // get the sum of all the rgb values
            int rSum = 0, gSum = 0, bSum = 0;
            for (int i = 0; i < HEIGHT; i++) {
                for (int j = 0; j < WIDTH; j++){
                    rSum += r[i][j];
                    gSum += g[i][j];
                    bSum += b[i][j];
                }
            }

            // store the rgb values for future use
            rgb[frameNumber][0] = rSum;
            rgb[frameNumber][1] = gSum;
            rgb[frameNumber][2] = bSum;
            frameNumber++;
        }
        grabber.stop();
        String videoNameWithoutExtension = videoName.substring(0, videoName.length() - 4);
        writeToFile(lowPass ? PREPROCESSED_LOW_PASS_PATH + videoNameWithoutExtension + ".txt" : PREPROCESSED_PATH + videoNameWithoutExtension + ".txt", rgb);
    }

    public static void main(String[] args) throws IOException {
        File folder = new File(VIDEO_PATH);
        File[] listOfFiles = folder.listFiles();
        Boolean lowPass = args[0].equals("1");
        for (int i = 0; i < Objects.requireNonNull(listOfFiles).length; i++) {
            String videoPath = listOfFiles[i].getPath();
            String videoName = listOfFiles[i].getName();
            if (videoName.endsWith(".mp4")) {
                System.out.println("Processing " + videoName);
                processVideo(videoPath, videoName, lowPass);
            }
        }
    }
}
