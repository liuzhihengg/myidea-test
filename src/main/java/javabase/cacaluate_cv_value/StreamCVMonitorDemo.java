package javabase.cacaluate_cv_value;

import lombok.Data;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.LongAdder;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * O(1) 流式变异系数(CV)监控 Runnable Demo
 * 核心思想：基于全局单调累加器与快照差值，彻底消灭数组遍历与数据淘汰过程。
 */
public class StreamCVMonitorDemo {

    public static void main(String[] args) throws InterruptedException {
        System.out.println(">>> 启动 O(1) 流式排队分布监控系统演示 <<<\n");

        // 1. 初始化核心组件：队列与全局监控器
        // 模拟你的本地 Queue A 或 Queue B
        BlockingQueue<MessageWrapper> queue = new LinkedBlockingQueue<>(100000);
        // 全局 O(1) 流式监控器
        O1StreamMonitor monitor = new O1StreamMonitor();

        // 用于控制测试结束的标志位
        AtomicBoolean isRunning = new AtomicBoolean(true);

        // 2. 启动消费者线程（模拟解析层或 ES 写入层）
        // 假设我们有 2 个工作线程在拼命消费队列
        Thread consumerThread1 = new Thread(new ConsumerTask(queue, monitor, isRunning, "Worker-1"));
        Thread consumerThread2 = new Thread(new ConsumerTask(queue, monitor, isRunning, "Worker-2"));
        consumerThread1.start();
        consumerThread2.start();

        // 3. 启动生产者（模拟 Canal 接收后打散塞入队列）
        System.out.println("--- 阶段一：模拟平滑大流量冲击（纯运力不足场景）---");
        // 瞬间塞入 100 个元素，模拟 Canal 批量拉取
        for (int i = 0; i < 100; i++) {
            // 每一个元素进入队列前，携带当前的全局快照！
            MessageWrapper msg = new MessageWrapper("Normal-SQL-" + i, monitor.takeSnapshot());
            queue.put(msg);
        }

        // 等待一段时间，让消费者处理完这一批
        Thread.sleep(3000);

        System.out.println("\n--- 阶段二：模拟长尾突刺冲击（混入毒药消息场景）---");


        // 紧接着，塞入 50 个正常的无辜消息，它们将被迫排在毒药消息后面
        for (int i = 0; i < 500; i++) {
            MessageWrapper msg = new MessageWrapper("Victim-SQL-" + i, monitor.takeSnapshot());
            queue.put(msg);
            if (i % 10 == 0) {
                // 先塞入一个“毒药消息”（极其复杂的正则或巨大的 JSON）
                MessageWrapper poisonMsg = new MessageWrapper("POISON_PILL_SQL", monitor.takeSnapshot());
                poisonMsg.isPoison = true; // 打个标记，消费者遇到它会卡住很久
                if (i % 50 == 0) {
                    poisonMsg.superPoision = true; // 打个标记，消费者遇到它会卡住很久
                }
                queue.put(poisonMsg);
            }
        }

        // 等待消费者处理完毕
        Thread.sleep(4000);

        // 4. 优雅关闭测试
        isRunning.set(false);
        consumerThread1.join();
        consumerThread2.join();
        System.out.println("\n>>> 演示结束，监控系统成功捕获全部场景特征 <<<");
    }

    // ==========================================
    // 组件一：消息包装器 (携带基因：入队快照)
    // ==========================================
    @Data
    static class MessageWrapper {
        public String payload;
        public Snapshot enqueueSnapshot; // 极其重要：记录入队时的系统总状态
        public boolean isPoison = false; // 仅用于 Demo 模拟毒药节点
        public boolean superPoision = false; // 超级毒药

        public MessageWrapper(String payload, Snapshot snapshot) {
            this.payload = payload;
            this.enqueueSnapshot = snapshot;
        }
    }

    // ==========================================
    // 组件二：消费者任务 (模拟执行解析或写入 ES)
    // ==========================================
    static class ConsumerTask implements Runnable {
        private final BlockingQueue<MessageWrapper> queue;
        private final O1StreamMonitor monitor;
        private final AtomicBoolean isRunning;
        private final String workerName;

        public ConsumerTask(BlockingQueue<MessageWrapper> queue, O1StreamMonitor monitor, AtomicBoolean isRunning, String workerName) {
            this.queue = queue;
            this.monitor = monitor;
            this.isRunning = isRunning;
            this.workerName = workerName;
        }

        @Override
        public void run() {
            try {
                while (isRunning.get() || !queue.isEmpty()) {
                    // poll 带超时，方便退出循环
                    MessageWrapper msg = queue.poll(100, java.util.concurrent.TimeUnit.MILLISECONDS);
                    if (msg == null) continue;

                    // 【核心环节 1：出队瞬间，结算排队期间的 CV 特征！】
                    long waitTimeMs = System.currentTimeMillis() - msg.enqueueSnapshot.enqueueTimeMs;

                    // 只有当排队时间超过一定阈值（比如这里设为 50ms），我们才有价值去分析为什么堵了
                    if (waitTimeMs > 50) {
                        DiagnosisReport report = monitor.analyzeWaitPeriod(msg.enqueueSnapshot);

                        // 基于 CV 值进行智能诊断和日志打印
                        printDiagnosticLog(msg.payload, waitTimeMs, report);
                    }

                    // 【模拟真正的业务处理耗时】
                    long startProcess = System.currentTimeMillis();
                    if (msg.isSuperPoision()) {
                        // 遇到毒药消息，线程被死死卡住 5000 毫秒
                        Thread.sleep(10000);
                    } else if (msg.isPoison) {
                        // 遇到毒药消息，线程被死死卡住 500 毫秒
                        Thread.sleep(100);
                    } else {
                        // 正常消息，处理极快，只需 10 到 20 毫秒
                        Thread.sleep(1 + (long)(Math.random() * 1));
                    }
                    long actualProcessTime = System.currentTimeMillis() - startProcess;

                    // 【核心环节 2：处理完毕，将自己的耗时载入全局计分板！】
                    monitor.recordExecution(actualProcessTime);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        private void printDiagnosticLog(String payload, long waitTime, DiagnosisReport report) {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("[%s] 警报! 消息 [%s] 遭遇严重堆积:\n", workerName, payload));
            sb.append(String.format("  -> 自身排队耗时: %d 毫秒\n", waitTime));
            sb.append(String.format("  -> 在此期间系统处理了: %d 个元素\n", report.processedElementsCount));
            sb.append(String.format("  -> 它们平均执行耗时: %.2f 毫秒\n", report.meanTimeMs));
            sb.append(String.format("  -> 【平整度 CV 值】: %.4f\n", report.cv));

            if (report.processedElementsCount == 0) {
                sb.append("  => [根因分析]: 期间无任何元素被处理，极大概率 Worker 已被死锁！\n");
            } else if (report.cv < 0.3) {
                sb.append("  => [根因分析]: CV 值极低。并发量过大导致算力不足，无异常慢 SQL。\n");
            } else if (report.cv > 1.0) {
                sb.append(String.format("  => [根因分析]: 抓到长尾元凶！根据方差逆向数学推演，\n"));
                sb.append(String.format("  => 【高能预警】这段时间内，存在单次耗时约高达 >>> %.0f 毫秒 <<< 的超级毒药！\n", report.estimatedMaxPoisonTime));
                sb.append(String.format("     💣 [破坏指数] 这唯一的超级毒药，吃掉了该批次高达 %.2f%% 的总算力！\n", report.top1PoisonTimeRatio * 100));
            } else {
                sb.append("  => [根因分析]: 混合业务正常波动。\n");
            }
            System.out.println(sb.toString());
        }
    }

    // ==========================================
    // 组件三：O(1) 核心算法引擎 (极致性能与数学的结合)
    // ==========================================
    static class O1StreamMonitor {
        // 使用 LongAdder 避免多线程并发更新时的 Cache Line 伪共享风暴
        private final LongAdder globalCount = new LongAdder();
        private final LongAdder globalSumMs = new LongAdder();
        private final LongAdder globalSqSumMs = new LongAdder();

        // 1. 获取全局快照
        public Snapshot takeSnapshot() {
            return new Snapshot(
                    globalCount.sum(),
                    globalSumMs.sum(),
                    globalSqSumMs.sum()
            );
        }

        // 2. 载入执行结果（永不淘汰，单调递增）
        public void recordExecution(long executionTimeMs) {
            globalCount.increment();
            globalSumMs.add(executionTimeMs);
            globalSqSumMs.add(executionTimeMs * executionTimeMs);
        }

//        // 3. O(1) 计算差值与变异系数
//        public DiagnosisReport analyzeWaitPeriod(Snapshot startSnapshot) {
//            long currentCount = globalCount.sum();
//            long currentSum = globalSumMs.sum();
//            long currentSqSum = globalSqSumMs.sum();
//
//            // 完美的隐式“淘汰”：只保留差值部分
//            long deltaN = currentCount - startSnapshot.count;
//            long deltaSum = currentSum - startSnapshot.sum;
//            long deltaSqSum = currentSqSum - startSnapshot.sqSum;
//
//            if (deltaN <= 0) {
//                return new DiagnosisReport(0, 0.0, 0.0, 0);
//            }
//
//            double mean = (double) deltaSum / deltaN;
//
//            // 计算方差，加入防卫式编程，应对浮点数灾难性抵消产生的微小负数
//            double variance = ((double) deltaSqSum / deltaN) - (mean * mean);
//            if (variance < 0) {
//                variance = 0.0;
//            }
//
//            double stdDev = Math.sqrt(variance);
//            double cv = (mean == 0) ? 0 : (stdDev / mean);
//
//            // 【新增神仙算法】：利用标准差和样本量，逆向推演这批数据中耗时最长的那个罪魁祸首！
//            // 当 CV > 1.0 时，这个估算值将极其接近真实的毒药耗时。
//            double estimatedMaxPoisonTime = stdDev * Math.sqrt(deltaN);
//
//            return new DiagnosisReport(deltaN, mean, cv, estimatedMaxPoisonTime);
//        }

        // 3. 升级版 O(1) 计算差值、相对方差(CV^2) 与 毒药推演
        public DiagnosisReport analyzeWaitPeriod(Snapshot startSnapshot) {
            long currentCount = globalCount.sum();
            long currentSum = globalSumMs.sum();
            long currentSqSum = globalSqSumMs.sum();

            long deltaN = currentCount - startSnapshot.count;
            long deltaSum = currentSum - startSnapshot.sum;
            long deltaSqSum = currentSqSum - startSnapshot.sqSum;

            if (deltaN <= 0) {
                return new DiagnosisReport(0, 0.0, 0.0, 0.0, 0);
            }

            double mean = (double) deltaSum / deltaN;

            // 计算方差
            double variance = ((double) deltaSqSum / deltaN) - (mean * mean);
            if (variance < 0) {
                variance = 0.0;
            }

            // 【性能极致优化】：完全抛弃开根号求标准差的步骤，直接求 CV 的平方！
            double cvSquared = (mean == 0) ? 0 : (variance / (mean * mean));

            // 【神仙公式保留】：估算单体最大耗时 E_max = sigma * sqrt(N)
            // 数学推导变形：由于 sigma = sqrt(variance)，则 E_max = sqrt(variance * N)
            // 这样我们依然只需要执行一次极其关键的 sqrt 运算。
            double estimatedMaxPoisonTime = Math.sqrt(variance * deltaN);

            // 3. 【全新引入】首席毒药时间占比百分比 P_max
            // 公式：sqrt(CV^2 / N)
            double top1PoisonTimeRatio = Math.sqrt(cvSquared / deltaN);

            return new DiagnosisReport(deltaN, mean, cvSquared, estimatedMaxPoisonTime, top1PoisonTimeRatio);
        }
    }

    // 数据结构：快照
    static class Snapshot {
        public final long count;
        public final long sum;
        public final long sqSum;
        public final long enqueueTimeMs;

        public Snapshot(long count, long sum, long sqSum) {
            this.count = count;
            this.sum = sum;
            this.sqSum = sqSum;
            this.enqueueTimeMs = System.currentTimeMillis();
        }
    }

    // 数据结构：诊断报告
    static class DiagnosisReport {
        public final long processedElementsCount;
        public final double meanTimeMs;
        public final double cv;
        public final double estimatedMaxPoisonTime; // 新增：单体毒药最大耗时还原
        public final double top1PoisonTimeRatio; // 新增：首席毒药吃掉的耗时比例 (0.0 ~ 1.0)

        public DiagnosisReport(long count, double mean, double cv, double estimatedMaxPoisonTime, double ratio) {
            this.processedElementsCount = count;
            this.meanTimeMs = mean;
            this.cv = cv;
            this.estimatedMaxPoisonTime = estimatedMaxPoisonTime;
            this.top1PoisonTimeRatio = ratio;
        }
    }
}
