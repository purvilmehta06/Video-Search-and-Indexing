import cv2
import moviepy.editor as mp
import numpy as np

def add_gaussian_noise(image, mean=10, sigma=30):
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

input_video_path = 'Dataset/Videos/video1.mp4'
output_video_path = 'Dataset/Generated/video1_10_30.mp4'
add_noise_to_video(input_video_path, output_video_path)

# import cv2
# import numpy as np

# def add_gaussian_noise(image, mean=0, sigma=25):
#     row, col, ch = image.shape
#     gauss = np.random.normal(mean, sigma, (row, col, ch))
#     noisy = np.clip(image + gauss, 0, 255)
#     return noisy.astype(np.uint8)

# def add_noise_to_video(input_video_path, output_video_path):
#     cap = cv2.VideoCapture(input_video_path)
#     fps = int(cap.get(cv2.CAP_PROP_FPS))
#     width = int(cap.get(cv2.CAP_PROP_FRAME_WIDTH))
#     height = int(cap.get(cv2.CAP_PROP_FRAME_HEIGHT))

#     fourcc = cv2.VideoWriter_fourcc(*'mp4v')
#     out = cv2.VideoWriter(output_video_path, fourcc, fps, (width, height))

#     random_seed = 42  # Set a random seed for reproducibility
#     np.random.seed(random_seed)

#     while True:
#         ret, frame = cap.read()
#         if not ret:
#             break

#         noisy_frame = add_gaussian_noise(frame)

#         out.write(noisy_frame)

#         cv2.imshow('Noisy Video', noisy_frame)
#         if cv2.waitKey(25) & 0xFF == ord('q'):
#             break

#     cap.release()
#     out.release()
#     cv2.destroyAllWindows()

# input_video_path = 'Dataset/Videos/video1.mp4'
# output_video_path = 'Dataset/Generated/video1_2.mp4'
# add_noise_to_video(input_video_path, output_video_path)