package javabase.zerocopytest;

import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class ZeroCopyBlackholeServer {
    public static void main(String[] args) throws Exception {
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        // 推荐数值：由于是本地极速测试，调大接收缓冲区
        serverChannel.setOption(StandardSocketOptions.SO_RCVBUF, 16 * 1024 * 1024);
        serverChannel.bind(new InetSocketAddress(8082));
        System.out.println("黑洞服务器已启动，监听端口 8080，等待极速数据倾泻...");

        SocketChannel clientChannel = serverChannel.accept();
        clientChannel.setOption(StandardSocketOptions.SO_RCVBUF, 16 * 1024 * 1024);
        System.out.println("客户端已连接，开始接收数据...");

        // 分配 8MB 的堆外内存用于接收
        ByteBuffer buffer = ByteBuffer.allocateDirect(8 * 1024 * 1024);
        long totalRead = 0;
        long startTime = System.nanoTime();

        while (true) {
            buffer.clear();
            int read = clientChannel.read(buffer);
            if (read == -1) break; // 连接关闭
            totalRead += read;
        }

        long endTime = System.nanoTime();
        double seconds = (endTime - startTime) / 1_000_000_000.0;
        double speedMBps = (totalRead / (1024.0 * 1024.0)) / seconds;

        System.out.println("接收完毕！总接收量: " + (totalRead / 1024 / 1024) + " MB");
        System.out.printf("平均吞吐量: %.2f MB/s\n", speedMBps);

        clientChannel.close();
        serverChannel.close();
    }
}
