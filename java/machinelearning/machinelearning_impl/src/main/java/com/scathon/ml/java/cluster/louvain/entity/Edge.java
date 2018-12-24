package com.scathon.ml.java.cluster.louvain.entity;

import lombok.*;
import lombok.experimental.Accessors;

/**
 * @author l00483433
 */
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@ToString
public class Edge {
    @Getter
    @Setter
    private int from;
    @Getter
    @Setter
    private int to;
    @Getter
    @Setter
    private int weight;

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
}
