package com.scathon.ml.java.common;

import java.util.function.BinaryOperator;

/**
 * @author l00483433
 */
public class FunctionsHolder {
    public static BinaryOperator<Integer> addTwoInt = (v1, v2) -> v1 + v2;
    public static BinaryOperator<Double> addTwoDouble = (v1, v2) -> v1 + v2;
}
