package com.huawei.scathon.aiops.logisticsregression;

import Jama.Matrix;

/**
 * Binary Logistics Regression
 *
 * @author linhuadong l00483433
 */
public class BinaryLogisticsRegression extends AbstractLogisticRegression {

    public static void main(String[] args) {
        Configuration.DATA_FILE_NAME = "binary_logistic_regression_data.txt";
        LogisticRegression logisticsRegression = new BinaryLogisticsRegression();
        Matrix theta = logisticsRegression.startLogisticRegression();
        theta.print(10, 9);
    }

    private static int count(Matrix xMatrix, Matrix yMatrix, Matrix theta) {
        int row = xMatrix.getRowDimension();
        int count = 0;
        for (int i = 0; i < row; i++) {
            double v = xMatrix.getMatrix(i, i, 0, xMatrix.getColumnDimension() - 1).times(theta).get(0, 0);
            double s = 1 / (1 + Math.exp(v));
            if (Math.abs(s - yMatrix.get(i, 0)) <= 1e-2) {
                count++;
            }
        }
        return count;
    }

    @Override
    public Matrix startLogisticRegression() {
        Matrix dataMatrix = getOriginalDataMatrix();
        assert dataMatrix != null;
        Matrix xMatrix = getX(dataMatrix);
        Matrix yMatrix = getY(dataMatrix);
        int featureCount = xMatrix.getColumnDimension();
        // except the first and end col,the first col is index, the end col is classfication value
        Matrix theta = getThetaMatrix(featureCount);
        int cycleCursor = 0;
        for (; cycleCursor < Configuration.MAX_CYCLE_TIMES; cycleCursor++) {
            kernal(xMatrix, yMatrix, theta);
        }
        System.out.println(count(xMatrix, yMatrix, theta));
        return theta;
    }

    @Override
    public void loss(Matrix xMatrix, Matrix yMatrix, Matrix theta) {
        Matrix sigmoidMatrix = sigmoid(xMatrix, yMatrix, theta);
        int rowDim = sigmoidMatrix.getRowDimension();
        double[][] sigmoidMatArr = new double[rowDim][1];
        double[][] oneMinusSigmoidMatArr = new double[rowDim][1];
        for (int i = 0; i < sigmoidMatArr.length; i++) {
            double iZero = sigmoidMatrix.get(i, 0);
            sigmoidMatArr[i][0] = Math.log(iZero);
            oneMinusSigmoidMatArr[i][0] = Math.log(1 - iZero);
        }
        Matrix sigmoidLogMat = new Matrix(sigmoidMatArr);
        Matrix oneMinusSigmoidLogMat = new Matrix(oneMinusSigmoidMatArr);
        Matrix lossMatrix = sigmoidLogMat.transpose()
            .times(yMatrix)
            .plus(oneMinusSigmoidLogMat.transpose().times(MatrixUtils.minusNumRight(yMatrix, 1)));
        lossMatrix.print(5, 4);
    }

    @Override
    public void kernal(Matrix xMatrix, Matrix yMatrix, Matrix theta) {
        Matrix sigmoidMatrix = sigmoid(xMatrix, yMatrix, theta);
        int rowDim = yMatrix.getRowDimension();
        double[][] sigmoidValueMinusYArr = new double[rowDim][1];
        double[][] sigmoidMatrixArray = sigmoidMatrix.getArray();
        for (int i = 0; i < sigmoidMatrixArray.length; i++) {
            sigmoidValueMinusYArr[i][0] = sigmoidMatrixArray[i][0] - yMatrix.get(i, 0);
        }
        Matrix sigmoidValueMinusYMatrix = new Matrix(sigmoidValueMinusYArr);
        theta.minusEquals(
            xMatrix.transpose().times(sigmoidValueMinusYMatrix).times(Configuration.LEARNING_STEP / rowDim));
        theta.print(5, 4);
    }

    @Override
    public Matrix sigmoid(Matrix xMatrix, Matrix yMatrix, Matrix theta) {
        int rowDim = yMatrix.getRowDimension();
        // allocate a rowDim*colDim Dimension Matrix
        double[][] sigmoidValueArr = new double[rowDim][1];
        Matrix thetaTimeX = xMatrix.times(theta);
        double[][] thetaTimeXArray = thetaTimeX.getArray();
        for (int i = 0; i < thetaTimeXArray.length; i++) {
            sigmoidValueArr[i][0] = 1 / (1 + Math.exp(-thetaTimeXArray[i][0]));
        }
        return new Matrix(sigmoidValueArr);
    }

    @Override
    public Matrix getX(Matrix trainingSet) {
        return trainingSet.getMatrix(0, trainingSet.getRowDimension() - 1, 0, trainingSet.getColumnDimension() - 2);
    }
}

