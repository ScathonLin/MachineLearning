package com.huawei.scathon.aiops.louvain.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.HashSet;

/**
 * @author l00483433
 */
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@ToString
public class Edge {
    // start point of edge
    @Getter @Setter private int from;
    // to point of edge
    @Getter @Setter private int to;
    // weight of edge
    @Getter @Setter private int weight;

    public Edge(int from, int to) {
        this.from = from;
        this.to = to;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Edge) {
            Edge target = (Edge) obj;
            return target.getFrom() == this.getFrom() && this.getTo() == this.getTo();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return (String.valueOf(from) + String.valueOf(to)).hashCode();
    }

    public static void main(String[] args) {
        HashSet<Edge> edgesSet = new HashSet<>();
        edgesSet.add(new Edge().setFrom(1).setTo(2).setWeight(100));
        edgesSet.add(new Edge().setFrom(1).setTo(2).setWeight(0));
        edgesSet.add(new Edge().setFrom(2).setTo(2));
        System.out.println(edgesSet);
        edgesSet.remove(new Edge().setFrom(1).setTo(2));
        System.out.println(edgesSet);
    }
}
