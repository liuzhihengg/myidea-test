package javabase.allocprofiler;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class AllocDemo {

    // 防止 JIT 过度优化导致“看起来没分配”
    private static volatile Object blackhole;

    public static void main(String[] args) throws Exception {
        System.out.println("PID = " + ProcessHandle.current().pid());

        // 先 warmup 2 秒，让 JIT 稍微稳定点（图会更干净）
        long warmupEnd = System.currentTimeMillis() + 2000;
        while (System.currentTimeMillis() < warmupEnd) {
            blackhole = allocPathA(2_000);
            blackhole = allocPathB(2_000);
        }

        System.out.println("Warmup done. Now allocating for ~15s...");

        long end = System.currentTimeMillis() + 15_000;
        long n = 0;
        while (System.currentTimeMillis() < end) {
            // 两条不同的分配路径，火焰图会出现两个“山头”
            blackhole = allocPathA(30_000);
            blackhole = allocPathB(30_000);

            // 偶尔让 GC 有机会跑一下（可选）
            if ((++n % 30) == 0) Thread.sleep(5);
        }

        System.out.println("Done.");
        // 稍微停一下，方便你在 IDE 里点 Stop / 看报告
        Thread.sleep(3000);
    }

    /** 典型“小对象/字符串/集合”分配 */
    static Object allocPathA(int count) {
        List<String> list = new ArrayList<>(count);
        ThreadLocalRandom r = ThreadLocalRandom.current();

        for (int i = 0; i < count; i++) {
            // char[] + String + 可能的编码转换
            String s = "user=" + r.nextInt() + ",ts=" + System.nanoTime();
            list.add(s);

            // 每隔一段再搞点 HashMap Entry 分配
            if ((i & 1023) == 0) {
                Map<String, String> m = new HashMap<>();
                m.put("k" + i, s);
                blackhole = m;
            }
        }
        return list;
    }

    /** 典型“数组/byte[]”分配 */
    static Object allocPathB(int count) {
        int total = 0;
        for (int i = 0; i < count; i++) {
            // byte[] 分配
            byte[] buf = new byte[256 + (i & 1023)];
            buf[0] = 1;
            total += buf.length;

            // 再加一点“编码/拷贝”类分配
            String x = Base64.getEncoder().encodeToString(buf);
            byte[] y = x.getBytes(StandardCharsets.UTF_8);
            total += y.length;
        }
        return total;
    }
}
