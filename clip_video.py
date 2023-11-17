import moviepy.editor as mp
import random

def clip_random_10_seconds(input_video_path, output_video_path):
    clip = mp.VideoFileClip(input_video_path)

    # Get the duration of the original video
    video_duration = clip.duration

    # Randomly select a starting point for the 10-second clip
    start_time = random.uniform(0, video_duration - 10)

    # Clip the 10-second segment
    clipped_clip = clip.subclip(start_time, start_time + 10)

    # Write the clipped video to the output file
    clipped_clip.write_videofile(output_video_path, codec='libx264', audio_codec='aac')

input_video_path = 'Dataset/Generated/video1_4.mp4'
output_video_path = 'Dataset/Generated/Clipped/video1_11.mp4'
clip_random_10_seconds(input_video_path, output_video_path)