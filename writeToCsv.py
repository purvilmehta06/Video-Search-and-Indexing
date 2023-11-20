import os
import sys
import signal
import pandas as pd

if __name__ == "__main__":
    signal.signal(signal.SIGINT, signal.SIG_DFL)
    file_name = sys.argv[1]
    file_name_parts = file_name.split('_')
    last_part = file_name_parts[-1]
    predictedAnswer = int(last_part.split('.')[0])
    expectedAnswer = int(sys.argv[2])
    correct = True
    if (expectedAnswer - predictedAnswer > 3 or expectedAnswer - predictedAnswer < -3):
        correct = False
    data_to_append = {'Query Video Name': file_name,
                      'Original Video Name': file_name_parts[0],
                      'Query Video Length (seconds)': file_name_parts[1],
                      'Mean': file_name_parts[2],
                      'Sigma': file_name_parts[3],
                      'Predicted Answer': predictedAnswer,
                      'Expected Answer': expectedAnswer,
                      'Difference': abs(predictedAnswer-expectedAnswer),
                      'Low Pass Filter': sys.argv[3],
                      'k for best match': sys.argv[4],
                      'Is Correct': correct}
    df = pd.DataFrame([data_to_append])
    if (os.path.exists("ans.csv")):
        df.to_csv("ans.csv", mode='a', header=False, index=False)
    else:
        df.to_csv("ans.csv", mode='a', header=True, index=False)