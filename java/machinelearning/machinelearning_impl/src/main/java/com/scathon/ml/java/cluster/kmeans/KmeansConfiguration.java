package com.scathon.ml.java.cluster.kmeans;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class KmeansConfiguration {
    public static String FILE_PATH;
    public static int MAX_ITERATION_NUM;
    public static int POINT_DIMENSION;
    public static int CLUSTER_NUM;

    static {
        try (InputStream configInputStream = KmeansConfiguration.class.getClassLoader().getResourceAsStream("cluster" +
                "/kmeans" +
                "/config/kmeans.properties");) {
            Properties prop = new Properties();
            prop.load(configInputStream);
            FILE_PATH = prop.getProperty("kmeans.training.file.path");
            MAX_ITERATION_NUM = Integer.parseInt(prop.getProperty("kmeans.max.iteration.num"));
            POINT_DIMENSION = Integer.parseInt(prop.getProperty("kmeans.point.dimension"));
            CLUSTER_NUM = Integer.parseInt(prop.getProperty("kmeans.cluster.num"));
        } catch (IOException e) {
            System.err.println("加载kmeans算法配置文件失败！");
            System.exit(-1);
        }
    }
}
