package javabase.zerocopytest;

import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class ZeroCopyNetworkLab {

    // 推荐数值：不要试图一次性分配 10GB。
    // 分配 64MB 的直接内存作为发送缓冲池，循环利用。
    private static final int BUFFER_SIZE = 64 * 1024 * 1024;

    public static void main(String[] args) throws Exception {
        System.out.println("=== 开始用户态内存直通网络的最大化优化测试 ===");

        // 1. 分配堆外内存（Direct Memory）。这块内存不由 JVM GC 频繁搬运。
        // 它在物理内存中，OS 可以直接对其发起 DMA 操作。
        ByteBuffer directBuffer = ByteBuffer.allocateDirect(BUFFER_SIZE);

        // 模拟向内存中填充 Canal 的 Entry 序列化数据
        for (int i = 0; i < BUFFER_SIZE; i++) {
            directBuffer.put((byte) 1);
        }
        directBuffer.flip(); // 准备读取

        // 2. 建立网络连接
        SocketChannel socketChannel = SocketChannel.open();
        // 推荐数值：由于我们在内网进行高吞吐测试，大幅调大内核的发送缓冲区 SO_SNDBUF
        // 建议设置为 16MB 或更高，减少 TCP 拥塞控制窗口的限制
        socketChannel.setOption(StandardSocketOptions.SO_SNDBUF, 16 * 1024 * 1024);
        socketChannel.setOption(StandardSocketOptions.TCP_NODELAY, true);
        socketChannel.connect(new InetSocketAddress("127.0.0.1", 8080)); // 替换为目标地址

        long startTime = System.nanoTime();

        // 3. 极限发送：将直接内存的数据推入 Socket
        // 这里的本质是调用操作系统的 write() 或 writev()
        long bytesWritten = 0;
        while (bytesWritten < BUFFER_SIZE) {
            // 此时发生了一次从 用户态(DirectMemory) 到 内核态(Socket Buffer) 的 CPU 拷贝
            // 以及一次上下文切换。这是 Java 网络编程无法逃避的物理屏障。
            bytesWritten += socketChannel.write(directBuffer);
        }

        long endTime = System.nanoTime();

        System.out.println("64MB 堆外内存网络发送完成！");
        System.out.println("发送字节数: " + bytesWritten);
        System.out.println("耗时: " + (endTime - startTime) / 1_000_000 + " ms");

        // 如果是要发送 10GB 数据，就是将上述 64MB 的 directBuffer 重复复用、填充、发送。
        socketChannel.close();
    }
}
