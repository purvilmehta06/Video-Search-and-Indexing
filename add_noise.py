import moviepy.editor as mp
import random
import os
import numpy as np

def get_fps(input_video_path):
    clip = mp.VideoFileClip(input_video_path)
    fps = clip.fps
    clip.close()
    return fps

def clip_random_10_seconds(input_video_path, output_video_path, query_video_length):
    clip = mp.VideoFileClip(input_video_path)
    video_duration = clip.duration
    start_time = random.uniform(0, video_duration - query_video_length)
    print(start_time)
    clipped_clip = clip.subclip(start_time, start_time + query_video_length)
    clipped_clip.write_videofile(output_video_path, codec='libx264', audio_codec='aac')

def add_gaussian_noise(image):
    row, col, ch = image.shape
    gauss = np.random.normal(mean, sigma, (row, col, ch))
    noisy = np.clip(image + gauss, 0, 255)
    return noisy.astype(np.uint8)

def process_frame(frame):
    return add_gaussian_noise(frame)

def add_noise_to_video(input_video_path, output_video_path):
    clip = mp.VideoFileClip(input_video_path)
    processed_clip = clip.fl_image(process_frame)
    processed_clip.write_videofile(output_video_path, codec='libx264', audio_codec='aac')

mean = 0
sigma = 5
count = 0
query_video_length = 10

input_video_path = 'Dataset/Videos/video1.mp4'
initial_name = input_video_path[:-4]
output_video_path = initial_name + str(mean) + '_' + str(sigma) + '.mp4'
if not os.path.exists(output_video_path):    
    add_noise_to_video(input_video_path, output_video_path)
else:
    print("Video with mean and sigma exists")

input_video_path = output_video_path
fps = get_fps(input_video_path)
print("FPS of output file: ", fps)
if (fps == 30.0):
    output_video_path = initial_name + str(mean) + '_' + str(sigma) + '_' + str(count) + '.mp4'
    if not os.path.exists(output_video_path):
        clip_random_seconds(input_video_path, output_video_path, query_video_length)
        print("Clip video created")
    else:
        print("Clip video exists")
else:
    print("Not creating clip as fps is less")