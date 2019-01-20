package com.scathon.ml.java.common;

import java.io.*;
import java.util.function.Function;

import static com.scathon.ml.java.cluster.kmeans.KmeansConfiguration.FILE_PATH;

/**
 * @author linhd
 */
public class FileUtils {
    public static final boolean isExists(String filePath) {
        return new File(filePath).exists();
    }

    public static final InputStream pathToInputStream(String filePath, boolean fromClassPath) {
        InputStream inputStream = null;
        try {
            if (!fromClassPath) {
                if (isExists(filePath)) {
                    inputStream = new FileInputStream(filePath);
                } else {
                    throw new FileNotFoundException("文件不存在：" + filePath);
                }
            } else {
                inputStream = FileUtils.class.getClassLoader().getResourceAsStream(filePath);
            }
        } catch (FileNotFoundException e) {
            System.out.println("文件不存在：" + e.getMessage());
        }
        return inputStream;
    }

    public static <T> T loadData(String filePath, boolean fromClassPath,
                                 Function<BufferedReader, T> lineProcessFunc) {
        try (InputStream inputStream = FileUtils.pathToInputStream(filePath, fromClassPath);
             InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
             BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
            return lineProcessFunc.apply(bufferedReader);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
