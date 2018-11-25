package com.scathon.ml.jama;

import Jama.Matrix;
import org.junit.Test;

public class JamaTest {
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
}
