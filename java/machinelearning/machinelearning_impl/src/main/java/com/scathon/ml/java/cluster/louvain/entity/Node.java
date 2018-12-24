package com.scathon.ml.java.cluster.louvain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.Set;

/**
 * @author l00483433
 */
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@ToString
@Builder
public class Node {
    @Getter @Setter private int nodeIndex;
    // 社区环的权重，初始化的时候为0
    @Getter @Setter private int ringWeight;
    @Getter @Setter private Set<Edge> edges;

    @Getter @Setter private Boolean isRemoved;
    @Getter @Setter private int sigmaIn;
    @Getter @Setter private int sigmaTot;
    @Getter @Setter private int ki;

    public Node(int nodeIndex) {
        this.nodeIndex = nodeIndex;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Node) {
            Node target = (Node) obj;
            return target.getNodeIndex() == nodeIndex;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return String.valueOf(nodeIndex + "*#").hashCode();
    }
}
