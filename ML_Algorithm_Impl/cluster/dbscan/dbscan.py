from cluster.dbscan.dbscan_config import *
from utils.logger import *
from utils.common_utils import CommonUtils
import math
import random
import queue
import matplotlib.pyplot as plt


class DataNode:
    node_counter = -1

    def __init__(self, data_point: list):
        DataNode.node_counter += 1
        self.node_index = DataNode.node_counter
        # 是否被访问过了.
        self.visited = False
        # 是否是噪声点.
        self.is_noised = True
        # 归属于那个集群.
        self.cluster = -1
        # 数据点的数据.
        self.data_point = data_point
        # 邻居节点.
        self.neigh_points = []
        # 是否是核心点
        self.is_core_point = False


class DBScan:

    def __init__(self, conf: dict):
        # 邻域节点数量最小值.
        self.__neighbor_ele_num = int(conf['neighbor_ele_num'])
        # 集群节点间的满足的最小距离.
        self.__points_min_distance_threashold = float(conf['points_min_distance_threashold'])
        # 数据点的坐标维度.
        self.__datapoint_dimension = conf['datapoint_dimension']
        # 数据集.
        self.data_point_list = self.load_data()

    # dbscan 核心执行逻辑.
    def exec_kernel(self):
        logger.info("start exec dbscan kernel process....")
        core_data_points_list = self.find_core_data_points(self.data_point_list)
        # 打乱所有的核心点的顺序
        random.shuffle(core_data_points_list)
        cluster_index = 0
        # 遍历核心点，这里因为是打乱顺序后的核心点，所以相当于随机选了一个核心点开始迭代计算.
        for core_datapoint in core_data_points_list:
            # 如果核心点已经被访问了，那么跳过本次迭代计算.
            if core_datapoint.visited:
                continue
            # 初始化一个空队列，将随机选择的核心点加入到队列中.
            q = queue.Queue()
            q.put(core_datapoint)
            # 直至队列中所有的点都被访问了，此时一个cluster就形成了.
            while not q.empty():
                data_point = q.get()
                # 标记数据点已经被访问了.
                data_point.visited = True
                # 如果数据点是核心点，将其邻域节点加入到队列中.
                if data_point.is_core_point:
                    # 设置集群id.
                    data_point.cluster = cluster_index
                    for neighbor_point in data_point.neigh_points:
                        # 将未访问过的节点加入到队列中，如果不加判断，那么这里将会使得queue不断膨胀，导致死循环.
                        if neighbor_point.visited:
                            continue
                        q.put(neighbor_point)
                        # 设置邻域节点的集群id.
                        neighbor_point.cluster = cluster_index
            # 集群id更新（+1）
            cluster_index += 1
        logger.info("complete exec dbscan kernel process")

    #  处理聚类结果数据，用于后续可视化展示.
    def process_result(self) -> dict:
        cluster = {}
        for datapoint in self.data_point_list:
            datapoints = cluster.get(datapoint.cluster)
            if datapoints is None:
                datapoints = []
                cluster[datapoint.cluster] = datapoints
            datapoints.append(datapoint)
        return cluster

    # 发现核心数据点.
    def find_core_data_points(self, data_point_list: list) -> list:
        logger.info("start find core data points...")
        core_points_with_neighbors_list = []
        for out_index, src_data_point in enumerate(data_point_list):
            count = 0
            neighbor_datapoints = []
            for in_index, dst_data_point in enumerate(data_point_list):
                if in_index == out_index:
                    continue
                distance = DBScan.cal_euclid_distance(src_data_point.data_point, dst_data_point.data_point)
                if distance <= self.__points_min_distance_threashold:
                    count += 1
                    neighbor_datapoints.append(dst_data_point)

            # 满足邻域节点个数的数据点标记为核心数据点，设置核心数据点标志位is_core_point.
            if count >= self.__neighbor_ele_num:
                core_points_with_neighbors_list.append(src_data_point)
                src_data_point.neigh_points = neighbor_datapoints
                src_data_point.is_core_point = True
        logger.info("complete find core data points: %s " % core_points_with_neighbors_list)
        return core_points_with_neighbors_list

    @staticmethod
    def show(data_processed: dict):
        color_set = ['bs', 'g^', 'rs', 'r^']
        idx = 0
        for entry in data_processed.items():
            datapoints = entry[1]
            p_x = []
            p_y = []
            for datapoint in datapoints:
                p_x.append(datapoint.data_point[0])
                p_y.append(datapoint.data_point[1])

            color = color_set[idx]
            if entry[0] == -1:
                color = 'ko'

            plt.plot(p_x, p_y, color)
            idx += 1

        plt.show()

    # 计算欧几里得距离.
    @staticmethod
    def cal_euclid_distance(src_point: list, dst_point: list):
        if len(src_point) != len(dst_point):
            raise ArithmeticError("the dimension between src_point and dst_dimension is not equal,please check...")
        dimension = len(src_point)
        pow_sum = 0
        for i in range(dimension):
            pow_sum += pow(src_point[i] - dst_point[i], 2)
        return math.sqrt(pow_sum)

    # 加载数据，此方法为手动实现，可以使用numpy简化操作.
    @staticmethod
    def load_data() -> list:
        logger.info('start read data file, file path is : %s' % data_file_path)
        data_list = []
        if not os.path.exists(data_file_path):
            raise FileNotFoundError('file: %s is not exists' % data_file_path)

        try:
            with open(data_file_path, mode='r', encoding='utf-8') as file:
                for line in file.readlines():
                    segments = line.strip().split(sep=' ')
                    data_list.append(list(map(lambda x: float(x), segments)))
            data_point_list = list(map(lambda data_tuple: DataNode(data_tuple), data_list))
        except Exception as e:
            logger.error(e)

        logger.info('finished load data from file: %s ' % data_file_path)
        return data_point_list


# 执行算法.
def start():
    conf_file_path = os.path.join(__file__, '..', 'conf', 'dbscan.conf')
    conf_dict = CommonUtils.load_config_to_dict(conf_file_path)
    logger.info('config info of dbscan is : %s\n' % conf_dict)
    dbscan = DBScan(conf_dict)
    dbscan.exec_kernel()
    result_data = dbscan.process_result()
    dbscan.show(result_data)


if __name__ == '__main__':
    start()
