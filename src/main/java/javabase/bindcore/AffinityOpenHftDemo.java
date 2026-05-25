package javabase.bindcore;

import net.openhft.affinity.Affinity;
import net.openhft.affinity.AffinityLock;

import java.util.BitSet;

public class AffinityOpenHftDemo {

    static void busy(long ms) {
        long end = System.nanoTime() + ms * 1_000_000L;
        long x = 0;
        while (System.nanoTime() < end) x++;
        if (x == 42) System.out.println();
    }

    static void loop(String tag) {
        while (true) {
            int cpu = Affinity.getCpu();
            BitSet mask = Affinity.getAffinity();
            System.out.printf("[%s] cpu=%d affinity=%s%n", tag, cpu, mask);
            busy(1500);
            try { Thread.sleep(3500); } catch (InterruptedException ignored) {}
        }
    }

    public static void main(String[] args) {
        for (int i = 0; i < 16; i++) {
            getPinned(i).start();
            getFree(i).start();
        }
    }

    private static Thread getFree(int i) {
        return new Thread(() -> loop("FREE"), "free-worker-" + i);
    }

    private static Thread getPinned(int i) {
        return new Thread(() -> {
            // 申请一个核并绑定（常用方式）
            try (AffinityLock lock = AffinityLock.acquireLock()) {
                System.out.println("PINNED lock=" + lock);
                loop("PINNED");
            }
        }, "pinned-worker-" + i);
    }
}
