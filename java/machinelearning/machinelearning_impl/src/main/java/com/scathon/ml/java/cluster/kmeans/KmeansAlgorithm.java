package com.scathon.ml.java.cluster.kmeans;

import com.scathon.ml.java.common.FileUtils;

import java.io.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.scathon.ml.java.cluster.kmeans.KmeansConfiguration.*;

/**
 * Kmeans聚类算法实现V1.0.0
 *
 * @author linhd
 */
public class KmeansAlgorithm {
    public static void main(String[] args) {
        List<List<Double>> oriDataList = loadData();
        List<List<Double>> initClusterCenter = getInitClusterCenter(oriDataList);
        System.out.println(initClusterCenter);
        List<List<Integer>> result = kernel(oriDataList, initClusterCenter);
        System.out.println(result);
    }

    /**
     * kmeans算法核心执行过程.
     *
     * @param oriDataList          原始数据集.
     * @param initCluserCenterList 初始化的聚类中心.
     * @return 聚类结果.
     */
    private static List<List<Integer>> kernel(List<List<Double>> oriDataList,
                                              List<List<Double>> initCluserCenterList) {
        int sampleCount = oriDataList.size();
        List<List<Integer>> cluser = new ArrayList<>(CLUSTER_NUM);
        for (int i = 0; i < CLUSTER_NUM; i++) {
            cluser.add(i, new ArrayList<>());
        }
        int iterCursor = 0;
        while (iterCursor < MAX_ITERATION_NUM) {
            for (int i = 0; i < sampleCount; i++) {
                int minClusterIndex = 0;
                double minDistance = Double.MAX_VALUE;
                for (int j = 0; j < CLUSTER_NUM; j++) {
                    double distance = calculateDistance(oriDataList.get(i), initCluserCenterList.get(j));
                    if (distance < minDistance) {
                        minDistance = distance;
                        minClusterIndex = j;
                    }
                }
                cluser.get(minClusterIndex).add(i);
            }
            for (int i = 0; i < CLUSTER_NUM; i++) {
                List<List<Double>> clusterMemeberList =
                        cluser.get(i).stream().map(oriDataList::get).collect(Collectors.toList());
                for (int j = 0; j < POINT_DIMENSION; j++) {
                    double sum = 0;
                    for (List<Double> point : clusterMemeberList) {
                        sum += point.get(j);
                    }
                    initCluserCenterList.get(i).add(j, sum / clusterMemeberList.size());
                }
            }
            if (iterCursor != MAX_ITERATION_NUM - 1) {
                cluser.forEach(List::clear);
            }
            iterCursor++;
        }
        return cluser;
    }

    private static double calculateDistance(List<Double> src, List<Double> tar) {
        int dimensions = src.size();
        double sum = 0;
        for (int i = 0; i < dimensions; i++) {
            sum += Math.pow(src.get(i) - tar.get(i), 2);
        }
        return Math.sqrt(sum);
    }

    /**
     * 获取初始的聚类中心点.
     *
     * @param oriDataList 原始数据集.
     * @return 随机筛选的聚类中心点.
     */
    private static List<List<Double>> getInitClusterCenter(List<List<Double>> oriDataList) {
        int samplePointNum = oriDataList.size();
        int haveChoiced = 0;
        Random random = new Random();
        List<List<Double>> initClusterCenter = new ArrayList<>(CLUSTER_NUM);
        Set<Integer> indexHaveChoiced = new HashSet<>();
        while (haveChoiced < CLUSTER_NUM) {
            int choicedIndex = random.nextInt(samplePointNum);
            if (indexHaveChoiced.contains(choicedIndex)) {
                continue;
            }
            indexHaveChoiced.add(choicedIndex);
            initClusterCenter.add(oriDataList.get(choicedIndex));
            haveChoiced++;
        }
        return initClusterCenter;
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
