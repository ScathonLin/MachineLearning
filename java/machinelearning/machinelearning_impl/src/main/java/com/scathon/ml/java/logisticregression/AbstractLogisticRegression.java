package com.scathon.ml.java.logisticregression;

import Jama.Matrix;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author linhd
 */
public abstract class AbstractLogisticRegression implements LogisticRegression {
    /**
     * logistics regression task execution entrance
     */
    @Override
    public Matrix startLogisticRegression() {
        //TODO Start Logistics Regression Application
        return null;
    }

    @Override
    public Matrix getThetaMatrix(int featureCount) {
        double[][] featureArr = new double[featureCount][1];
        for (int i = 0; i < featureArr.length; i++) {
            featureArr[i][0] = 1;
        }
        return new Matrix(featureArr);
    }

    @Override
    public Matrix getY(Matrix trainingSet) {
        int row = trainingSet.getRowDimension();
        int col = trainingSet.getColumnDimension();
        return trainingSet.getMatrix(0, row - 1, col - 1, col - 1);
    }

    @Override
    public Matrix getOriginalDataMatrix() {
        try (InputStream inputStream = BinaryLogisticsRegression.class.getClassLoader()
                .getResourceAsStream(LRConfiguration.DATA_FILE_NAME);
             InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
             BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
            String line;
            List<List<Double>> dataList = new LinkedList<>();
            while ((line = bufferedReader.readLine()) != null) {
                List<Double> lineSplitList = Arrays.stream(line.split(LRConfiguration.LINE_SPLIT_SEPARATOR))
                        .map(Double::parseDouble)
                        .collect(Collectors.toList());
                dataList.add(lineSplitList);
            }
            double[][] dataArray = new double[dataList.size()][];
            for (int i = 0; i < dataList.size(); i++) {
                dataArray[i] = new double[dataList.get(i).size() + 1];
                dataArray[i][0] = 1;
                for (int j = 0; j < dataArray[i].length - 1; j++) {
                    dataArray[i][j + 1] = dataList.get(i).get(j);
                }
            }
            return new Matrix(dataArray);
        } catch (IOException ex) {
            System.err.println("读取训练数据发生异常");
        }
        return null;
    }
}
