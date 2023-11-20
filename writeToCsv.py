import os
import sys
import signal
import pandas as pd

if __name__ == "__main__":
    signal.signal(signal.SIGINT, signal.SIG_DFL)
    fileName = sys.argv[1]
    last_part = fileName.split('_')[-1]
    predictedAnswer = int(last_part.split('.')[0])
    expectedAnswer = int(sys.argv[2])
    correct = True
    if (expectedAnswer - predictedAnswer > 3 or expectedAnswer - predictedAnswer < -3):
        correct = False
    data_to_append = {'Name': sys.argv[1],
                      'Predicted Answer': predictedAnswer,
                      'Expected Answer': expectedAnswer,
                      'IsCorrect': correct}
    df = pd.DataFrame([data_to_append])
    if (os.path.exists("ans.csv")):
        df.to_csv("ans.csv", mode='a', header=False, index=False)
    else:
        df.to_csv("ans.csv", mode='a', header=True, index=False)