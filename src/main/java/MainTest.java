import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class MainTest {

    public static final Integer WIDTH = 352;
    public static final Integer HEIGHT = 288;
    public static final Integer pixelsPerFrame = WIDTH * HEIGHT;
    public static final Integer bytesPerPixel = 3;
    public static final Integer UNIQUE_FRAMES = 300;
    public static final String PREPROCESSED_FOLDER = "../Preprocessing/rgb_sum_values";
    public static final String HashFile = "../Preprocessing/rgb_hash.txt";

    /** Process a frame and return the sum of RGB values
     * @param buffer: byte array of the frame
     * @return String: sum of RGB values
     */
    private static String processFrame(byte[] buffer) {
        int []rgb = new int[3];
        int index = 0;
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                int red = buffer[index] & 0xFF;
                int green = buffer[index + 1] & 0xFF;
                int blue = buffer[index + 2] & 0xFF;
                index += 3;
                rgb[0] += red;
                rgb[1] += green;
                rgb[2] += blue;
            }
        }
        return rgb[0] + "-" + rgb[1] + "-" + rgb[2];
    }

    /** Process all the txt files in the Preprocessing folder and create a hash map
     * @return HashMap: key: sum of RGB values of 300 unique frames, value: [video name, starting frame]
     * @throws IOException
     */
    private static HashMap<String, String[]> processVideoTxtFiles() throws IOException {
        File[] files = new File(PREPROCESSED_FOLDER).listFiles();
        HashMap<String, String[]> rgbHash = new HashMap<>();
        for (File file : files) {
            if (file.isFile() && file.getName().endsWith(".txt")) {
                String fileName = file.getName().substring(0, file.getName().length() - 4);
                ArrayList<String> rgbSums = new ArrayList<>();
                BufferedReader br = new BufferedReader(new FileReader(file));
                String line;
                int frameNumber = 0;
                while ((line = br.readLine()) != null) {
                    rgbSums.add(line.replace(" ", "-"));
                    frameNumber++;
                    if (rgbSums.size() == UNIQUE_FRAMES) {
                        rgbHash.put(String.join("-", rgbSums), new String[]{fileName, String.valueOf(frameNumber - UNIQUE_FRAMES)});
                        rgbSums.remove(0);
                    }
                }
            }
        }
        return rgbHash;
    }

    /** Process the input query video and return the video name and starting frame
     * @param inputQueryVideo: path to the input query video
     * @param rgbHash: hash map created from the pre-processing step
     * @return String[]: [video name, starting frame]
     * @throws IOException
     */
    public static String[] processInput(String inputQueryVideo, HashMap<String, String[]> rgbHash) throws IOException {
        ArrayList<String> rgbSums = new ArrayList<>();
        DataInputStream dis = new DataInputStream(new FileInputStream(inputQueryVideo));
        byte[] buffer = new byte[pixelsPerFrame * bytesPerPixel];
        while (dis.read(buffer) != -1) {
            rgbSums.add(processFrame(buffer));
            if (rgbSums.size() == UNIQUE_FRAMES) {
                String keyToFind = String.join("-", rgbSums);
                return rgbHash.getOrDefault(keyToFind, null);
            }
        }
        return null;
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        HashMap<String, String[]> rgbHash = processVideoTxtFiles();
        String[] ans = processInput(args[0], rgbHash);
        if (ans == null) {
            System.out.println("No match found for " + args[0]);
        } else {
            System.out.println(ans[0] + " " + ans[1] + " " + args[0]);
//            String cmd = "python3 write_to_csv.py" + " " + ans[0] + " " + ans[1] + " " + args[0];
//            System.out.println(cmd);
//            Process p = Runtime.getRuntime().exec(cmd);
//            p.waitFor();
        }
    }
}
