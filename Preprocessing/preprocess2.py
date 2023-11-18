import cv2
import numpy as np
import os
import sys

def write_rgb_to_file(file_path, sum_list):
    sum_string = ", ".join(map(str, sum_list))
    with open(file_path, "a") as file:
        file.write(sum_string + "\n")


def write_sum_to_file(file_path, sum_value):
    with open(file_path, "a") as file:
        file.write(str(sum_value) + "\n")


def pass_from_low_pass_filter(channel):
    kernel = np.ones((3, 3), np.float32) / 9
    return cv2.filter2D(channel, -1, kernel)


def get_channel_sums(frame, should_pass_low_pass_filter):
    b, g, r = cv2.split(frame)
    if should_pass_low_pass_filter:
        b = pass_from_low_pass_filter(b)
        g = pass_from_low_pass_filter(g)
        r = pass_from_low_pass_filter(r)
    sum_r = np.sum(r)
    sum_g = np.sum(g)
    sum_b = np.sum(b)
    return [sum_r, sum_g, sum_b]


def process_video_to_frame(video_path, video_name, should_pass_low_pass_filter=False):
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
        sum_list = get_channel_sums(frame, should_pass_low_pass_filter)
        folder_name = "rgb_values/"
        if should_pass_low_pass_filter:
            folder_name = "rgb_values_low_pass/"
        write_rgb_to_file(folder_name + video_name + ".txt", sum_list)
        count = count + 1
    print("Number of frames: ", count)
    cap.release()


folder_path = "../Dataset/Videos"
low_pass = sys.argv[1] == "True"
folder_name = "rgb_values/"
if low_pass:
    folder_name = "rgb_values_low_pass/"
for file_name in os.listdir(folder_name):
    os.remove(folder_name + file_name)
for video_file in os.listdir(folder_path):
    if video_file.endswith(".mp4"):
        video_path = folder_path + "/" + video_file
        video_name = video_file[:-4]
        process_video_to_frame(video_path, video_name, low_pass)