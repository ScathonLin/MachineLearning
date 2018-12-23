package com.huawei.scathon.aiops.louvain;

import java.util.function.BinaryOperator;

/**
 * @author l00483433
 */
class FunctionsHolder {
    static BinaryOperator<Integer> addTwoInt = (v1, v2) -> v1 + v2;
    static BinaryOperator<Double> addTwoDouble = (v1, v2) -> v1 + v2;
}
