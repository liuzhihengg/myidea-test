package javabase.zerocopytest;

import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class ZeroCopyNetworkLabV2 {

    // 推荐数值：单块缓冲区大小。对于网络 I/O，过大（如 1GB）会导致 CPU 缓存命中率下降
    // 过小（如 4KB）会导致频繁的系统调用。8MB 到 32MB 是万兆网络下的甜点值。
    private static final int CHUNK_SIZE = 8 * 1024 * 1024; // 8MB

    public static void main(String[] args) throws Exception {

        long targetMB = Long.parseLong("100000");
        long targetBytes = targetMB * 1024 * 1024;
        System.out.println("=== 动态参数化网络零拷贝实验室 ===");
        System.out.println("目标发送数据量: " + targetMB + " MB (" + targetBytes + " Bytes)");

        // 1. 预先分配一块复用的堆外直接内存（真正的用户态物理内存空间）
        ByteBuffer directBuffer = ByteBuffer.allocateDirect(CHUNK_SIZE);

        // 模拟向内存中填充 Canal 的序列化数据 (全填 1)
        for (int i = 0; i < CHUNK_SIZE; i++) {
            directBuffer.put((byte) 1);
        }

        // 2. 建立网络连接与内核参数调优
        SocketChannel socketChannel = SocketChannel.open();
        // 推荐数值：发送缓冲区 16MB，关闭 Nagle 算法降低延迟
        socketChannel.setOption(StandardSocketOptions.SO_SNDBUF, 16 * 1024 * 1024);
        socketChannel.setOption(StandardSocketOptions.TCP_NODELAY, true);
        socketChannel.connect(new InetSocketAddress("127.0.0.1", 8082));

        System.out.println("连接成功，开始倾泻数据...");
        long startTime = System.nanoTime();
        long bytesWrittenTotal = 0;

        // 3. 核心循环：精准控制发送总量
        while (bytesWrittenTotal < targetBytes) {
            directBuffer.position(0);

            // 计算当前这一轮还需要发多少。
            // 如果剩余需要发送的量大于 CHUNK_SIZE，就发满整个 CHUNK；否则只发剩余量。
            long remainingBytes = targetBytes - bytesWrittenTotal;
            int bytesToSendThisRound = (int) Math.min(CHUNK_SIZE, remainingBytes);
            directBuffer.limit(bytesToSendThisRound);

            // 极限发送：这是一个阻塞调用（默认情况下）。
            // 内核的 TCP 栈不一定能一次性把你要求的 bytesToSendThisRound 全发出去，
            // 必须用 while 循环确保这块 Buffer 里的有效数据被彻底榨干并推入内核。
            while (directBuffer.hasRemaining()) {
                // 底层触发 write() 或 writev() 系统调用。
                // 数据从 Direct Memory (用户态) 拷贝到 Socket Send Buffer (内核态)
                int written = socketChannel.write(directBuffer);
                bytesWrittenTotal += written;
            }
        }

        long endTime = System.nanoTime();
        double seconds = (endTime - startTime) / 1_000_000_000.0;
        double speedMBps = targetMB / seconds;

        System.out.println("发送完成！");
        System.out.println("实际发送字节数: " + bytesWrittenTotal);
        System.out.printf("总耗时: %.3f 秒\n", seconds);
        System.out.printf("发送端吞吐量: %.2f MB/s\n", speedMBps);

        socketChannel.close();
    }
}
