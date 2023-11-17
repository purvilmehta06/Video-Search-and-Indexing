import moviepy.editor as mp

def get_fps(input_video_path):
    clip = mp.VideoFileClip(input_video_path)
    fps = clip.fps
    clip.close()
    return fps

input_video_path = 'Dataset/Generated/Clipped/video1_10.mp4'
fps = get_fps(input_video_path)
print(f"Frames per second (fps): {fps}")