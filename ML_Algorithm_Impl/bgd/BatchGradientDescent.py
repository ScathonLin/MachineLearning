import numpy as np
import matplotlib.pyplot as plt


# 获取训练集数据
def get_training_set():
    return np.loadtxt("./bgd_training.txt", delimiter=",")


checkJ = []


def kernal(train_set, theta_arr, sample_count, theta_count, step, max_iteration):
    for i in range(max_iteration):
        theta_arr_copy = theta_arr.copy()
        for theta_index in range(theta_count):
            # 计算所有误差总和部分
            sum = 0
            for train_item in train_set:
                sum += (np.dot(theta_arr.T, train_item[:len(train_item) - 1]) - train_item[len(train_item) - 1]) * \
                       train_item[theta_index]
            sum /= sample_count
            theta_arr_copy[theta_index] -= step * sum
        theta_arr = theta_arr_copy.copy()
        if (i + 1) % 100 == 0:
            JTheta(theta_arr, train_set, sample_count, i + 1, checkJ)

    return theta_arr


def JTheta(theta_arr, train_set, sample_count, index, checkJ):
    sum = 0
    for item in train_set:
        sum += np.dot(theta_arr.T, item[:len(item) - 1]) - item[len(item) - 1]
    checkJ.append((index, sum / (2 * sample_count)))


def forcast_func(theta_arr, x):
    y = theta_arr[0] + theta_arr[1] * x
    return y


# 可视化
def visualization(theta_arr, train_set):
    p_x = []
    p_y = []
    for entry in train_set:
        p_x.append(entry[0])
        p_y.append(entry[1])

    plt.plot(p_x, p_y, 'g^')
    x = np.linspace(1, 3, 10)
    y = [theta_arr[0] + theta_arr[1] * i for i in x]
    plt.plot(x, y, 'b-', linewidth=1, label='predication function gradient descent')
    plt.show()


def visualJTheta(checkJ):
    p_x = []
    p_y = []
    for (index, sum) in checkJ:
        p_x.append(index)
        p_y.append(sum)
    plt.plot(p_x, p_y, 'b', label="误差函数的走势")
    plt.show()
    x = np.linspace(0, p_x[len(p_x) - 1], len(p_x))


def main():
    # 获取训练集
    train_set = get_training_set()
    # 获取训练集数量
    sample_count, theta_count = train_set.shape
    theta_count -= 1
    print("训练集数量是：" + str(sample_count))
    print("θ系数的数量是：" + str(theta_count))
    # 定义学习步长
    step = 0.058
    print("学习步长是：" + str(step))
    theta_arr = np.ones(theta_count)
    print("theta 初始系数矩阵是：", theta_arr)
    # 定义最大迭代次数
    max_iteration = 10000
    theta_arr = kernal(train_set, theta_arr, sample_count, theta_count, step, max_iteration)
    print(theta_arr)
    print(checkJ)
    visualization(theta_arr, train_set)
    visualJTheta(checkJ)


if __name__ == '__main__':
    main()
