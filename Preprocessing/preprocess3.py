import imageio
import numpy as np

def get_rgb_values(frame):
    # Access the RGB values of the frame
    red_channel = frame[:, :, 0]  # Red channel
    green_channel = frame[:, :, 1]  # Green channel
    blue_channel = frame[:, :, 2]  # Blue channel
    return red_channel, green_channel, blue_channel

def get_channel_sums(red_values, green_values, blue_values):
    print(blue_values)
    sum_r = np.sum(red_values)
    sum_g = np.sum(green_values)
    sum_b = np.sum(blue_values)
    return [sum_r, sum_g, sum_b]

def read_frames(video_path):
    # Create a reader object
    reader = imageio.get_reader(video_path)

    # Iterate through frames
    for frame_number, frame in enumerate(reader):
        # Get RGB values from the frame
        red_values, green_values, blue_values = get_rgb_values(frame)
        get_channel_sums(red_values, green_values, blue_values)
        break

        

    # Close the reader when done
    reader.close()

# Replace 'path/to/your/video.mp4' with the actual path to your video file
video_path = '../Dataset/Videos/video1.mp4'
read_frames(video_path)
