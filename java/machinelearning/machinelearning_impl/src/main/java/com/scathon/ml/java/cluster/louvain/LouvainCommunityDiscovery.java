package com.scathon.ml.java.cluster.louvain;

import com.scathon.ml.java.cluster.louvain.config.AppPropertiesHolder;
import com.scathon.ml.java.cluster.louvain.entity.Community;
import com.scathon.ml.java.cluster.louvain.entity.Edge;
import com.scathon.ml.java.cluster.louvain.entity.Node;
import com.scathon.ml.java.common.FunctionsHolder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Louvain社区发现算法Java实现.
 *
 * @author l00483433 linhuadong
 */
public class LouvainCommunityDiscovery {
    private static double ALL_EDGE_WEIGHT_SUM = 0;

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        kernel();
        System.out.println(System.currentTimeMillis() - start);
    }

    /**
     * Louvain算法核心方法.
     */
    private static void kernel() {
        Map<Integer, Node> nodeMap = loadData();
        Map<Integer, Node> tmpNodeMap = nodeMap;
        Map<Integer, Set<Integer>> resultMap = new HashMap<>(1 << 6);
        ALL_EDGE_WEIGHT_SUM = getAllEdgeWeightSum(tmpNodeMap);
        double lastCycleModularity = Double.MIN_VALUE;
        Map<Integer, Set<Integer>> finalResultMap = new HashMap<>();
        Map<Integer, Set<Integer>> tmpResultMap = new HashMap<>();
        while (true) {
            Map<Community, Set<Integer>> communityDistribution = buildCommunityDistribution(tmpNodeMap);
            List<Integer> communities = buildCommunities(tmpNodeMap);
            firstPhase(communities, communityDistribution, tmpNodeMap);
            System.out.println(communities);
            Map<Integer, List<Integer>> tempResultMap = new HashMap<>(1 << 6);
            for (int i = 0; i < communities.size(); i++) {
                tempResultMap.computeIfAbsent(communities.get(i), k -> new ArrayList<>());
                tempResultMap.get(communities.get(i)).add(i);
            }
            System.out.println(tempResultMap);
            Map<Integer, Integer> oldComIdAndNewNodeIdMap = new HashMap<>(1 << 6);
            tmpNodeMap = compressMap(communityDistribution, tmpNodeMap, communities,
                    oldComIdAndNewNodeIdMap);
            oldComIdAndNewNodeIdMap.forEach((comId, nId) -> {
                Set<Integer> subNodeIds = communityDistribution.get(Community.builder().comId(comId).build());
                Set<Integer> lastCycleNodeIds = finalResultMap.get(nId);
                Set<Integer> ids = new HashSet<>();
                if (lastCycleNodeIds == null) {
                    ids.add(nId);
                }
            });
            // 更新图中边的总权重.
            ALL_EDGE_WEIGHT_SUM = getAllEdgeWeightSum(tmpNodeMap);
            double mapModularity = getMapModularity(tmpNodeMap);
            if (lastCycleModularity == Double.MIN_VALUE) {
                lastCycleModularity = mapModularity;
                continue;
            }
            System.out.println(tmpNodeMap);
            if (Math.abs(mapModularity - lastCycleModularity) <= 1e-2) {
                break;
            }
            lastCycleModularity = mapModularity;
        }
        System.out.println(resultMap);
    }

    /**
     * Louvain 算法的第一阶段逻辑.
     *
     * @param communities           保存节点所在社区编号的list.
     * @param communityDistribution 节点的社区分布情况.
     * @param nodesMap              节点map.
     */
    private static void firstPhase(List<Integer> communities,
                                   Map<Community, Set<Integer>> communityDistribution, Map<Integer, Node> nodesMap) {
        boolean flag = true;
        while (flag) {
            int count = 0;
            List<Integer> nodeIdList = new ArrayList<>(nodesMap.keySet());
            Collections.shuffle(nodeIdList);
            for (Integer nodeId : nodeIdList) {
                Node nodeChoiced = nodesMap.get(nodeId);
                Set<Integer> neighborsSet = new HashSet<>();
                nodeChoiced.getEdges().forEach(edge -> {
                    int neighborNodeId = edge.getTo();
                    int neighborComId = communities.get(neighborNodeId);
                    if (neighborComId != communities.get(nodeId)) {
                        neighborsSet.add(neighborNodeId);
                    }
                });
                // 计算模块度增量.
                String complexMaxDeltaQ = neighborsSet.stream()
                        .map(neighborNodeId -> {
                            int neighborBelongComId = communities.get(neighborNodeId);
                            double deltaQ = calculateDeltaQ(nodeChoiced, neighborBelongComId, communityDistribution,
                                    nodesMap);
                            return String.valueOf(deltaQ) + "," + neighborNodeId;
                        })
                        .filter(complexDeltaQ -> Double.parseDouble(complexDeltaQ.split(",")[0]) > 0)
                        .max(Comparator.comparing(complexDeltaQ -> complexDeltaQ.split(",")[0]))
                        .orElse(null);
                if (complexMaxDeltaQ != null) {
                    // 社区移动
                    int targetNeighborNodeId = Integer.parseInt(complexMaxDeltaQ.split(",")[1]);
                    int targetComId = communities.get(targetNeighborNodeId);
                    Set<Integer> targetComNodes =
                            communityDistribution.get(Community.builder().comId(targetComId).build());
                    targetComNodes.add(nodeId);
                    int selfComId = communities.get(nodeId);
                    communityDistribution.get(Community.builder().comId(selfComId).build()).remove(nodeId);
                    communities.set(nodeId, targetComId);
                    count++;
                }
            }
            if (count < 4) {
                flag = false;
            }
        }
    }

    /**
     * Louvain算法的第一阶段结束之后，图的压缩过程，将社区中的点合并成一个超节点.
     *
     * @param communityDistribution   节点在社区中的分布情况.
     * @param oldNodeMap              旧的节点集合.
     * @param communities             保存节点在哪个社区的数组.
     * @param oldComIdAndNewNodeIdMap 旧的社区ID和新的超节点之间的映射关系.
     * @return 新的节点集合.
     */
    private static Map<Integer, Node> compressMap(Map<Community, Set<Integer>> communityDistribution,
                                                  Map<Integer, Node> oldNodeMap, List<Integer> communities,
                                                  Map<Integer, Integer> oldComIdAndNewNodeIdMap) {
        Map<Integer, Node> newNodeMap = new HashMap<>(1 << 4);
        generateSigmaInAfterCompress(newNodeMap, communityDistribution, oldNodeMap, oldComIdAndNewNodeIdMap);
        generateEdgeAfterCompress(newNodeMap, communityDistribution, oldNodeMap, oldComIdAndNewNodeIdMap, communities);
        generateKiAfterCompress(newNodeMap);
        return newNodeMap;
    }

    /**
     * 生成压缩之后超节点的ki值.
     *
     * @param newNodeMap 新的节点map.
     */
    private static void generateKiAfterCompress(Map<Integer, Node> newNodeMap) {
        newNodeMap.forEach((nodeId, node) -> {
            Set<Edge> edges = node.getEdges();
            int weightSum = edges.stream().map(Edge::getWeight).reduce(FunctionsHolder.addTwoInt).orElse(0);
            node.setKi(weightSum);
        });
    }

    /**
     * 计算稳定社区内部的边的之间的权重之和.
     *
     * @param newNodeMap            新的节点集合.
     * @param communityDistribution 节点在社区的分布情况.
     * @param oldNodeMap            旧的节点集合.
     * @param oldAndNewComIdMap     旧社区的id与压缩之后的新节点的id之间的映射关系.
     */
    private static void generateSigmaInAfterCompress(Map<Integer, Node> newNodeMap,
                                                     Map<Community, Set<Integer>> communityDistribution,
                                                     Map<Integer, Node> oldNodeMap,
                                                     Map<Integer, Integer> oldAndNewComIdMap) {
        int initNodeId = 0;
        for (Map.Entry<Community, Set<Integer>> entry : communityDistribution.entrySet()) {
            Set<Integer> subNodeIds = entry.getValue();
            if (subNodeIds.size() == 0) {
                continue;
            }
            int newNodeId = initNodeId++;
            Node newNode = Node.builder().nodeIndex(newNodeId).build();
            // 计算社区内部节点之间的边的权重之和，作为超节点的环值.
            int sigmaIn = subNodeIds.stream()
                    .map(oldNodeMap::get)
                    .map(node -> node.getEdges().stream()
                            .filter(edge -> subNodeIds.contains(edge.getTo()))
                            .map(Edge::getWeight)
                            .reduce(FunctionsHolder.addTwoInt)
                            .orElse(0))
                    .reduce(FunctionsHolder.addTwoInt).orElse(0);
            newNode.setSigmaIn(sigmaIn);
            newNodeMap.put(newNodeId, newNode);
            oldAndNewComIdMap.put(entry.getKey().getComId(), newNodeId);
        }
    }

    /**
     * 压缩社区之间的边形成新的边.
     *
     * @param newNodeMap            新的节点集合.
     * @param communityDistribution 节点在社区分布情况.
     * @param oldNodeMap            旧的节点集合.
     * @param oldAndNewComIdMap     旧的社区id和新的节点id之间的映射关系.
     * @param communities           保存节点所在社区的id的数组.
     */
    private static void generateEdgeAfterCompress(Map<Integer, Node> newNodeMap,
                                                  Map<Community, Set<Integer>> communityDistribution,
                                                  Map<Integer, Node> oldNodeMap,
                                                  Map<Integer, Integer> oldAndNewComIdMap,
                                                  List<Integer> communities) {
        for (Map.Entry<Community, Set<Integer>> entry : communityDistribution.entrySet()) {
            Community curCom = entry.getKey();
            Set<Integer> subNodeIds = entry.getValue();
            Set<Integer> neighborNodeComBelongTo = new HashSet<>();
            if (subNodeIds.size() == 0) {
                continue;
            }
            subNodeIds.stream()
                    .map(oldNodeMap::get)
                    .forEach(node -> node.getEdges().forEach(edge -> {
                        int neighborComId = communities.get(edge.getTo());
                        int selfComId = communities.get(node.getNodeIndex());
                        if (neighborComId != selfComId) {
                            neighborNodeComBelongTo.add(communities.get(edge.getTo()));
                        }
                    }));
            int newNodeId = oldAndNewComIdMap.get(curCom.getComId());
            Node newNode = newNodeMap.get(newNodeId);
            if (newNode.getEdges() == null) {
                newNode.setEdges(new HashSet<>());
            }
            neighborNodeComBelongTo.forEach(neighComId -> {
                int newNeighborNodeId = oldAndNewComIdMap.get(neighComId);
                Node neighborNode = newNodeMap.get(newNeighborNodeId);
                if (neighborNode.getEdges() == null) {
                    neighborNode.setEdges(new HashSet<>());
                }
                int newWeight = communityDistribution.get(Community.builder().comId(neighComId).build()).stream()
                        .map(oldNodeMap::get)
                        .map(node -> node.getEdges().stream()
                                .filter(edge1 -> subNodeIds.contains(edge1.getTo()))
                                .map(Edge::getWeight)
                                .reduce(FunctionsHolder.addTwoInt).orElse(0))
                        .reduce(FunctionsHolder.addTwoInt).orElse(0);
                // 一定要设置两条边，因为是无向图.
                newNode.getEdges().add(new Edge().setFrom(newNodeId).setTo(newNeighborNodeId).setWeight(newWeight));
                neighborNode.getEdges().add(new Edge().setFrom(newNeighborNodeId).setTo(newNodeId).setWeight(newWeight));
            });
        }
    }

    /**
     * 计算模块度增量.
     *
     * @param curNode               当前节点.
     * @param neighborBelongComId   邻居节点所在的社区id.
     * @param communityDistribution 社区中点的分布情况.
     * @param nodesMap              节点map.
     * @return 模块度增量.
     */
    private static double calculateDeltaQ(Node curNode, Integer neighborBelongComId,
                                          Map<Community, Set<Integer>> communityDistribution,
                                          Map<Integer, Node> nodesMap) {
        Community targetCom = communityDistribution.entrySet()
                .stream()
                .filter(entry -> entry.getKey().getComId() == neighborBelongComId)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList())
                .get(0);
        int kiComIn = calculateKiCommaIn(curNode, targetCom, nodesMap, communityDistribution);
        int sigmaTot = calculateSigmaTot(targetCom, nodesMap, communityDistribution);
        int ki = curNode.getKi();
        return (1.0 / (2 * ALL_EDGE_WEIGHT_SUM)) * (kiComIn - sigmaTot * ki * 1.0 / ALL_EDGE_WEIGHT_SUM);
    }

    /**
     * 计算∑tot的值.
     *
     * @param targetCom             目标社区
     * @param nodesMap              节点map.
     * @param communityDistribution 社区节点分布情况.
     * @return ∑tot的值.
     */
    private static int calculateSigmaTot(Community targetCom, Map<Integer, Node> nodesMap,
                                         Map<Community, Set<Integer>> communityDistribution) {
        Set<Integer> neighborSubNodeIds = communityDistribution.get(targetCom);
        Set<Node> neighborNodes = neighborSubNodeIds.stream().map(nodesMap::get).collect(Collectors.toSet());
        return neighborNodes.stream()
                .map(node -> node.getEdges()
                        .stream()
                        //.filter(edge -> !neighborNodes.contains(new Node(edge.getTo())))
                        .map(Edge::getWeight)
                        .reduce(FunctionsHolder.addTwoInt)
                        .orElse(0))
                .reduce(FunctionsHolder.addTwoInt)
                .orElse(0);
    }

    /**
     * 计算即将被合入的节点入射到目标社区的边的权重.
     *
     * @param nodeToMerge           被合入的节点.
     * @param targetCommunity       目标邻居社区.
     * @param nodesMap              节点map.
     * @param communityDistribution 社区关系数据集.
     * @return Ki, in.
     */
    private static int calculateKiCommaIn(Node nodeToMerge, Community
            targetCommunity, Map<Integer, Node> nodesMap,
                                          Map<Community, Set<Integer>> communityDistribution) {
        Set<Integer> neighborSubNodeIds = communityDistribution.get(targetCommunity);
        Set<Node> neighborNodes = neighborSubNodeIds.stream().map(nodesMap::get).collect(Collectors.toSet());
        return nodeToMerge.getEdges()
                .stream()
                .filter(edge -> neighborNodes.contains(new Node(edge.getTo())))
                .map(Edge::getWeight)
                .reduce(FunctionsHolder.addTwoInt)
                .orElse(0);
    }

    /**
     * @param nodeMap 节点map.
     * @return 权重值.
     */
    private static double getAllEdgeWeightSum(Map<Integer, Node> nodeMap) {
        return nodeMap.entrySet()
                .stream()
                .map(entry -> entry.getValue().getEdges().stream().map(Edge::getWeight).reduce(FunctionsHolder.addTwoInt).orElse(0))
                .reduce(FunctionsHolder.addTwoInt)
                .orElse(0) / 2.0;
    }

    /**
     * 计算整图的模块度.
     *
     * @return modularity
     */
    private static double getMapModularity(Map<Integer, Node> nodeMap) {
        if (nodeMap.entrySet().size() == 0) {
            throw new IllegalArgumentException("nodeMap is empty");
        }
        double edgeWeightSum = ALL_EDGE_WEIGHT_SUM;
        return nodeMap.entrySet().stream().map(entry -> {
            Node node = entry.getValue();
            return node.getEdges().stream().map(edge -> {
                int from = edge.getFrom();
                int to = edge.getTo();
                return edge.getWeight()
                        - getKi(nodeMap.get(from)) * getKi(nodeMap.get(to)) * 1.0 / (2 * edgeWeightSum);
            }).reduce(FunctionsHolder.addTwoDouble).orElse(0D);
        }).reduce(FunctionsHolder.addTwoDouble).orElse(0D) / (2 * edgeWeightSum);
    }

    /**
     * 计算指向社区节点的边的权重.
     *
     * @param srcNode 社区节点
     * @return ki
     */
    private static int getKi(Node srcNode) {
        return srcNode.getEdges().stream().map(Edge::getWeight).reduce(FunctionsHolder.addTwoInt).orElse(0);
    }

    /**
     * 构建一个存储节点所在社区代号的列表.
     *
     * @param nodeMap 节点map.
     */
    private static List<Integer> buildCommunities(Map<Integer, Node> nodeMap) {
        List<Integer> communities = new ArrayList<>();
        nodeMap.forEach((key, value) -> communities.add(key, key));
        return communities;
    }

    /**
     * 构建初始的时候节点在社区中的分布情况.
     *
     * @param nodeMap 节点map.
     * @return 社区中节点分布情况.
     */
    private static Map<Community, Set<Integer>> buildCommunityDistribution(Map<Integer, Node> nodeMap) {
        Map<Community, Set<Integer>> communityRelation = new HashMap<>(1 << 4);
        nodeMap.forEach((k, v) -> {
            Set<Integer> subNodeIds = new HashSet<>();
            subNodeIds.add(k);
            int edgeCount = v.getEdges().size();
            communityRelation.put(Community.builder().comId(k).ki(edgeCount).sigmaIn(0).sigmaTot(edgeCount).build(),
                    subNodeIds);
        });
        return communityRelation;
    }

    /**
     * 加载测试数据.
     *
     * @return 数据节点map.
     */
    private static Map<Integer, Node> loadData() {
        HashMap<Integer, Node> nodeMap = new HashMap<>(1 << 6);
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
                generateNodeAndEdge(edgeStart, edgeEnd, nodeMap);
                generateNodeAndEdge(edgeEnd, edgeStart, nodeMap);
            });
        } catch (IOException e) {
            System.err.println("加载数据失败！");
        }
        nodeMap.forEach((id, node) -> {
            node.setKi(node.getEdges().size());
            node.setSigmaIn(0);
            node.setSigmaTot(node.getEdges().size());
            node.setRingWeight(0);
        });
        return nodeMap;
    }

    /**
     * 生成节点和边，并将新生成的节点和边注入到map中.
     *
     * @param edgeStart 边的起始id.
     * @param edgeEnd   边的终点id.
     * @param nodeMap   节点map.
     */
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

