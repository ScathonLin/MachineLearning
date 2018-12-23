package com.huawei.scathon.aiops.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 程序配置参数封装类
 *
 * @author l00483433
 */
public class AppPropertiesHolder {
    public static String LOUVAIN_SAMPLE_DATA_PATH = null;
    public static String LOUVAIN_SAMPLE_DATA_LINE_SEPARATOR = null;

    static {
        Properties properties = new Properties();
        try (InputStream resourceStream = AppPropertiesHolder.class.getClassLoader()
            .getResourceAsStream("app.properties")) {
            properties.load(resourceStream);
            LOUVAIN_SAMPLE_DATA_PATH = properties.getProperty("louvain.input.data.path");
            String separator = properties.getProperty(
                "louvain.input.data.line.separator");
            if (Constants.SPACE_SEPARATOR_NAME.equals(separator)) {
                LOUVAIN_SAMPLE_DATA_LINE_SEPARATOR = Constants.SPACE_SEPARATOR;
            } else if (Constants.TAB_SEPARATOR_NAME.equals(separator)) {
                LOUVAIN_SAMPLE_DATA_LINE_SEPARATOR = Constants.TAB_SEPARATOR;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
