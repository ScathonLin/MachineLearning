package com.scathon.ml.java.logisticregression;

import Jama.Matrix;

/**
 * @author linhd
 */
public class BinaryLogisticsRegression extends AbstractLogisticRegression {
    public static void main(String[] args) {
        LRConfiguration.DATA_FILE_NAME = "binary_classfication_data.txt";
        LRConfiguration.LEARNING_STEP = 0.001;
        LRConfiguration.MAX_CYCLE_TIMES = 50000;
        LRConfiguration.LINE_SPLIT_SEPARATOR = "\t";
        LogisticRegression logisticsRegression = new BinaryLogisticsRegression();
        Matrix theta = logisticsRegression.startLogisticRegression();
        System.out.println("Theta:");
        theta.print(10, 9);
    }

    private static int getClassficationDiff(Matrix xMatrix, Matrix yMatrix, Matrix theta) {
        int row = xMatrix.getRowDimension();
        int count = 0; 
        for (int i = 0; i < row; i++) {
            double v = xMatrix.getMatrix(i, i, 0, xMatrix.getColumnDimension() - 1).times(theta).get(0, 0);
            double s = 1 / (1 + Math.exp(-v));
            int flag = 0;
            if (s > 0.5) {
                flag = 1;
            }
            if (flag == yMatrix.get(i, 0)) {
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
        for (; cycleCursor < LRConfiguration.MAX_CYCLE_TIMES; cycleCursor++) {
            kernal(xMatrix, yMatrix, theta);
        }
        int rightDiff = getClassficationDiff(xMatrix, yMatrix, theta);
        int wrongDiff = xMatrix.getRowDimension() - rightDiff;
        System.out.println("Right: " + rightDiff + ", Wrong: " + wrongDiff + ", Right Ratio: " + rightDiff * 1.0 / (wrongDiff + rightDiff));
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
                xMatrix.transpose().times(sigmoidValueMinusYMatrix).times(LRConfiguration.LEARNING_STEP / rowDim));
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
