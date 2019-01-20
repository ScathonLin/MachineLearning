package com.scathon.ml.java.cluster.dbscan;

import org.apache.commons.collections4.CollectionUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class Test {
    public static void main(String[] args) {
        List<Integer> l1 = Arrays.asList(1, 2, 3, 4);
        List<Integer> l2 = Arrays.asList(1, 3, 4, 5);
        Collection<Integer> result = CollectionUtils.subtract(l2, l1);
        System.out.println(Arrays.toString(result.toArray()));
        //Collection<Integer> intersection = CollectionUtils.intersection(l1, l2);
        //System.out.println(Arrays.toString(intersection.toArray()));
    }
}
