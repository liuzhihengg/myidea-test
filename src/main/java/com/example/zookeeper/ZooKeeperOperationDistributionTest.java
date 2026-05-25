package com.example.zookeeper;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ZooKeeperOperationDistributionTest {
    private static final String ZK_ADDRESS = "localhost:2181,localhost:2182,localhost:2183";
    private static final int TOTAL_OPERATIONS = 10000;
    private static final Random random = new Random();

    public static void main(String[] args) throws Exception {
        CuratorFramework client = CuratorFrameworkFactory.newClient(
                ZK_ADDRESS, new ExponentialBackoffRetry(1000, 3));
        client.start();

        try {
            // 收集所有第三层及以下的节点
            List<String> allNodes = new ArrayList<>();
            collectNodes(client, "/testRoot", allNodes, 0);

            long startTime = System.currentTimeMillis();
            for (int i = 0; i < TOTAL_OPERATIONS; i++) {
                int operationChoice = random.nextInt(100);
                String selectedNode = allNodes.get(random.nextInt(allNodes.size()));

                if (operationChoice < 50) { // 50% Create
                    // 创建新节点
                    String newNodePath = selectedNode + "/newNode" + System.currentTimeMillis();
                    client.create().creatingParentsIfNeeded().forPath(newNodePath, "newData".getBytes());
                } else if (operationChoice < 90) { // 40% Update
                    // 更新节点数据
                    client.setData().forPath(selectedNode, ("updatedData" + System.currentTimeMillis()).getBytes());
                } else { // 10% Delete
                    // 删除节点，这里小心处理以避免删除有子节点的节点
                    try {
                        client.delete().forPath(selectedNode);
                        allNodes.remove(selectedNode); // 从列表中移除已删除的节点
                    } catch (Exception e) {
                        // 可能因为节点有子节点而无法删除，忽略这种情况
                    }
                }
            }
            long endTime = System.currentTimeMillis();
            System.out.println("Completed " + TOTAL_OPERATIONS + " operations in " + (endTime - startTime) + " ms");
        } finally {
            client.close();
        }
    }

    private static void collectNodes(CuratorFramework client, String path, List<String> nodes, int currentDepth) throws Exception {
        if (currentDepth >= 2) { // 第三层及以下
            nodes.add(path);
        }
        if (currentDepth < 5) { // 防止递归太深
            List<String> children = client.getChildren().forPath(path);
            for (String child : children) {
                collectNodes(client, path + "/" + child, nodes, currentDepth + 1);
            }
        }
    }
}

