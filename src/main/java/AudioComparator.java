import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class AudioComparator {

    public static double compareAudioFiles(String audioFile1, String audioFile2, int startFrame)
            throws IOException, UnsupportedAudioFileException {
        AudioInputStream audio1 = AudioSystem.getAudioInputStream(new File(audioFile1));
        AudioInputStream audio2 = AudioSystem.getAudioInputStream(new File(audioFile2));

        // Read audio data into arrays
        float[] audioData1 = readAudioData(audio1, 0, -1);
        float[] audioData2 = readAudioData(audio2, startFrame, audioData1.length);

        return calculateDistanceMatrix(audioData1, audioData2);
    }

    private static float[] readAudioData(AudioInputStream audioStream, int startFrame, int length) throws IOException {
        int bytesPerFrame = audioStream.getFormat().getFrameSize();
        int framesToSkip = startFrame;
        long skipBytes = framesToSkip * bytesPerFrame;

        // Skip to the desired frame
        long skippedBytes = audioStream.skip(skipBytes);

        if (skippedBytes != skipBytes) {
            throw new IOException("Unable to skip to the specified frame.");
        }

        int numBytes = (int) audioStream.getFrameLength() * bytesPerFrame;
        byte[] audioBytes = new byte[numBytes];

        int numBytesRead = audioStream.read(audioBytes);

        // Convert bytes to float values
        float[] audioData = new float[numBytesRead / 2];
        int j = 0;
        for (int i = 0; i < numBytesRead; i += 2) {
            audioData[j++] = ((audioBytes[i + 1] & 0xFF) << 8 | (audioBytes[i] & 0xFF)) / 32768.0f;
        }
        return length > 0 ? Arrays.copyOfRange(audioData, 0, length) : audioData;
    }

    private static float calculateDistanceMatrix(float[] audioData1, float[] audioData2) {
        int len1 = audioData1.length;
        float diff = 0;
        for (int i = 0; i < len1; i++) {
            diff += Math.abs(audioData1[i] - audioData2[i]);
        }
        return diff;
    }
}