package com.scathon.ml.java.cluster.dbscan;


import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * DBScan算法配置信息封装.
 *
 * @author linhd
 */
public class DBScanConfiguration {
    public static String FILE_PATH;
    public static int POINT_DIMENSION;
    public static double MIN_DISTANCE;
    public static int CLUSTER_POINTS_MIN_NUM;

    static {
        try (InputStream configInputStream = DBScanConfiguration.class.getClassLoader().getResourceAsStream("cluster" +
                "/dbscan" +
                "/config/dbscan.properties")) {
            Properties prop = new Properties();
            prop.load(configInputStream);
            FILE_PATH = prop.getProperty("dbscan.training.file.path");
            POINT_DIMENSION = Integer.parseInt(prop.getProperty("dbscan.point.dimension"));
            MIN_DISTANCE = Double.parseDouble(prop.getProperty("dbscan.points.distance.min.threshold"));
            CLUSTER_POINTS_MIN_NUM = Integer.parseInt(prop.getProperty("dbscan.cluster.points.min.num"));
        } catch (IOException e) {
            System.err.println("加载dbscan算法配置文件失败！");
            System.exit(-1);
        }
    }
}
