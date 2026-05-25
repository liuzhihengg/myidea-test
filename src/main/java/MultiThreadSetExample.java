import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class MultiThreadSetExample {

    // 通过volatile确保写线程更新后的sharedSet可以被所有读线程看到
    private static Set<Integer> sharedSet = new HashSet<>();

    public static void main(String[] args) throws InterruptedException {
        // 初始化Set
        for (int i = 0; i < 100; i++) {
            sharedSet.add(i);
        }

        // 创建读线程池和写线程池
        ExecutorService readExecutor = Executors.newFixedThreadPool(20);
        ExecutorService writeExecutor = Executors.newFixedThreadPool(20);

        // 启动读线程
        for (int i = 0; i < 20; i++) {
            readExecutor.submit(() -> {
                while (true) {
//                    Set<Integer> localSet = sharedSet;
                    boolean found = sharedSet.stream().anyMatch(value -> value % 2 == 0); // 示例条件
//                    System.out.println("Any match found: " + found);
                    try {
                        TimeUnit.MILLISECONDS.sleep(100); // 模拟读取延迟
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            });
        }

        // 启动写线程
        for (int i = 0; i < 20; i++) {
            writeExecutor.submit(() -> {
                while (true) {
                    int newSize = ThreadLocalRandom.current().nextInt(100, 1000);
                    Set<Integer> newSet = new HashSet<>(newSize);

                    for (int j = 0; j < newSize; j++) {
                        newSet.add(ThreadLocalRandom.current().nextInt(0, 2000));
                    }

                    // 使用volatile字段更新共享Set
                    sharedSet = newSet;

                    System.out.println("Set updated with new size: " + newSet.size());
                    try {
                        TimeUnit.MILLISECONDS.sleep(200); // 模拟写入延迟
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            });
        }

        // 让线程运行一段时间
        TimeUnit.SECONDS.sleep(10);

        // 关闭线程池
        readExecutor.shutdownNow();
        writeExecutor.shutdownNow();

        readExecutor.awaitTermination(5, TimeUnit.SECONDS);
        writeExecutor.awaitTermination(5, TimeUnit.SECONDS);

        System.out.println("All threads have finished.");
    }
}
