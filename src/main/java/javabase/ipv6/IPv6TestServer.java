package javabase.ipv6;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class IPv6TestServer {

    // 推荐测试端口范围：1024-65535，避免使用系统保留端口
    private static final int LISTEN_PORT = 53128;

    public static void main(String[] args) {
        // 设置JVM参数以优化IPv6支持
        System.setProperty("java.net.preferIPv4Stack", "false");
        System.setProperty("java.net.preferIPv6Addresses", "true");

        // 推荐的Backlog队列大小，对于测试环境50即可，生产环境建议1024
        int backlog = 50;

        try (ServerSocket serverSocket = new ServerSocket()) {
            // 启用地址重用，避免重启测试程序时报 "Address already in use"
            serverSocket.setReuseAddress(true);

            // 绑定到IPv6的通配地址 "::" 和指定端口
            InetAddress wildcardIPv6 = InetAddress.getByName("::");
            InetSocketAddress bindAddress = new InetSocketAddress(wildcardIPv6, LISTEN_PORT);
            
            serverSocket.bind(bindAddress, backlog);
            System.out.println("====== IPv6 服务端已启动 ======");
            System.out.println("正在监听IPv6地址: " + wildcardIPv6.getHostAddress() + " 端口: " + LISTEN_PORT);

            // 持续监听外网的探测请求
            while (true) {
                try {
                    // accept() 是一个阻塞方法，直到有客户端连接才会返回
                    Socket clientSocket = serverSocket.accept();
                    InetAddress remoteAddress = clientSocket.getInetAddress();
                    
                    System.out.println("\n[收到新连接] 客户端地址: " + remoteAddress.getHostAddress() + 
                                       " 客户端端口: " + clientSocket.getPort());

                    // 验证是否为IPv6地址
                    if (remoteAddress instanceof Inet6Address) {
                        System.out.println("[状态] 这是一个合法的IPv6连接");
                    } else {
                        System.out.println("[警告] 这是一个IPv4映射连接或非纯IPv6连接");
                    }

                    // 处理客户端请求 (在实际高并发场景应放入线程池，此处为测试直接同步执行)
                    handleClient(clientSocket);
                    
                } catch (Exception e) {
                    System.err.println("处理单个客户端连接时发生异常: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("服务端启动失败，请检查端口是否被占用或是否有权限: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void handleClient(Socket clientSocket) {
        // 使用 try-with-resources 确保流和Socket资源被安全释放
        try (InputStream in = clientSocket.getInputStream();
             OutputStream out = clientSocket.getOutputStream()) {
             
            // 设置读取超时时间，防止恶意连接挂起（推荐值：5000ms）
            clientSocket.setSoTimeout(5000);

            byte[] buffer = new byte[1024];
            int bytesRead = in.read(buffer);

            if (bytesRead != -1) {
                String receivedMessage = new String(buffer, 0, bytesRead, StandardCharsets.UTF_8);
                System.out.println("[数据接收] 收到客户端探测包: " + receivedMessage.trim());

                // 组装回执数据
                String ackMessage = "Server ACK: 成功接收到来自IPv6外网的探测. Timestamp: " + System.currentTimeMillis() + "\n";
                out.write(ackMessage.getBytes(StandardCharsets.UTF_8));
                out.flush();
                System.out.println("[数据发送] 已向客户端发送确认回执");
            } else {
                System.out.println("[数据接收] 客户端建立了连接但未发送任何数据即断开");
            }

        } catch (Exception e) {
            System.err.println("客户端数据交互异常: " + e.getMessage());
        } finally {
            try {
                if (!clientSocket.isClosed()) {
                    clientSocket.close();
                }
            } catch (Exception e) {
                System.err.println("关闭Socket失败: " + e.getMessage());
            }
        }
    }
}