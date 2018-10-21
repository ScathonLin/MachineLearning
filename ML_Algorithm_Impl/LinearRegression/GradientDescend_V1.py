# 线性规划梯度下降算法Version1
import numpy as np
import matplotlib.pyplot as plt
from mpl_toolkits.mplot3d import Axes3D
from matplotlib import cm
from numpy import random


# 计算梯度向量
def generate_gradient(X, theta, y):
    sample_count = X.shape[0]
    # 计算梯度，采用矩阵进行计算，代码很简洁
    return (1./sample_count)*X.T.dot(X.dot(theta)-y)


# 读取训练集数据
def get_training_sample_data(file_path):
    ori_data = np.loadtxt(file_path, delimiter=",")
    cols = ori_data.shape[1]
    return (ori_data, ori_data[:, :cols - 1], ori_data[:, cols-1:])

# 初始化θ数组
def init_theta(feature_count):
    return np.ones(feature_count).reshape(feature_count, 1)


# 记录J(θ)的变化趋势，验证梯度下降是否运行正确
diff_value = []


def gradient_descend_kernal(X, theta, y, step):
    h = (X.dot(theta)-y).T.dot(X.dot(theta)-y)
    index = 0
    init_gradient = generate_gradient(X, theta, y)
    while not np.all(np.absolute(init_gradient) <= 1e-5):
        theta = theta - step * init_gradient
        init_gradient = generate_gradient(X, theta, y)
        # 计算误差值
        h = (X.dot(theta)-y).T.dot(X.dot(theta)-y)
        if (index+1) % 10 == 0:
            diff_value.append((index, h[0]))
        index += 1
    return theta


def visualJTheta(diff_value):
    p_x = []
    p_y = []
    for (index, sum) in diff_value:
        p_x.append(index)
        p_y.append(sum)
    plt.plot(p_x, p_y, 'b', label="difference function")
    plt.show()
    x = np.linspace(0, p_x[len(p_x) - 1], len(p_x))

# 可视化
def visualization(theta, sample_training_set):
    fig = plt.figure()
    ax = Axes3D(fig)
    plt.title("Linear Regression With Gradient Descend")
    ax.legend()
    ax.set_xlabel('X', color='r')
    ax.set_ylabel('Y', color='g')
    ax.set_zlabel('Z', color='b')
    x, y, z = sample_training_set[:,
                                  1], sample_training_set[:, 2], sample_training_set[:, 3]
    ax.scatter(x, y, z, c='b')  # 绘制数据点
    X, Y = np.meshgrid(sample_training_set[:, 1], sample_training_set[:, 2])
    Z = theta[0] + theta[1]*X + theta[2]*Y
    ax = fig.gca(projection='3d')
    surf = ax.plot_surface(X, Y, Z, color='g')
    plt.show()


def app_main():
    training_sample_include_y, training_sample, y = get_training_sample_data(
        'F:\MachineLearn\ML_Algorithm_Impl\LinearRegression\data\sample.txt')
    sample_count, feature_count = training_sample.shape
    step = 0.01
    theta = init_theta(feature_count)
    result_theta = gradient_descend_kernal(training_sample, theta, y, step)
    print(result_theta)
    visualJTheta(diff_value)
    visualization(result_theta, training_sample_include_y)


if __name__ == '__main__':
    app_main()
