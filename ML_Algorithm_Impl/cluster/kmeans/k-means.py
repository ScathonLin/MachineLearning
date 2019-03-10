import numpy as np
from numpy.linalg import inv, qr
import numpy.random
import matplotlib.pyplot as plt


# 计算欧几里得距离
def calculateDistance(center_point, data_point):
    d_x = data_point[0]
    d_y = data_point[1]
    c_x = center_point[0]
    c_y = center_point[1]
    return pow(pow(c_x - d_x, 2) + pow(c_y - d_y, 2), 0.5)


clusterCount = 4
arr = np.loadtxt('./origin.txt', delimiter=" ")
centerSet = numpy.random.randint(0, arr.size / 2, clusterCount)
centerPointSet = []
for i in centerSet:
    centerPointSet.append(arr[i])
flag = True
cluster = {}
print(type(arr))
for i in range(clusterCount):
    cluster[i] = []
iterationTimes = 10
while iterationTimes > 0:
    for point in arr:
        minDistance = 0.0
        minIndex = 0
        for idx, centerPoint in enumerate(centerPointSet):
            if idx == 0:
                minDistance = calculateDistance(centerPoint, point)
            distance = calculateDistance(centerPoint, point)
            if distance < minDistance:
                minDistance = distance
                minIndex = idx
        cluster[minIndex].append(point)

    for i in range(clusterCount):
        clusterPoints = cluster[i]
        sum_x = 0
        sum_y = 0
        for item in clusterPoints:
            sum_x += item[0]
            sum_y += item[1]
        # 更新聚类中心点
        clusterNodeSize = len(clusterPoints)
        if clusterNodeSize > 0:
            centerPointSet[i] = np.array([sum_x / clusterNodeSize, sum_y / clusterNodeSize])

    # 判断算法终止条件
    if iterationTimes != 1:
        for i in range(clusterCount):
            cluster[i].clear()
    iterationTimes -= 1

# 可视化

color_set = ['bs', 'g^', 'rs', 'r^']
idx = 0
for each_cluster in cluster.values():
    p_x = []
    p_y = []
    for item in each_cluster:
        p_x.append(item[0])
        p_y.append(item[1])
        # print(item)
    plt.plot(p_x, p_y, color_set[idx])
    idx += 1

p_x = []
p_y = []
for centerNode in centerPointSet:
    p_x.append(centerNode[0])
    p_y.append(centerNode[1])
    plt.plot(p_x, p_y, 'b^')

plt.show()
