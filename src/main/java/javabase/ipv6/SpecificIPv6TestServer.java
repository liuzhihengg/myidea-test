package javabase.ipv6;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class SpecificIPv6TestServer {

    // ================= 核心配置区 =================
    // 请在此处替换为您通过系统命令查到的、真实的公网 IPv6 地址
    // 例如: "2408:8207:1111:2222:3333:4444:5555:6666"
    // 注意：不要带有任何方括号 [] 或端口号，仅仅粘贴地址本身。
    private static final String SPECIFIC_IPV6_ADDRESS = "240e:306:3480:7e01:1499:b477:5d24:f473";
    
    // 监听的端口号
    private static final int LISTEN_PORT = 53128;
    // ==============================================

    public static void main(String[] args) {
        // 强制 JVM 使用 IPv6 协议栈进行解析和绑定
        System.setProperty("java.net.preferIPv4Stack", "false");
        System.setProperty("java.net.preferIPv6Addresses", "true");

        // 全连接队列 Backlog 设置为推荐值 50
        int backlog = 50;

        try (ServerSocket serverSocket = new ServerSocket()) {
            // 启用 SO_REUSEADDR 端口复用，避免重启测试时报端口占用
            serverSocket.setReuseAddress(true);

            // 【核心变更】：根据您提供的具体字符串，解析出特定的 InetAddress 实例
            InetAddress specificAddress = InetAddress.getByName(SPECIFIC_IPV6_ADDRESS);
            
            // 严谨性校验：确保您粘贴进去的确实是一个 IPv6 地址
            if (!(specificAddress instanceof Inet6Address)) {
                System.err.println("[致命错误] 您输入的字符串无法被解析为合法的 IPv6 地址，请检查格式！");
                System.err.println("当前解析类型: " + specificAddress.getClass().getSimpleName());
                return;
            }

            // 将 ServerSocket 严格绑定到这个特定的地址和端口上
            InetSocketAddress bindEndpoint = new InetSocketAddress(specificAddress, LISTEN_PORT);
            serverSocket.bind(bindEndpoint, backlog);
            
            System.out.println("====== 特定 IPv6 服务端已精准启动 ======");
            System.out.println("[成功] 正在严格监听指定 IPv6 地址: " + specificAddress.getHostAddress());
            System.out.println("[成功] 监听端口: " + LISTEN_PORT);
            System.out.println("[提示] 本程序现已对本机的其他 IPv6 地址（如局域网地址 fe80:: 或其他网卡地址）免疫，仅响应发往上述地址的请求。");
            System.out.println("==================================================");

            // 阻塞监听循环
            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    InetAddress remoteAddress = clientSocket.getInetAddress();
                    
                    System.out.println("\n[收到新连接] 客户端地址: " + remoteAddress.getHostAddress() + 
                                       " 客户端端口: " + clientSocket.getPort());

                    handleClient(clientSocket);
                    
                } catch (Exception e) {
                    System.err.println("处理客户端连接时发生异常: " + e.getMessage());
                }
            }
        } catch (java.net.BindException be) {
            System.err.println("\n[绑定失败] 无法将程序绑定到您填写的地址: " + SPECIFIC_IPV6_ADDRESS);
            System.err.println("底层原因：Cannot assign requested address。");
            System.err.println("排查建议：请绝对确认这个 IPv6 地址目前确确实实分配在了您当前运行这段代码的电脑网卡上。如果该地址属于光猫或上级路由器，而没有透传给您的电脑，Java是无法绑定的。");
        } catch (Exception e) {
            System.err.println("服务端启动失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void handleClient(Socket clientSocket) {
        try (InputStream in = clientSocket.getInputStream();
             OutputStream out = clientSocket.getOutputStream()) {
             
            // 设定数据读取超时 5000 毫秒
            clientSocket.setSoTimeout(5000);

            byte[] buffer = new byte[1024];
            int bytesRead = in.read(buffer);

            if (bytesRead != -1) {
                String receivedMessage = new String(buffer, 0, bytesRead, StandardCharsets.UTF_8);
                System.out.println("[数据接收] " + receivedMessage.trim());

                String ackMessage = "Server ACK: 成功通过精准绑定的 IPv6 通道收到探测.\n";
                out.write(ackMessage.getBytes(StandardCharsets.UTF_8));
                out.flush();
                System.out.println("[数据发送] 回执已下发。");
            }
        } catch (Exception e) {
            System.err.println("交互异常: " + e.getMessage());
        } finally {
            try { if (!clientSocket.isClosed()) clientSocket.close(); } catch (Exception ignored) {}
        }
    }
}