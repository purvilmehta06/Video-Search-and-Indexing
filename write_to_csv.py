import os
import sys
import signal
import pandas as pd

if __name__ == "__main__":
    signal.signal(signal.SIGINT, signal.SIG_DFL)
    file_name = sys.argv[1]
    file_name_parts = file_name.split('_')
    last_part = file_name_parts[-1]
    predictedAnswer = int(sys.argv[2])
    expectedAnswer = int(last_part.split('.')[0])
    data_to_append = {'Query Video Name': file_name,
                      'Original Video Name': file_name_parts[0],
                      'Query Video Length (seconds)': file_name_parts[1],
                      'Mean': file_name_parts[2],
                      'Sigma': file_name_parts[3],
                      'Expected Answer': int(last_part.split('.')[0]),
                      'Difference': abs(predictedAnswer-expectedAnswer),
                      'Predicted Answer': int(sys.argv[2]),
                      'Low Pass Filter': sys.argv[3],
                      'k for best match': sys.argv[4]}
    df = pd.DataFrame([data_to_append])
    should_write_header = False
    if not os.path.exists("analysis.csv"):
        should_write_header = True
    df.to_csv("analysis.csv", mode='a', header=should_write_header, index=False)