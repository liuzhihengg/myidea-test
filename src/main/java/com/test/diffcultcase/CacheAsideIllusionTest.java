package com.test.diffcultcase;

import org.junit.jupiter.api.Test;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CacheAsideIllusionTest {

    // 极其简陋的模拟数据库，初始值为 0
    private final AtomicInteger database = new AtomicInteger(0);
    // 极其简陋的模拟缓存，null 代表缓存未命中
    private final AtomicReference<Integer> cache = new AtomicReference<>(null);

    // 极其极其经典的 Cache-Aside 读请求逻辑
    public Integer readData() {
        Integer val = cache.get();
        if (val == null) {
            // 极其致命的漏洞潜伏区：查数据库 -> 写入缓存
            val = database.get();
            // 现实中这里可能有微秒级的网络延迟，但单测里它极其极速！
            cache.set(val);
        }
        return val;
    }

    // 极其极其经典的 Cache-Aside 写请求逻辑
    public void writeData(int newValue) {
        database.set(newValue);
        // 写完数据库，极其守规矩地删除缓存
        cache.set(null);
    }

    @Test
    public void testCacheConsistencyUnderHighConcurrency() throws InterruptedException {
        int threads = 10000; // 极其狂妄地开启 1000 个线程狂轰滥炸
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(1); // 发令枪
        CountDownLatch endLatch = new CountDownLatch(threads);

        for (int i = 0; i < threads; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    latch.await(); // 极其极其公平地在起跑线等待
                    if (threadId % 2 == 0) {
                        // 偶数线程疯狂执行极其极其密集的读操作
                        readData();
                    } else {
                        // 奇数线程极其极其暴力地修改数据库
                        writeData(threadId);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    endLatch.countDown();
                }
            });
        }

        // 极其极其响亮地扣动发令枪！并发宇宙大爆炸！
        latch.countDown();
        endLatch.await(); // 等待所有极其疯狂的线程执行完毕
        executor.shutdown();

        // 极其极其天真的最终断言 (Safety Assertion)
        Integer finalCache = cache.get();
        Integer finalDb = database.get();

        System.out.println("最终数据库值: " + finalDb);
        System.out.println("最终缓存值: " + finalCache);

        // 如果缓存不为空，它必须绝对等于数据库的值！
        if (finalCache != null) {
            assertTrue(finalCache.equals(finalDb),
                    "极其恐怖的灾难！缓存 (" + finalCache + ") 和数据库 (" + finalDb + ") 不一致！");
        }
        System.out.println("【单测宣判】: 极其完美！系统绝对安全！");
    }
}
