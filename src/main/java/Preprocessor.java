import java.io.*;
import java.io.IOException;

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

    private static void writeToFile(String fileName, int[][] rgb) throws IOException {
        FileWriter fileWriter = new FileWriter(fileName);
        PrintWriter printWriter = new PrintWriter(fileWriter);
        for (int i = 0; i < rgb.length; i++) {
            printWriter.printf("%d %d %d\n", rgb[i][0], rgb[i][1], rgb[i][2]);
        }
        printWriter.close();
    }

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

            // get the rgb values of each frame
            UByteRawIndexer indexer = mat.createIndexer();
            int r = 0, g = 0, b = 0;
            for (int i = 0; i < HEIGHT; i++) {
                for (int j = 0; j < WIDTH; j++) {
                    b += indexer.get(i, j, 0);
                    g += indexer.get(i, j, 1);
                    r += indexer.get(i, j, 2);
                }
            }

            // store the rgb values for future use
            rgb[frameNumber][0] = r;
            rgb[frameNumber][1] = g;
            rgb[frameNumber][2] = b;
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
        for (int i = 0; i < listOfFiles.length; i++) {
            String videoPath = listOfFiles[i].getPath();
            String videoName = listOfFiles[i].getName();
            if (videoName.endsWith(".mp4")) {
                System.out.println("Processing " + videoName);
                processVideo(videoPath, videoName, lowPass);
            }
        }

    }

}
