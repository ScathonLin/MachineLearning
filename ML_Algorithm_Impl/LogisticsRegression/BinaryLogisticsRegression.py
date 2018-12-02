import matplotlib.pyplot as plt
import numpy as np

'''
 加载训练数据
'''


def load_data():
    oridata = np.loadtxt(
        'F:\MachineLearn\ML_Algorithm_Impl\LogisticsRegression\data\ml_in_action.txt', delimiter='\t')
    return np.hstack((np.ones((oridata.shape[0], 1)), oridata))


'''
 计算sigmoid函数的值
'''


def get_sigmoid_func_value(theta_matrix, dataset_X):
    # print(1.0/(1.0+np.exp(-dataset_X.dot(theta_matrix))))
    return 1.0 / (1.0 + np.exp(-dataset_X.dot(theta_matrix)))


""" 
计算梯度值 
"""


def generate_gradient(record_count, theta_matrix, dataset_X, dataset_y):
    return (1 / record_count) * dataset_X.T.dot(get_sigmoid_func_value(theta_matrix, dataset_X) - dataset_y)
    # return dataset_X.T.dot(get_sigmoid_func_value(theta_matrix, dataset_X)-dataset_y)


""" 采集计算过程中代价函数的趋势，绘图展示代价函数的走向，判断梯度下降运行是否正确 """


def generate_cost_value(dataset_X, dataset_y, theta_matrix):
    sigmoid = get_sigmoid_func_value(theta_matrix, dataset_X)
    return -1 * dataset_y.T.dot(np.log(sigmoid)) - (1 - dataset_y).T.dot(np.log(1 - sigmoid))


def classification_kernal(dataset_X, dataset_y, theta_matrix):
    record_count = dataset_X.shape[0]
    gradient = generate_gradient(
        record_count, theta_matrix, dataset_X, dataset_y)
    alpha = 0.001
    loop = 0
    # while not np.all(np.absolute(gradient) <= 1e-2):
    while loop < 50000:
        theta_matrix = theta_matrix - alpha * gradient
        gradient = generate_gradient(
            record_count, theta_matrix, dataset_X, dataset_y)
        # print(gradient)
        loop += 1
        # print(generate_cost_value(dataset_X,dataset_y,theta_matrix))
    return theta_matrix


# 可视化
def visualization(theta_matrix, dataset_X, dataset_y):
    fittingFig = plt.figure()
    title = 'Logistic Regression Demo'
    ax = fittingFig.add_subplot(111, title=title)
    ax.set_xlabel('X1')
    ax.set_ylabel('X2')
    m, n = dataset_X.shape
    print('Starting Draw Data Point...')
    for i in range(m):
        x = dataset_X[i]
        if dataset_y[i] == 1:
            ax.scatter(x[1], x[2], marker='*', color='black', s=50)
        else:
            ax.scatter(x[1], x[2], marker='o', color='green', s=50)
    print("Starting Draw Decision Border...")
    x1_min = dataset_X[:, 1].min()
    x1_max = dataset_X[:, 1].max()
    x2_min = dataset_X[:, 2].min()
    x2_max = dataset_X[:, 2].max()
    xx1, xx2 = np.meshgrid(np.linspace(x1_min, x1_max),
                           np.linspace(x2_min, x2_max))
    h = get_sigmoid_func_value(theta_matrix,
                               np.c_[np.ones((xx1.ravel().shape[0], 1)), xx1.ravel(), xx2.ravel()])
    h = h.reshape(xx1.shape)
    print('------------')
    print(h)
    print('------------')
    print(theta_matrix)
    plt.contour(xx1, xx2, h, [0.5], colors='b', linewidth=.5)
    plt.show()


def classification_entrance():
    training_data = load_data()
    ori_theta_matrix = np.ones((training_data.shape[1] - 1, 1))
    dataset_X = training_data[:, 0:training_data.shape[1] - 1]
    dataset_y = training_data[:, training_data.shape[1] - 1:]
    result_theta = classification_kernal(
        dataset_X, dataset_y, ori_theta_matrix)
    visualization(result_theta, dataset_X, dataset_y)


def app():
    classification_entrance()


if __name__ == '__main__':
    app()
