package javabase.ipv6;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class IPv6TestClient {

    // 填入您刚刚开启外网访问的公网IPv6地址，注意无需加方括号
    // 例如: "2408:8207:xxxx:xxxx:xxxx:xxxx:xxxx:xxxx"
    private static final String TARGET_IPV6_ADDRESS = "240e:306:3480:7e01:1499:b477:5d24:f473";
    private static final int TARGET_PORT = 53128;

    public static void main(String[] args) {
        System.setProperty("java.net.preferIPv4Stack", "false");
        System.setProperty("java.net.preferIPv6Addresses", "true");

        System.out.println("====== IPv6 客户端连通性测试开始 ======");
        System.out.println("目标地址: [" + TARGET_IPV6_ADDRESS + "]:" + TARGET_PORT);

        // 核心参数设定
        // 连接超时时间：推荐 3000ms。外网环境复杂，超过3秒无响应通常意味着丢包或防火墙拦截
        int connectTimeout = 3000;
        // 读取超时时间：推荐 5000ms。连接建立后等待服务端返回数据的最大时间
        int readTimeout = 5000;

        long startTime = System.currentTimeMillis();
        long connectEndTime = 0;

        try (Socket socket = new Socket()) {
            // 优化Socket参数
            // 禁用Nagle算法，确保探测包立即发送（推荐值：true）
            socket.setTcpNoDelay(true);
            socket.setSoTimeout(readTimeout);

            // 1. 发起网络连接测试 (TCP三次握手)
            InetSocketAddress endpoint = new InetSocketAddress(TARGET_IPV6_ADDRESS, TARGET_PORT);
            System.out.println("\n[阶段一] 正在发起TCP连接请求...");
            
            socket.connect(endpoint, connectTimeout);
            connectEndTime = System.currentTimeMillis();
            
            System.out.println("[成功] 网络连接建立完成！");
            System.out.println("[延迟] TCP建连耗时 (RTT): " + (connectEndTime - startTime) + " ms");

            // 2. 发送应用层探测包
            System.out.println("\n[阶段二] 正在发送应用层探测数据...");
            try (OutputStream out = socket.getOutputStream();
                 InputStream in = socket.getInputStream()) {

                String pingMessage = "PING: IPv6 Network Test. Client Time: " + System.currentTimeMillis();
                out.write(pingMessage.getBytes(StandardCharsets.UTF_8));
                out.flush();
                long sendTime = System.currentTimeMillis();

                // 3. 等待服务端回执
                System.out.println("\n[阶段三] 正在等待服务端响应...");
                byte[] buffer = new byte[1024];
                int bytesRead = in.read(buffer);

                if (bytesRead != -1) {
                    String response = new String(buffer, 0, bytesRead, StandardCharsets.UTF_8);
                    long receiveTime = System.currentTimeMillis();
                    System.out.println("[成功] 收到服务端回执: " + response.trim());
                    System.out.println("[延迟] 应用层交互耗时: " + (receiveTime - sendTime) + " ms");
                } else {
                    System.err.println("[失败] 服务端未返回任何数据，连接已断开。");
                }
            }

        } catch (java.net.SocketTimeoutException e) {
            System.err.println("\n[致命失败] 连接或读取超时！");
            System.err.println("可能原因: 1. 目标主机的防火墙(iptables/ufw)拦截了入站流量。");
            System.err.println("         2. 宽带运营商(ISP)在骨干网封锁了该端口。");
            System.err.println("         3. 路由器的IPv6 SPI防火墙未放行。");
            System.err.println("建议值: 检查端口是否为常规高危被封端口(如80, 443, 8080)，建议更换至 10000 以上的随机端口重试。");
        } catch (java.net.ConnectException e) {
            System.err.println("\n[致命失败] 连接被拒绝 (Connection refused)！");
            System.err.println("可能原因: 1. 目标地址上没有任何程序在监听 " + TARGET_PORT + " 端口。");
            System.err.println("         2. 服务端程序未绑定到公网IPv6地址 (例如错误地绑定到了 127.0.0.1 或仅限内网的地址)。");
        } catch (java.net.NoRouteToHostException e) {
            System.err.println("\n[致命失败] 找不到到主机的路由！");
            System.err.println("可能原因: 执行测试的客户端本身没有获取到有效的公网IPv6地址，或者本地网络不支持IPv6。");
        } catch (Exception e) {
            System.err.println("\n[未知错误] 发生未捕获的网络异常: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            e.printStackTrace();
        }
    }
}