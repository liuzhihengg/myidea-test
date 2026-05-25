package javabase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class ConcurrentAddDemo {

    // 共享的 ArrayList
    private static final List<Integer> sharedList = new ArrayList<>();

    // 线程数量（可根据需要调整）
    private static final int THREAD_COUNT = 10;

    // 每个线程要添加的元素数量
    private static final int ELEMENTS_PER_THREAD = 10_000;

    public static void main(String[] args) throws InterruptedException {
//        commonTest();
        threadPoolTest();
    }

    private static void commonTest() {
        // 线程1：不断分配大块内存，触发 OOM
        Thread memoryEaterThread = new Thread(() -> {
            try {
                List<byte[]> list = new ArrayList<>();
                while (true) {
                    // 这里不断分配大对象，内存耗尽后会抛出 OutOfMemoryError
                    byte[] block = new byte[10_000_000];
                    list.add(block);
                    System.out.println("list add");
                    // 为了更明显地加速 OOM，留一点空隙再继续下次分配
                    Thread.sleep(100);
                }
            } catch (OutOfMemoryError e) {
                System.err.println("===> Thread 1 hit OOM and will exit!");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            System.out.println("===> Thread 1 is done.");
        }, "MemoryEater");

        // 线程2：定期打印
        Thread aliveThread = new Thread(() -> {
            try {
                while (true) {
                    System.out.println("Thread 2 is still running...");
                    Thread.sleep(1000);
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "AliveThread");

        // 启动两个线程
        memoryEaterThread.start();
        aliveThread.start();
    }

    public static void threadPoolTest() {
        // 创建一个拥有2个工作线程的固定大小线程池
        ExecutorService executorService1 = Executors.newFixedThreadPool(1);
        ExecutorService executorService2 = Executors.newFixedThreadPool(1);


        // 提交任务1：不断分配大块内存，触发 OOM
        executorService1.submit(() -> {
            try {
                List<byte[]> list = new ArrayList<>();
                while (true) {
                    // 这里不断分配大对象，内存耗尽后会抛出 OutOfMemoryError
                    byte[] block = new byte[100000000];
                    Arrays.fill(block,(byte) 1);
                    list.add(block);
                    System.out.println("list add");
                    // 为了更明显地加速 OOM，留一点空隙再继续下次分配
                    Thread.sleep(100);
                }
            } catch (OutOfMemoryError oom) {
                System.err.println("===> Task1 OOM, 线程即将退出！");
                oom.printStackTrace();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            System.out.println("===> Task1 执行结束。");
        });

        // 提交任务2：定期打印，证明还在运行
        executorService2.submit(() -> {
            try {
                while (true) {
                    System.out.println("Task2 is still running...");
                    System.out.println("AcCount:" + ((ThreadPoolExecutor)executorService1).getActiveCount());
                    if (((ThreadPoolExecutor)executorService1).getActiveCount() == 0) {
                        executorService1.submit(() -> {
                            try {
                                List<byte[]> list = new ArrayList<>();
                                while (true) {
                                    // 这里不断分配大对象，内存耗尽后会抛出 OutOfMemoryError
                                    byte[] block = new byte[100000000];
                                    Arrays.fill(block,(byte) 1);
                                    list.add(block);
                                    System.out.println("list add");
                                    // 为了更明显地加速 OOM，留一点空隙再继续下次分配
                                    Thread.sleep(100);
                                }
                            } catch (OutOfMemoryError oom) {
                                System.err.println("===> Task1 OOM, 线程即将退出！");
                                oom.printStackTrace();
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                            System.out.println("===> Task1 执行结束。");
                        });
                    }
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            System.out.println("===> Task2 执行结束。");
        });
    }
}
