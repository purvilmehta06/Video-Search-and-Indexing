import moviepy.editor as mp
import random
import os
import numpy as np
import sys
import contextlib
import io
import math

@contextlib.contextmanager
def suppress_moviepy_output():
    original_stdout = sys.stdout
    sys.stdout = io.StringIO()
    try:
        yield
    finally:
        sys.stdout = original_stdout

def generate_clip(input_video_path):
    with suppress_moviepy_output():
        clip = mp.VideoFileClip(input_video_path)
        return clip
    
def add_gaussian_noise(image):
    row, col, ch = image.shape
    gauss = np.random.normal(mean, sigma, (row, col, ch))
    noisy = np.clip(image + gauss, 0, 255)
    return noisy.astype(np.uint8)

def clip_and_add_noise(clip, query_video_length, start_time, output_video_path):
    with suppress_moviepy_output():
        clipped_clip = clip.subclip(start_time, start_time + query_video_length)
        noised_clip = clipped_clip.fl_image(add_gaussian_noise)
        noised_clip.write_videofile(output_video_path, codec='libx264', audio_codec='aac')

mean = 0
sigma = 5
query_video_length = 10

if (len(sys.argv) >= 4):
    mean = int(sys.argv[1])
    sigma = int(sys.argv[2])
    query_video_length = int(sys.argv[3])
elif (len(sys.argv) == 3):
    mean = int(sys.argv[1])
    sigma = int(sys.argv[2])
elif (len(sys.argv) == 2):
    mean = int(sys.argv[1])

folder_path = "../Dataset/Videos"

for video_file in os.listdir(folder_path):
    
    if video_file.endswith(".mp4"):
        
        print("Processing video: ", video_file)

        input_video_path = folder_path + "/" + video_file
        video_name = video_file[:-4]
        
        clip = generate_clip(input_video_path)
        video_duration = math.floor(clip.duration)
        start_time = random.randint(0, video_duration - query_video_length)
        frame_number = start_time*30

        output_video_path = "../Dataset/NoiseQuery/" + video_name + '_' + str(query_video_length) + '_' + str(mean) + '_' + str(sigma) + '_' + str(frame_number) + '.mp4'

        if not os.path.exists(output_video_path):    
            clip_and_add_noise(clip, query_video_length, start_time, output_video_path)
            print("Clipped ", query_video_length, "seconds video with mean: ", mean, " and sigma: ", sigma ," created")
        else:
            print("Clipped ", query_video_length, "seconds video with mean: ", mean, " and sigma: ", sigma ," exists")