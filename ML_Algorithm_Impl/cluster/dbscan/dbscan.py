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

    def exec_kernel(self):
        logger.info("start exec dbscan kernel process....")
        core_data_points_list = self.find_core_data_points(self.data_point_list)
        random.shuffle(core_data_points_list)
        cluster_index = 0
        for core_datapoint in core_data_points_list:
            if core_datapoint.visited:
                continue
            q = queue.Queue()
            q.put(core_datapoint)
            while not q.empty():
                data_point = q.get()
                data_point.visited = True
                if data_point.is_core_point:
                    data_point.cluster = cluster_index
                    for neighbor_point in data_point.neigh_points:
                        if neighbor_point.visited:
                            continue
                        q.put(neighbor_point)
                        neighbor_point.cluster = cluster_index
            cluster_index += 1
        logger.info("complete exec dbscan kernel process")

    def process_result(self) -> dict:
        cluster = {}
        for datapoint in self.data_point_list:
            datapoints = cluster.get(datapoint.cluster)
            if datapoints is None:
                datapoints = []
                cluster[datapoint.cluster] = datapoints
            datapoints.append(datapoint)
        return cluster

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

    @staticmethod
    def cal_euclid_distance(src_point: list, dst_point: list):
        if len(src_point) != len(dst_point):
            raise ArithmeticError("the dimension between src_point and dst_dimension is not equal,please check...")
        dimension = len(src_point)
        pow_sum = 0
        for i in range(dimension):
            pow_sum += pow(src_point[i] - dst_point[i], 2)
        return math.sqrt(pow_sum)

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
