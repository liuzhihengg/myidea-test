package com.multithread;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class ConcurrentHashMapTest {
    private static final int THREADS = 200; // 根据你的系统性能调整线程数
    private static final int OPERATIONS = 100000; // 每种操作的次数

    private static final ConcurrentHashMap<Integer, Integer> map = new ConcurrentHashMap<>();

    public static final Object lock = new Object();

//    private static final Map<Integer,Integer> map = Collections.synchronizedMap(new HashMap<>());

    public static void main(String[] args) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(THREADS);
        CountDownLatch latch = new CountDownLatch(THREADS);
        AtomicInteger keyGenerator = new AtomicInteger();

        long startTime = System.nanoTime();

        // 启动线程执行增删改查操作
        for (int i = 0; i < THREADS; i++) {
            executor.submit(() -> {
                for (int j = 0; j < OPERATIONS / THREADS; j++) {
                    int key = keyGenerator.incrementAndGet();
                    // 增加
                    map.put(key, key);
                    // 删除
                    map.remove(key);
                    // 更新
                    map.replace(key, key + 1);
                    // 查询
                    map.get(key);
                }
                latch.countDown();
            });
        }

        latch.await(); // 等待所有线程执行完成
        executor.shutdown();
        long endTime = System.nanoTime();

        long duration = endTime - startTime;
        double seconds = duration / 1_000_000_000.0;
        System.out.println("Total time: " + seconds + " seconds");

        double qps = (4.0 * OPERATIONS) / seconds; // 四种操作各一次，总共执行了4 * OPERATIONS次操作
        System.out.println("QPS: " + qps);
    }
}

