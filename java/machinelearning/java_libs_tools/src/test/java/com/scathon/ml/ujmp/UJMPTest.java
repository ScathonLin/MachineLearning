package com.scathon.ml.ujmp;

import org.junit.Test;
import org.ujmp.core.DenseMatrix;
import org.ujmp.core.Matrix;
import org.ujmp.core.SparseMatrix;
import org.ujmp.core.doublematrix.DenseDoubleMatrix2D;
import org.ujmp.core.doublematrix.DoubleMatrix;
import org.ujmp.core.util.MathUtil;

import java.util.Arrays;

public class UJMPTest {
    @Test
    public void quickStart() {
        // 创建密集矩阵
        Matrix dense = DenseMatrix.Factory.zeros(4, 4);
        dense.setAsDouble(5.0, 2, 3);
        dense.setAsDouble(3.0, 1, 1);
        dense.setAsDouble(4.0, 2, 2);
        dense.setAsDouble(-2.0, 3, 3);
        dense.setAsDouble(-2.0, 1, 3);
        System.out.println(dense.toString());
        // 创建稀疏矩阵
        SparseMatrix sparse = SparseMatrix.Factory.zeros(4, 4);
        sparse.setAsDouble(2.0, 0, 0);
        System.out.println(sparse);
    }

    /**
     * 利用数组创建matrix
     */
    @Test
    public void createMatrixFromArr() {
        double[][] arr1 = new double[][]{
                {1, 2, 4},
                {2, 5, 3}
        };
        DenseDoubleMatrix2D matrix = DoubleMatrix.Factory.importFromArray(arr1);
        System.out.println("row: " + matrix.getRowCount());
        System.out.println("col: " + matrix.getColumnCount());
        System.out.println(matrix);
    }

    /**
     * 线性代数相关，矩阵的一些性质
     */
    @Test
    public void linearAlgebraTest() {
        double[][] arr1 = new double[][]{
                {1, 2, 4},
                {2, 5, 3}
        };
        double[][] arr2 = new double[][]{
                {1, 2},
                {4, 1},
                {2, 3}
        };

        DenseDoubleMatrix2D m1 = DoubleMatrix.Factory.importFromArray(arr1);
        DenseDoubleMatrix2D m2 = DoubleMatrix.Factory.importFromArray(arr2);
        System.out.println("m1 transpose: ");
        System.out.println(m1.transpose());
        System.out.println("m1 * m2: ");
        System.out.println(m1.mtimes(m2));

        double[][] arr3 = new double[][]{
                {1, 2},
                {4, 1},
        };
        DenseDoubleMatrix2D m3 = DoubleMatrix.Factory.importFromArray(arr3);
        Matrix m3Inv = m3.inv();
        System.out.println("m3 inverse: ");
        System.out.println(m3Inv);
        System.out.println("m3 * m3Inv: ");
        System.out.println(m3Inv.mtimes(m3));
        System.out.println("m2 pinv: ");
        System.out.println(m2.pinv());
        System.out.println("m3 svd: ");
        System.out.println(Arrays.toString(m3.svd()));
        // QR LU eig chol....
    }

    @Test
    public void mathCal() {
        double[][] arr1 = new double[][]{
                {2, 4},
                {5, 3}
        };
        double[][] arr2 = new double[][]{
                {1, 2},
                {4, 1},
        };

        DenseDoubleMatrix2D m1 = DoubleMatrix.Factory.importFromArray(arr1);
        DenseDoubleMatrix2D m2 = DoubleMatrix.Factory.importFromArray(arr2);
        System.out.println(m1.minus(m2));
        System.out.println(m1.minus(1));
        System.out.println(m1.plus(m2));
        System.out.println(m1.plus(2));
        System.out.println(m1.times(m2));
    }

    @Test
    public void randomMatrix() throws InterruptedException {
        DenseMatrix rand = Matrix.Factory.rand(100, 10);
        rand.showGUI();
        System.out.println(MathUtil.hypot(2, 4));
    }
}
