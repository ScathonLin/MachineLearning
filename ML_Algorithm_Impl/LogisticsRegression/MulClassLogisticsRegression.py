import numpy as np

# the num of classfier
classfier_count = 3
# the num of classfication value
# n种分类情况
classfier_kind = 0
# max cycle times
max_cycle_times = 16000
# learning step
alpha = 0.02
classMap = {}
thetaClassfierMap = {}

'''
1->Iris-setosa
2->Iris-versicolor
3->Iris-virginica
'''
classfication_mapping = {
    1: "Iris-setosa",
    2: "Iris-versicolor",
    3: "Iris-virginica"
}

'''
load original training sample data
'''


def load_data():
    print("==>Starting Read Data...")
    ori_data = np.loadtxt(
        "F:\MachineLearn\ML_Algorithm_Impl\LogisticsRegression\data\mul_logistics_regression_sample.txt",
        delimiter=",")
    print("==>Complete Read Data...")
    return np.hstack((np.ones((ori_data.shape[0], 1)), ori_data))


'''
get x(feature matrix) and y(classification values of samples) from original data
'''


def getXAndY(oriData):
    r, c = oriData.shape
    return oriData[:, 0:c - 1], oriData[:, c - 1:]


'''
decide +1 or -1 for every classfication value
'''


def allocateYForEachClassfier():
    classMap[1] = [1, 2]
    classMap[2] = [2, 3]
    classMap[3] = [1, 3]
    # for i in range(classfier_kind):
    #     yChoice = np.random.choice(3, np.random.randint(1, classfier_kind, 1), replace=False)
    #     classMap[i + 1] = yChoice + 1


'''
calculate sigmoid result
'''


def sigmoid(X, theta):
    return 1 / (1 + np.exp(-X.dot(theta)))


def gradient(X, Y, theta):
    return X.T.dot(sigmoid(X, theta) - Y)


'''
transfer Y to right experssion for each classfier
because positive class is not same between each classfier
'''


def transferY(Y, classfierIndex):
    positiveClass = classMap[classfierIndex]
    s = set(positiveClass)
    r, c = Y.shape
    yList = list()
    for i in range(r):
        for j in range(c):
            if Y[i][j] in s:
                yList.append(1)
            else:
                yList.append(0)
    return np.array([yList]).T


def kernal(X, Y):
    sampleCount, featureCount = X.shape
    i = 0
    while i < classfier_count:
        theta = np.ones((featureCount, 1))
        newY = transferY(Y, i + 1)
        cycleCursor = 0
        while cycleCursor < max_cycle_times:
            theta -= (alpha / sampleCount) * gradient(X, newY, theta)
            cycleCursor += 1
        i += 1
        thetaClassfierMap[i] = theta


if __name__ == "__main__":
    ori_data = load_data()
    X, Y = getXAndY(ori_data)
    classfier_kind = int(np.max(Y))
    print(classfier_kind)
    allocateYForEachClassfier()
    print(classMap)
    kernal(X, Y)
    print(thetaClassfierMap)
