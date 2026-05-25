package javabase.zerocopytest;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * 零拷贝极限物理实验室：跨越用户态与内核态的算力绞肉机
 */
public class ZeroCopyLab {

    private static final String FILE_PATH = "/Users/tiaojiheng/source_code_study/sandBox/huge_payload.data";
    private static final int PORT = 9999;

    public static void main(String[] args) throws Exception {
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            System.err.println("❌ 找不到物理文件！请先执行 dd 命令生成 1GB 数据！");
            return;
        }

        System.out.println("==================================================");
        System.out.println("🌌 零拷贝物理实验室启动 | 载荷大小: " + (file.length() / 1024 / 1024) + " MB");
        System.out.println("==================================================");

        // 1. 启动暗物质黑洞（模拟极速消费的网卡对端）
        Thread blackhole = new Thread(ZeroCopyLab::startBlackholeServer);
        blackhole.setDaemon(true);
        blackhole.start();
        Thread.sleep(1000); // 等待黑洞引力场就绪

        // 2. 实验 A：传统 I/O 的物理折磨 (四次拷贝 + 四次上下文切换)
        System.out.println("\n[实验 A] 启动传统 InputStream/OutputStream 拷贝...");
        testTraditionalCopy(file);

        // 3. 实验 B：零拷贝的光速直通 (DMA 物理搬运)
        System.out.println("\n[实验 B] 启动 FileChannel.transferTo 零拷贝...");
        testZeroCopy(file);

        // 4. 实验 C：协议封包的“蛋疼”限制
        System.out.println("\n[实验 C] 零拷贝的物理枷锁：试图追加 13 字节协议头...");
        testZeroCopyWithProtocolLimit(file);

        System.out.println("\n✅ 物理实验结束！");
    }

    /**
     * 实验 A：极其愚蠢的用户态/内核态来回穿透
     */
    private static void testTraditionalCopy(File file) {
        try (Socket socket = new Socket("127.0.0.1", PORT);
             FileInputStream fis = new FileInputStream(file);
             DataOutputStream dos = new DataOutputStream(socket.getOutputStream())) {

            byte[] buffer = new byte[4096]; // 经典的 4KB 物理页大小缓冲
            long startTime = System.currentTimeMillis();
            long totalBytes = 0;
            int read;

            // 致命循环：每一次 read() 和 write() 都会强行触发 CPU 的内核态陷阱 (Trap)！
            while ((read = fis.read(buffer)) >= 0) {
                dos.write(buffer, 0, read);
                totalBytes += read;
            }

            long cost = System.currentTimeMillis() - startTime;
            System.out.printf("👉 [传统 I/O] 发送 %d 字节，耗时: %d 毫秒\n", totalBytes, cost);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 实验 B：剥夺 CPU 控制权的 DMA 物理直飞
     */
    private static void testZeroCopy(File file) {
        try (SocketChannel socketChannel = SocketChannel.open();
             FileChannel fileChannel = new FileInputStream(file).getChannel()) {

            socketChannel.connect(new InetSocketAddress("127.0.0.1", PORT));
            long startTime = System.currentTimeMillis();

            // 终极魔法指令：底层直接调用操作系统的 sendfile() 甚至 macOS 的 sendfile()
            // 彻底绕过 JVM 的 byte[] 用户态内存！
            long transferred = fileChannel.transferTo(0, fileChannel.size(), socketChannel);

            long cost = System.currentTimeMillis() - startTime;
            System.out.printf("🚀 [零拷贝 I/O] 发送 %d 字节，耗时: %d 毫秒\n", transferred, cost);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 实验 C：揭露协议封包的致命伤
     */
    private static void testZeroCopyWithProtocolLimit(File file) {
        try (SocketChannel socketChannel = SocketChannel.open();
             FileChannel fileChannel = new FileInputStream(file).getChannel()) {

            socketChannel.connect(new InetSocketAddress("127.0.0.1", PORT));
            long startTime = System.currentTimeMillis();

            // 1. 极其难受的拼装：首先，你必须单独发一次协议头！引发一次额外的系统调用！
            String header = "$1073741824\r\n"; // 模拟 RESP 协议头
            ByteBuffer headerBuffer = ByteBuffer.wrap(header.getBytes());
            while (headerBuffer.hasRemaining()) {
                socketChannel.write(headerBuffer);
            }

            // 2. 然后再用零拷贝发送主体
            long transferred = fileChannel.transferTo(0, fileChannel.size(), socketChannel);

            // 3. 最后再单独发一次协议尾部！又是一次系统调用！
            String footer = "\r\n";
            ByteBuffer footerBuffer = ByteBuffer.wrap(footer.getBytes());
            while (footerBuffer.hasRemaining()) {
                socketChannel.write(footerBuffer);
            }

            long cost = System.currentTimeMillis() - startTime;
            System.out.printf("⚠️ [协议拼接] 零拷贝被迫碎片化，发送载荷 %d 字节，耗时: %d 毫秒\n", transferred, cost);
            System.out.println("   -> 物理点评：原本完美的一体化 DMA 管道，被应用层协议强行切断成了三截！");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 极其贪婪的底层吞噬器，只进不出，防止网络滑动窗口阻塞实验数据
     */
    private static void startBlackholeServer() {
        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {
            serverSocketChannel.bind(new InetSocketAddress(PORT));
            ByteBuffer buffer = ByteBuffer.allocateDirect(1024 * 1024); // 1MB 直接内存
            while (true) {
                SocketChannel client = serverSocketChannel.accept();
                new Thread(() -> {
                    try {
                        while (client.read(buffer) != -1) {
                            buffer.clear(); // 物理级清空，数据化为虚无
                        }
                    } catch (Exception ignored) {}
                }).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
