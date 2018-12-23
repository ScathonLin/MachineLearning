package com.huawei.scathon.aiops.louvain;

import com.huawei.scathon.aiops.louvain.entity.Community;
import com.huawei.scathon.aiops.louvain.entity.Node;
import com.huawei.scathon.aiops.louvain.entity.Edge;
import com.huawei.scathon.aiops.utils.AppPropertiesHolder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Louvain社区发现算法Java实现.
 *
 * @author l00483433 linhuadong
 */
public class LouvainCommunityDiscovery {
    private static final Map<Integer, Node> NODES_MAP = new HashMap<>(1 << 4);
    private static final Map<Community, Set<Integer>> COMMUNITY_MAP = new HashMap<>();
    private static final List<Integer> COMMUNITIES = new ArrayList<>();
    private static final Map<Integer, Community> COMMUNITY_POOL = new HashMap<>();
    private static int COUNTER = 0;
    private static Integer ALL_EDGE_WEIGHT_SUM = 0;

    public static void main(String[] args) {
        List<Integer> numList = new ArrayList<>();
        System.out.println(numList);
        loadData();
        kernel();
    }

    private static void kernel() {
        buildCommunities();
        double currentMapModularity = getMapModularity();
        double iterMapModularity;
        Map<Community, Set<Integer>> communityRelation = buildCommunityRelation(NODES_MAP);
        List<Integer> communities = buildCommunities();
        List<Set<Integer>> firstPhaseResult = firstPhase(communities, communityRelation, NODES_MAP);
        System.out.println(communities);
        Map<Integer, List<Integer>> resultMap = new HashMap<>(1 << 6);
        for (int i = 0; i < communities.size(); i++) {
            resultMap.computeIfAbsent(communities.get(i), k -> new ArrayList<>());
            resultMap.get(communities.get(i)).add(i);
        }
        System.out.println(communityRelation);
        System.out.println(resultMap);
    }

    private static List<Set<Integer>> firstPhase(List<Integer> communities,
        Map<Community, Set<Integer>> communityRelation, Map<Integer, Node> nodesMap) {
        boolean flag = true;
        while (flag) {
            int count = 0;
            //TODO 将顺序打散，进行随机
            List<Integer> nodeIdList = new ArrayList<>(nodesMap.keySet());
            Collections.shuffle(nodeIdList);
            for (Integer nodeId : nodeIdList) {
                Node nodeChoiced = nodesMap.get(nodeId);
                Set<Integer> neighborsSet = new HashSet<>();
                nodeChoiced.getEdges().forEach(edge -> {
                    int to = edge.getTo();
                    int neighborComId = communities.get(to);
                    if (neighborComId != communities.get(nodeId)) {
                        neighborsSet.add(neighborComId);
                    }
                });
                // 计算模块度增量.
                String complexMaxDeltaQ = neighborsSet.stream()
                    .map(neighborBelongComId -> {
                        double deltaQ = calculateDeltaQ(nodeChoiced, neighborBelongComId, communityRelation, nodesMap);
                        return String.valueOf(deltaQ) + "," + neighborBelongComId;
                    })
                    .filter(complexDeltaQ -> Double.parseDouble(complexDeltaQ.split(",")[0]) > 0)
                    .max(Comparator.comparing(complexDeltaQ -> complexDeltaQ.split(",")[0]))
                    .orElse(null);
                if (complexMaxDeltaQ != null) {
                    // 社区移动
                    int targetComId = Integer.parseInt(complexMaxDeltaQ.split(",")[1]);
                    Set<Integer> targetComNodes = communityRelation.get(Community.builder().comId(targetComId).build());
                    targetComNodes.add(nodeId);
                    int selfComId = communities.get(nodeId);
                    communityRelation.get(Community.builder().comId(selfComId).build()).remove(nodeId);
                    // updateTargetCom(nodeId, targetComId, nodesMap, communityRelation, communities);
                    // updateBeMergeCom(nodesMap.get(nodeId), communityRelation, communities);
                    communities.set(nodeId, targetComId);
                    count++;
                }
            }
            if (count == 0) {
                flag = false;
            }
        }
        return null;
    }

    private static double calculateDeltaQ(Node curNode, Integer neighborBelongComId,
        Map<Community, Set<Integer>> communityRelation, Map<Integer, Node> nodesMap) {
        Community targetCom = communityRelation.entrySet()
            .stream()
            .filter(entry -> entry.getKey().getComId() == neighborBelongComId)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList())
            .get(0);
        int kiComIn = calculateKiCommaIn(curNode, targetCom, nodesMap, communityRelation);
        int sigmaTot = calculateSigmaTot(targetCom, nodesMap, communityRelation);
        // int sigmaTot = targetCom.getSigmaTot();
        int ki = curNode.getKi();
        return kiComIn - sigmaTot * ki * 1.0 / getAllEdgeWeightSum();
    }

    private static int calculateSigmaTot(Community targetCom, Map<Integer, Node> nodesMap,
        Map<Community, Set<Integer>> communityRelation) {
        Set<Integer> neighborSubNodeIds = communityRelation.get(targetCom);
        Set<Node> neighborNodes = neighborSubNodeIds.stream().map(nodesMap::get).collect(Collectors.toSet());
        return neighborNodes.stream()
            .map(node -> node.getEdges()
                .stream()
                .filter(edge -> !neighborNodes.contains(new Node(edge.getTo())))
                .map(Edge::getWeight)
                .reduce(FunctionsHolder.addTwoInt)
                .orElse(0))
            .reduce(FunctionsHolder.addTwoInt)
            .orElse(0);
    }

    /**
     * 计算即将被合入的节点入射到目标社区的边的权重.
     *
     * @param nodeToMerge       被合入的节点.
     * @param targetCommunity   目标邻居社区.
     * @param nodesMap          节点map.
     * @param communityRelation 社区关系数据集.
     * @return Ki, in.
     */
    private static int calculateKiCommaIn(Node nodeToMerge, Community targetCommunity, Map<Integer, Node> nodesMap,
        Map<Community, Set<Integer>> communityRelation) {
        Set<Integer> neighborSubNodeIds = communityRelation.get(targetCommunity);
        Set<Node> neighborNodes = neighborSubNodeIds.stream().map(nodesMap::get).collect(Collectors.toSet());
        return nodeToMerge.getEdges()
            .stream()
            .filter(edge -> neighborNodes.contains(new Node(edge.getTo())))
            .map(Edge::getWeight)
            .reduce(FunctionsHolder.addTwoInt)
            .orElse(0);
    }

    private static void updateBeMergeCom(Node node, Map<Community, Set<Integer>> communityRelation,
        List<Integer> communities) {
        int nodeId = node.getNodeIndex();
        Community selfCom = getCommunityByNodeId(nodeId, communities, communityRelation);
        int sigmaIn = selfCom.getSigmaIn() - 2 - node.getSigmaIn();
        System.out.println(sigmaIn);
        int sigmaTot = selfCom.getSigmaTot() + 2 - node.getSigmaTot();
        System.out.println(sigmaTot);
        selfCom.setSigmaIn(sigmaIn >= 0 ? sigmaIn : 0)
            .setSigmaTot(sigmaTot >= 0 ? sigmaTot : 0)
            .setKi(selfCom.getSigmaIn() + selfCom.getSigmaTot());
        System.out.println(selfCom.getKi());
        System.out.println("============");
    }

    /**
     * 更新社区代表节点的权值信息，用该点代表整个社区的边的权值分布.
     *
     * @param nodeToMerge       将被合并到目标社区的节点id.
     * @param neighborNodeId    社区核心节点id.
     * @param nodesMap          节点映射集合.
     * @param communityRelation
     */
    private static void updateTargetCom(int nodeToMerge, int neighborNodeId, Map<Integer, Node> nodesMap,
        Map<Community, Set<Integer>> communityRelation, List<Integer> communities) {
        Node mergedNode = nodesMap.get(nodeToMerge);
        Community targetCom = getCommunityByNodeId(neighborNodeId, communities, communityRelation);
        targetCom.setSigmaIn(targetCom.getSigmaIn() + mergedNode.getSigmaIn() + 2)
            .setSigmaTot(targetCom.getSigmaTot() + mergedNode.getSigmaTot() - 2)
            .setKi(targetCom.getKi() + mergedNode.getKi());
    }

    private static Community getCommunityByNodeId(int nodeId, List<Integer> communities,
        Map<Community, Set<Integer>> communityRelation) {
        List<Community> communityList = communityRelation.entrySet()
            .stream()
            .filter(entry -> entry.getKey().getComId() == communities.get(nodeId))
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
        return communities.size() != 0 ? communityList.get(0) : null;
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
     * 构建一个存储节点所在社区代号的列表.
     */
    private static List<Integer> buildCommunities() {
        List<Integer> communities = new ArrayList<>();
        NODES_MAP.forEach((key, value) -> communities.add(key, key));
        return communities;
    }

    private static Map<Community, Set<Integer>> buildCommunityRelation(Map<Integer, Node> nodeMap) {
        Map<Community, Set<Integer>> communityRelation = new HashMap<>();
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

