package com.example.zookeeper;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

public class ZooKeeperDataPreparation {
    private static final String ZK_ADDRESS = "localhost:2181,localhost:2182,localhost:2183"; // ZooKeeper服务器地址
    private static final int TOTAL_NODES = 10000; // 总共要创建的节点数量

    public static void main(String[] args) {
        // 创建一个客户端实例
        CuratorFramework client = CuratorFrameworkFactory.newClient(
                ZK_ADDRESS,
                new ExponentialBackoffRetry(1000, 3)
        );
        client.start();

        try {
            // 创建多层次的持久节点
            createPersistentNodes(client, "/testRoot", 4, 5);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 关闭客户端
            client.close();
        }
    }

    private static void createPersistentNodes(CuratorFramework client, String path, int depth, int width) throws Exception {
        if (depth == 0) {
            return;
        }

        for (int i = 0; i < width; i++) {
            String currentPath = path + "/node" + i;
            // 创建当前节点
            if (client.checkExists().forPath(currentPath) == null) {
                client.create().creatingParentsIfNeeded().forPath(currentPath);
                System.out.println("Created node: " + currentPath);
            }

            // 递归创建子节点
            createPersistentNodes(client, currentPath, depth - 1, width);
        }
    }
}

