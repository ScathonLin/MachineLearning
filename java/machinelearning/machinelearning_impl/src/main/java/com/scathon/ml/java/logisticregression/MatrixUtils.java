package com.scathon.ml.java.logisticregression;

import Jama.Matrix;

/**
 * @author linhd
 */
public class MatrixUtils {

    public static Matrix minusNumRight(Matrix yMatrix, int num) {
        double[][] arr = yMatrix.getArrayCopy();
        for (int i = 0; i < arr.length; i++) {
            for (int j = 0; j < arr[i].length; j++) {
                arr[i][j] = num - arr[i][j];
            }
        }
        return new Matrix(arr);
    }
}
