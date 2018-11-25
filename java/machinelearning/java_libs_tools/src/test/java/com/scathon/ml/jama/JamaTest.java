package com.scathon.ml.jama;

import Jama.LUDecomposition;
import Jama.Matrix;
import org.junit.Test;

/**
 * @Description java 矩阵运算包 jama 的使用教程
 * @Author cn:Lin Huadong en: ScathonLin
 * @Mail scathonlin@gmail.com
 * @Create 2018-11-25
 */
public class JamaTest {
    double[][] a = new double[][]{{1, 2, 3, 4}, {4, 5, 6, 7}};
    Matrix matrix = new Matrix(a);

    /**
     * 构造矩阵matrix
     */
    @Test
    public void construct() {
        double[][] a = new double[][]{{1, 2, 3, 4}, {4, 5, 6, 7}};
        Matrix matrix = new Matrix(a);
        /**
         * Print函数参数解释：
         *  @param w    Column width. 打印每个元素的长度
         *  @param d    Number of digits after the decimal. 保留的小数的位数
         */
        matrix.print(4, 2);
    }

    /**
     * 设置矩阵元素值
     * 下标都是从0开始
     */
    @Test
    public void set() {
        matrix.set(1, 1, 666);
        matrix.print(4, 1);
    }

    /**
     * 取值
     */
    @Test
    public void get() {
        matrix.print(4, 0);
        double value = matrix.get(1, 1);
        System.out.println(value);
        /*
        取出矩阵中的某一部分作为子矩阵返回
        第1、2两个参数分别代表起始的行的index，
        第3、4两个参数分别代表起始的列的index，
        注意的是：
        这是个闭区间，不是通常的左闭右开区间
         */
        matrix.getMatrix(0, 1, 2, 3).print(3, 0);
        // 获取第二列数据
        System.out.println("==>Matrix Row Dimension: " + matrix.getRowDimension());
        matrix.getMatrix(0, matrix.getRowDimension() - 1, 2, 2).print(3, 0);
    }

    //==============矩阵元素级别的操作===================

    /**
     * 矩阵加法
     * 会进行同型矩阵的校验
     */
    @Test
    public void plus() {
        double[][] a1 = new double[][]{{-1, 1, 0}, {-4, 3, 0}, {1, 0, 2}};
        double[][] a2 = new double[][]{{1, 1, 0}, {1, 3, 0}, {1, 0, 2}};
        Matrix m1 = new Matrix(a1);
        Matrix m2 = new Matrix(a2);
        Matrix resultMatrix = m1.plus(m2);
        resultMatrix.print(4, 0);
    }

    /**
     * 矩阵减法
     */
    @Test
    public void minus() {
        double[][] a1 = new double[][]{{-1, 1, 0}, {-4, 3, 0}, {1, 0, 2}};
        double[][] a2 = new double[][]{{1, 1, 0}, {1, 3, 0}, {1, 0, 1}};
        Matrix m1 = new Matrix(a1);
        Matrix m2 = new Matrix(a2);
        Matrix resultMatrix = m1.minus(m2);
        resultMatrix.print(4, 0);
    }

    /**
     * 矩阵乘法
     */
    @Test
    public void times() {
        double[][] a1 = new double[][]{{-1, 1, 0}, {-4, 3, 0}};
        double[][] a2 = new double[][]{{1, 1, 0}, {1, 3, 0}, {1, 0, 1}};
        Matrix m1 = new Matrix(a1);
        Matrix m2 = new Matrix(a2);
        m1.times(m2).print(3, 0);
    }

    /**
     * 矩阵放大缩小
     */
    @Test
    public void timesByNum() {
        double[][] a1 = new double[][]{{-1, 1, 0}, {-4, 3, 0}, {1, 0, 2}};
        Matrix m1 = new Matrix(a1);
        m1.times(10).print(3, 0);
    }

    /**
     * 矩阵除法
     * 除数是0有问题,print方法显示的是乱码，转换成数据显示的是NaN，
     * 使用的时候，要改进源码中的方法。
     */
    @Test
    public void divide() {
        double[][] a1 = new double[][]{{-1, 1, 0}, {-4, 3, 0}, {1, 0, 2}};
        double[][] a2 = new double[][]{{1, 1, 0}, {1, 3, 0}, {1, 0, 1}};
        Matrix m1 = new Matrix(a1);
        Matrix m2 = new Matrix(a2);
        System.out.println("==>矩阵左除");
        m1.arrayLeftDivideEquals(m2).print(3, 2);
        System.out.println("==>矩阵右除");
        m1.arrayRightDivide(m2).print(3, 2);
    }

    /**
     * 矩阵求逆矩阵
     */
    @Test
    public void inverse() {
        double[][] a1 = new double[][]{{-1, 1, 0}, {-4, 3, 0}, {1, 0, 2}};
        Matrix m1 = new Matrix(a1);
        m1.inverse().print(4, 3);
        m1.inverse().times(m1).print(4, 3);
    }

    /**
     * 求矩阵的转置
     */
    @Test
    public void transpose() {
        double[][] a1 = new double[][]{{-1, 1, 0}, {-4, 3, 0}, {1, 0, 2}};
        Matrix m1 = new Matrix(a1);
        m1.transpose().print(4, 2);
    }

    /**
     * 求矩阵的范式
     */
    @Test
    public void cond() {
        double cond = matrix.cond();
        System.out.println(cond);
    }

    //=============矩阵分解================

    /**
     * LU分解
     */
    @Test
    public void LU() {
        double[][] a1 = new double[][]{{2, 1}, {8, 7}};
        double[][] a2 = new double[][]{{2, 1}, {0, 3}};
        Matrix m1 = new Matrix(a2);
        LUDecomposition lu = m1.lu();
//        lu.solve(new Matrix(a1)).print(4, 1);
        System.out.println(lu);
    }

    /**
     * 数学量
     */
    @Test
    public void mathAttrs() {
        double[][] a1 = new double[][]{{2, 1}, {8, 7}};
        Matrix m1 = new Matrix(a1);
        System.out.println("===>Chol: " + m1.chol().toString());
        System.out.println("===>Det: " + m1.det());
        System.out.println("===>Rank: " + m1.rank());
    }
}
