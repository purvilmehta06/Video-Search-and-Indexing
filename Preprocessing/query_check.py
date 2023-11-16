import os
import cv2
import numpy as np


def write_rgb_to_file(file_path, sum_list):
    sum_string = ", ".join(map(str, sum_list))
    with open(file_path, "a") as file:
        file.write(sum_string + "\n")


def write_sum_to_file(file_path, sum_value):
    with open(file_path, "a") as file:
        file.write(str(sum_value) + "\n")


def get_channel_sums(frame):
    b, g, r = cv2.split(frame)
    sum_r = np.sum(r)
    sum_g = np.sum(g)
    sum_b = np.sum(b)
    return [sum_r, sum_g, sum_b]


def process_video_to_frame(video_path, video_name):
    cap = cv2.VideoCapture(video_path)
    print("Video: ", video_path)
    print("FPS: ", cap.get(cv2.CAP_PROP_FPS))
    if not cap.isOpened():
        print("Error: Could not open video file.")
        return
    count = 0
    while True:
        ret, frame = cap.read()
        if not ret:
            break
        sum_list = get_channel_sums(frame)
        write_rgb_to_file("query_rgb_values/" + video_name + ".txt", sum_list)
        write_sum_to_file(
            "query_sum_values/" + video_name + ".txt",
            sum_list[0] + sum_list[1] + sum_list[2],
        )
        count = count + 1
    print("Number of frames: ", count)
    cap.release()


# folder_path = 'Dataset/Queries'
# rgb_values_folder_path = "rgb_values/"

# for video_file in os.listdir(folder_path):
# 	if video_file.endswith('.mp4'):
# 			video_path = folder_path + "/" + video_file
# 			video_name = video_file[:-4]
# 			process_video_to_frame(video_path, video_name)
