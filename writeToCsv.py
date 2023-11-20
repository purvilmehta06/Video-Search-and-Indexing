import os
import sys
import signal
import pandas as pd

if __name__ == "__main__":
    signal.signal(signal.SIGINT, signal.SIG_DFL)
    fileName = sys.argv[1]
    videoName = fileName.split('_')[0]
    query_video_length = fileName.split('_')[1]
    mean = fileName.split('_')[2]
    sigma = fileName.split('_')[3]
    last_part = fileName.split('_')[-1]
    predictedAnswer = int(last_part.split('.')[0])
    expectedAnswer = int(sys.argv[2])
    correct = True
    if (expectedAnswer - predictedAnswer > 3 or expectedAnswer - predictedAnswer < -3):
        correct = False
    data_to_append = {'Query Video Name': sys.argv[1],
                      'Original Video Name': videoName,
                      'Query Video Length (seconds)': query_video_length,
                      'Mean': mean,
                      'Sigma': sigma,
                      'Predicted Answer': predictedAnswer,
                      'Expected Answer': expectedAnswer,
                      'Is Correct': correct}
    df = pd.DataFrame([data_to_append])
    if (os.path.exists("ans.csv")):
        df.to_csv("ans.csv", mode='a', header=False, index=False)
    else:
        df.to_csv("ans.csv", mode='a', header=True, index=False)