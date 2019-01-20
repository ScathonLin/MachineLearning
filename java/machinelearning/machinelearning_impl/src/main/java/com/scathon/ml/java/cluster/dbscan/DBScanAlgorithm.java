package com.scathon.ml.java.cluster.dbscan;

import com.scathon.ml.java.common.FileUtils;
import org.apache.commons.collections4.CollectionUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.scathon.ml.java.cluster.dbscan.DBScanConfiguration.*;

/**
 * DBScan 聚类算法实现v1.0.0
 *
 * @author linhd
 */
public class DBScanAlgorithm {
    public static void main(String[] args) {
        Map<Integer, Collection<Integer>> result = kernel();
        System.out.println(result);
    }

    private static Map<Integer, Collection<Integer>> kernel() {
        List<List<Double>> oriDataList = loadData();
        Map<Integer, List<Integer>> neightbors = new HashMap<>(1 << 4);
        List<Integer> corePts = new ArrayList<>();
        obtainInitCorePtsAndNeighboirs(oriDataList, neightbors, corePts);
        Collection<Integer> allPtdIdList = new HashSet<>();
        Collection<Integer> tmpPtdIdList = new HashSet<>();
        for (int i = 0; i < oriDataList.size(); i++) {
            allPtdIdList.add(i);
            tmpPtdIdList.add(i);
        }
        // cluster index
        int k = 0;
        Map<Integer, Collection<Integer>> clusterMap = new HashMap<>(1 << 4);
        Random random = new Random();
        // 循环终止条件是，核心对象集合为空，因为每轮迭代都会移除一部分元素
        while (!CollectionUtils.isEmpty(corePts)) {
            int corePtsNum = corePts.size();
            // 随机选取一个核心对象进行计算.
            int corePtsId = corePts.get(random.nextInt(corePtsNum));
            // 存放密度相连的点的数组索引.
            ArrayDeque<Integer> queue = new ArrayDeque<>();
            // 将随机选取的核心对象点加入到队列中.
            queue.addFirst(corePtsId);
            // 当队列不为空的时候进行迭代.
            while (!queue.isEmpty()) {
                // 出队一个对象.
                int ptsId = queue.poll();
                // 取出核心对象的邻域中的点.
                List<Integer> neiPtsIdList = neightbors.get(ptsId);
                // 如果集合为空，说明不是核心对象
                if (CollectionUtils.isEmpty(neiPtsIdList)) {
                    continue;
                }
                // 求当前核心对象邻域中的点与样本集合中的点交集，将求出的交集加入到队列中.
                Collection<Integer> ptsIdsAddToDeque = CollectionUtils.intersection(tmpPtdIdList, neiPtsIdList);
                queue.addAll(ptsIdsAddToDeque);
                // 将样本集合中的已经加入到队列中的对象点删除.
                tmpPtdIdList = CollectionUtils.subtract(tmpPtdIdList, ptsIdsAddToDeque);
            }
            // 求出已经找到集群的对象点(所有点-剩下的点).
            Collection<Integer> ptsIdsInCurrentCluster = CollectionUtils.subtract(allPtdIdList, tmpPtdIdList);
            // 将已经找到集群的对象点放入clusterMap中.
            clusterMap.put(k++, ptsIdsInCurrentCluster);
            // 将对象集合置为剩下的对象点集合
            allPtdIdList = tmpPtdIdList;
            // 移除核心对象点数组中已经访问过的那些核心对象点的id
            corePts.removeAll(ptsIdsInCurrentCluster.stream().filter(corePts::contains).collect(Collectors.toList()));
        }
        return clusterMap;
    }


    /**
     * 获取核心点对象以及每个核心点的邻域中的点.
     *
     * @param oriDataList 原始数据集.
     * @param neighbors   每个核心点的邻域中的点的集合.
     * @param corePts     核心点集合（存的是点的索引）.
     */
    private static void obtainInitCorePtsAndNeighboirs(List<List<Double>> oriDataList,
                                                       Map<Integer, List<Integer>> neighbors,
                                                       List<Integer> corePts) {
        int ptsNum = oriDataList.size();
        for (int i = 0; i < ptsNum; i++) {
            int count = 0;
            List<Integer> tmpNeighList = new ArrayList<>();
            for (int j = 0; j < ptsNum; j++) {
                // 计算欧几里得距离
                double distance = calculateEuclidDistance(oriDataList.get(i), oriDataList.get(j));
                // 如果距离小于设置的阈值，进行计数，将对象点加入集合中.
                if (distance < MIN_DISTANCE) {
                    count++;
                    tmpNeighList.add(j);
                }
            }
            // 如果当前点的邻域总的对象点的个数大于设置的阈值，保存当前对象点为核心对象.
            if (count > CLUSTER_POINTS_MIN_NUM) {
                neighbors.put(i, tmpNeighList);
                corePts.add(i);
            }
        }
    }

    /**
     * 计算欧氏距离.
     *
     * @param srcPts 源点.
     * @param tarPts 目标点.
     * @return distance value.
     */
    private static double calculateEuclidDistance(List<Double> srcPts, List<Double> tarPts) {
        int dimensions = srcPts.size();
        double sum = 0;
        for (int i = 0; i < dimensions; i++) {
            sum += Math.pow(srcPts.get(i) - tarPts.get(i), 2);
        }
        return Math.sqrt(sum);
    }

    @SuppressWarnings("Duplicates")
    private static List<List<Double>> loadData() {
        Function<BufferedReader, List<List<Double>>> lineProcessFunc = br -> {
            List<List<Double>> dataList = new ArrayList<>();
            try {
                String line;
                while ((line = br.readLine()) != null) {
                    List<Double> lineDataList = new ArrayList<>(POINT_DIMENSION);
                    String[] lineSplitArr = line.split(",");
                    if (lineSplitArr.length != POINT_DIMENSION) {
                        continue;
                    }
                    Arrays.stream(lineSplitArr).forEach(item -> lineDataList.add(Double.parseDouble(item)));
                    dataList.add(lineDataList);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return dataList;
        };
        return FileUtils.loadData(FILE_PATH, true, lineProcessFunc);
    }
}

