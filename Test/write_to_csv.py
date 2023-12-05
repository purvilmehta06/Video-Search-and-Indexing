import os
import sys
import signal
import pandas as pd

if __name__ == "__main__":
    signal.signal(signal.SIGINT, signal.SIG_DFL)
    prediction_start_frame = int(sys.argv[2])
    input_video = sys.argv[3].split('/')[-1]
    file_name_parts = input_video.split('_')
    expected_start_frame = int(file_name_parts[1])
    data_to_append = {'Query Video Name': input_video,
                      'Original Video Name': file_name_parts[0] + '.mp4',
                      'Query Video Length (seconds)': file_name_parts[2].split('.')[0],
                      'Prediction Video Name': sys.argv[1],
                      'Expected Answer': expected_start_frame,
                      'Predicted Answer': prediction_start_frame,
                      'Difference': abs(prediction_start_frame - expected_start_frame)}
    df = pd.DataFrame([data_to_append])
    should_write_header = False
    if not os.path.exists("analysis.csv"):
        should_write_header = True
    df.to_csv("analysis.csv", mode='a', header=should_write_header, index=False)