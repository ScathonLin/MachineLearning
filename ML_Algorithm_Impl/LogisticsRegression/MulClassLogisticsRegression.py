import numpy as np
from numpy import random


def load_data():
    print("==>Starting Read Data...")
    ori_data = np.loadtxt(
        "F:\MachineLearn\ML_Algorithm_Impl\LogisticsRegression\data\ml_in_action.txt", delimiter="\t")
    print("==>Complete Read Data...")
    

if __name__ == "__main__":
    load_data()
