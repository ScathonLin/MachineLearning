package com.huawei.scathon.aiops.louvain;

import com.huawei.scathon.aiops.louvain.entity.Node;
import com.huawei.scathon.aiops.louvain.entity.Edge;
import com.huawei.scathon.aiops.utils.AppPropertiesHolder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Louvain社区发现算法Java实现.
 *
 * @author l00483433 linhuadong
 */
public class LouvainCommunityDiscovery {
    private static final Map<Integer, Node> NODES_MAP = new HashMap<>(1 << 4);
    private static final Map<Node, Set<Node>> COMMUNITY_MAP = new HashMap<>();
    private static final List<Integer> COMMUNITIES = new ArrayList<>();
    private static Integer ALL_EDGE_WEIGHT_SUM = 0;

    public static void main(String[] args) {
        List<Integer> numList = new ArrayList<>();
        numList.add(1);
        numList.add(2);
        numList.add(3);
        numList.add(4);
        Collections.shuffle(numList);
        System.out.println(numList);
        loadData();
        ALL_EDGE_WEIGHT_SUM = getAllEdgeWeightSum();
        System.out.println(ALL_EDGE_WEIGHT_SUM);
        buildCommunityMap();
        System.out.println(getSigmaTot(new Node(4)));
        System.out.println(getMapModularity());
        kernel();
    }

    private static void kernel() {
        buildCommunities();
        double currentMapModularity = getMapModularity();
        double iterMapModularity;
        List<Set<Integer>> communityRelation = new ArrayList<>();
        NODES_MAP.entrySet().stream().forEach(entry -> {
            Set<Integer> subNodes = new HashSet<>();
            subNodes.add(entry.getKey());
            communityRelation.add(entry.getKey(), subNodes);
        });
        List<Set<Integer>> firstPhaseResult = firstPhase(COMMUNITIES, communityRelation, NODES_MAP);
        System.out.println(COMMUNITIES);
        Map<Integer, List<Integer>> resultMap = new HashMap<>();
        for (int i = 0; i < COMMUNITIES.size(); i++) {
            resultMap.computeIfAbsent(COMMUNITIES.get(i), k -> new ArrayList<>());
            resultMap.get(COMMUNITIES.get(i)).add(i);
        }
        System.out.println(resultMap);
    }

    private static List<Set<Integer>> firstPhase(List<Integer> communities, List<Set<Integer>> communityRelation,
        Map<Integer, Node> nodesMap) {
        boolean flag = true;
        while (flag) {
            int count = 0;
            //TODO 将顺序打散，进行随机
            List<Integer> comIdList = new ArrayList<>(nodesMap.keySet());
            Collections.shuffle(comIdList);
            for (Integer comId : comIdList) {
                Node nodeChoiced = nodesMap.get(comId);
                List<Node> subNodes = communityRelation.get(comId)
                    .stream()
                    .map(nodesMap::get)
                    .collect(Collectors.toList());
                Set<Integer> neighborsSet = new HashSet<>();
                subNodes.forEach(node -> node.getEdges().forEach(edge -> {
                    int to = edge.getTo();
                    Integer neighborId = communities.get(to);
                    if (neighborId.intValue() != communities.get(comId).intValue()) {
                        neighborsSet.add(neighborId);
                    }
                }));
                // 计算模块度增量.
                String complexMaxDeltaQ = neighborsSet.stream()
                    .map(nodeId -> {
                        double deltaQ = calculateDeltaQ(nodeChoiced, nodeId, communityRelation, nodesMap);
                        return String.valueOf(deltaQ) + "," + nodeId;
                    })
                    .filter(complexDeltaQ -> Double.parseDouble(complexDeltaQ.split(",")[0]) > 0)
                    .max(Comparator.comparing(complexDeltaQ -> complexDeltaQ.split(",")[0]))
                    .orElse(null);
                if (complexMaxDeltaQ != null) {
                    // 社区移动
                    int targetNeighborId = Integer.parseInt(complexMaxDeltaQ.split(",")[1]);
                    communityRelation.get(comId).forEach(node -> communityRelation.get(targetNeighborId).add(node));
                    communityRelation.get(comId).forEach(nodeId -> communities.set(nodeId, targetNeighborId));
                    communityRelation.get(comId).removeIf(node -> node.intValue() != comId.intValue());
                    // 更新社区核心节点的权值
                    updateCommunityCoreNode(comId, targetNeighborId, nodesMap);
                    resetCommunityCoreNode(nodesMap.get(comId));
                    System.out.println(comId);
                    count++;
                }
            }
            if (count == 0) {
                flag = false;
            }
        }
        return null;
    }

    private static void resetCommunityCoreNode(Node node) {
        Set<Edge> edges = node.getEdges();
        node.setSigmaIn(0).setSigmaTot(edges.size()).setKi(edges.size());
    }

    /**
     * 更新社区代表节点的权值信息，用该点代表整个社区的边的权值分布.
     *
     * @param coreNodeId  社区核心节点id.
     * @param nodeToMerge 将被合并到目标社区的节点id.
     * @param nodesMap    节点映射集合.
     */
    private static void updateCommunityCoreNode(int nodeToMerge, int coreNodeId, Map<Integer, Node> nodesMap) {
        Node coreNode = nodesMap.get(coreNodeId);
        Node mergedNode = nodesMap.get(nodeToMerge);
        coreNode.setSigmaIn(coreNode.getSigmaIn() + mergedNode.getSigmaIn() + 2)
            .setSigmaTot(coreNode.getSigmaTot() + mergedNode.getSigmaTot() - 2)
            .setKi(coreNode.getKi() + mergedNode.getKi());
    }

    private static double calculateDeltaQ(Node curNode, Integer leaderNeighborId, List<Set<Integer>> communityRelation,
        Map<Integer, Node> nodesMap) {
        Node leaderNeighborNode = nodesMap.get(leaderNeighborId);
        int kiComIn = calculateKiCommaIn(curNode.getNodeIndex(), leaderNeighborId, nodesMap, communityRelation);
        int sigmaTot = leaderNeighborNode.getSigmaTot();
        int ki = curNode.getKi();
        return kiComIn - sigmaTot * ki * 1.0 / getAllEdgeWeightSum();
    }

    /**
     * 计算即将被合入的节点入射到目标社区的边的权重.
     *
     * @param nodeToMerge           被合入的节点的id.
     * @param leaderOfNeighborNodes 邻居社区的leader节点.
     * @param nodesMap              节点map.
     * @param communityRelation     社区关系数据集.
     * @return Ki, in.
     */
    private static int calculateKiCommaIn(int nodeToMerge, int leaderOfNeighborNodes, Map<Integer, Node> nodesMap,
        List<Set<Integer>> communityRelation) {
        Set<Integer> mergeNodesIds = communityRelation.get(nodeToMerge);
        Set<Integer> leaderNodesIdsOfNeighbor = communityRelation.get(leaderOfNeighborNodes);
        Set<Node> mergedNodes = mergeNodesIds.stream().map(nodesMap::get).collect(Collectors.toSet());
        Set<Node> neighborNodes = leaderNodesIdsOfNeighbor.stream().map(nodesMap::get).collect(Collectors.toSet());
        return mergedNodes.stream()
            .map(node -> node.getEdges()
                .stream()
                .filter(edge -> neighborNodes.contains(new Node(edge.getTo())))
                .map(Edge::getWeight)
                .reduce(FunctionsHolder.addTwoInt)
                .orElse(0))
            .reduce(FunctionsHolder.addTwoInt)
            .orElse(0);
    }

    private static int getAllEdgeWeightSum() {
        return NODES_MAP.entrySet()
            .stream()
            .map(entry -> entry.getValue().getEdges().size())
            .reduce(FunctionsHolder.addTwoInt)
            .orElse(0);
    }

    /**
     * 计算整图的模块度.
     *
     * @return modularity
     */
    private static double getMapModularity() {
        if (NODES_MAP.entrySet().size() == 0) {
            throw new IllegalArgumentException("NODES_MAP is empty");
        }
        int edgeWeightSum = getAllEdgeWeightSum();
        return NODES_MAP.entrySet().stream().map(entry -> {
            Node node = entry.getValue();
            return node.getEdges().stream().map(edge -> {
                int from = edge.getFrom();
                int to = edge.getTo();
                return edge.getWeight()
                    - getKi(NODES_MAP.get(from)) * getKi(NODES_MAP.get(to)) * 1.0 / 2 * edgeWeightSum;
            }).reduce(FunctionsHolder.addTwoDouble).orElse(0D);
        }).reduce(FunctionsHolder.addTwoDouble).orElse(0D);
    }

    /**
     * 计算指向社区节点的边的权重.
     *
     * @param srcNode 社区节点
     * @return ki
     */
    private static int getKi(Node srcNode) {
        return srcNode.getSigmaIn() + srcNode.getSigmaTot();
    }

    /**
     * @param targetNode 目标社区节点.
     * @return ∑tot
     */
    private static double getSigmaTot(Node targetNode) {
        Set<Node> comNodeSet = COMMUNITY_MAP.keySet();
        return comNodeSet.stream()
            .filter(node -> !node.getIsRemoved() && node.getNodeIndex() != targetNode.getNodeIndex())
            .map(node -> COMMUNITY_MAP.get(node)
                .stream()
                .map(subNode -> getWeightFromNodeToCom(subNode, targetNode))
                .reduce(FunctionsHolder.addTwoInt)
                .orElse(0))
            .reduce(FunctionsHolder.addTwoInt)
            .orElse(0);
    }

    /**
     * 计算当前节点到目标社区的边的权重之和，也就是计算公式中的k_i_in的值
     *
     * @param srcNode    源节点（社区）
     * @param targetNode 目标节点（社区）
     * @return
     */
    private static int getWeightFromNodeToCom(Node srcNode, Node targetNode) {
        Set<Node> memNodeSet = COMMUNITY_MAP.getOrDefault(targetNode, new HashSet<>());
        return srcNode.getEdges()
            .stream()
            .filter(edge -> memNodeSet.contains(new Node(edge.getTo())))
            .map(Edge::getWeight)
            .reduce(FunctionsHolder.addTwoInt)
            .orElse(0);
    }

    private static void buildCommunityMap() {
        NODES_MAP.entrySet().forEach(entry -> {
            HashSet<Node> subNodes = new HashSet<>();
            subNodes.add(entry.getValue());
            COMMUNITY_MAP.put(entry.getValue(), subNodes);
        });
    }

    /**
     * 构建一个存储节点所在社区代号的列表.
     */
    private static void buildCommunities() {
        NODES_MAP.forEach((key, value) -> COMMUNITIES.add(key, key));
    }

    /**
     * 加载测试数据.
     */
    private static void loadData() {
        String dataFilePath = AppPropertiesHolder.LOUVAIN_SAMPLE_DATA_PATH;
        try (InputStream inputStream = LouvainCommunityDiscovery.class.getClassLoader()
            .getResourceAsStream(dataFilePath);
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
            bufferedReader.lines().forEach(line -> {
                String[] items = line.split(AppPropertiesHolder.LOUVAIN_SAMPLE_DATA_LINE_SEPARATOR);
                int edgeStart = Integer.parseInt(items[0]);
                int edgeEnd = Integer.parseInt(items[1]);
                // 无向图，所以一条边要更新两个节点
                generateNodeAndEdge(edgeStart, edgeEnd, NODES_MAP);
                generateNodeAndEdge(edgeEnd, edgeStart, NODES_MAP);
            });
        } catch (IOException e) {
            System.err.println("加载数据失败！");
        }
        NODES_MAP.forEach((id, node) -> {
            node.setKi(node.getEdges().size());
            node.setSigmaIn(0);
            node.setSigmaTot(node.getEdges().size());
            node.setRingWeight(0);
        });
    }

    private static void generateNodeAndEdge(int edgeStart, int edgeEnd, Map<Integer, Node> nodeMap) {
        Node node;
        if ((node = nodeMap.getOrDefault(edgeStart, null)) == null) {
            node = new Node(edgeStart);
            node.setIsRemoved(false);
            Set<Edge> edges = new HashSet<>();
            node.setEdges(edges);
            nodeMap.put(edgeStart, node);
        }
        node.getEdges().add(new Edge().setFrom(edgeStart).setTo(edgeEnd).setWeight(1));
    }
}

