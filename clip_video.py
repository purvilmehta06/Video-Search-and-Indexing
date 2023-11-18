import moviepy.editor as mp
import random

def clip_random_10_seconds(input_video_path, output_video_path):
    clip = mp.VideoFileClip(input_video_path)
    video_duration = clip.duration
    start_time = random.uniform(0, video_duration - 10)
    print(start_time)
    clipped_clip = clip.subclip(start_time, start_time + 10)
    clipped_clip.write_videofile(output_video_path, codec='libx264', audio_codec='aac')

input_video_path = 'Dataset/Generated/video1_2_2.mp4'
output_video_path = 'Dataset/Generated/Clipped/video1_2_2_4.mp4'
clip_random_10_seconds(input_video_path, output_video_path)