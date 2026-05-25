package javabase.ipv6;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executors;

public class IPv6WebProbeServer {

    // 推荐使用高位端口，避开运营商对 80/443 等常用 Web 端口的强制封锁
    private static final int LISTEN_PORT = 53128;
    
    // 如果您想绑定特定的 IPv6 地址，填在这里；如果留空或填 "::"，则绑定所有网卡
    private static final String SPECIFIC_IPV6_ADDRESS = "240e:306:348a:dc01:5dfd:f93f:9885:f184"; // 默认使用通配符地址，最省事

    public static void main(String[] args) {
        // 强制 JVM 优先使用 IPv6 网络栈
        System.setProperty("java.net.preferIPv4Stack", "false");
        System.setProperty("java.net.preferIPv6Addresses", "true");

        try {
            InetAddress bindAddress = InetAddress.getByName(SPECIFIC_IPV6_ADDRESS);
            InetSocketAddress socketAddress = new InetSocketAddress(bindAddress, LISTEN_PORT);

            // 创建内置的 HTTP 服务器，第二个参数 0 表示使用系统默认的 TCP backlog 队列长度
            HttpServer server = HttpServer.create(socketAddress, 0);

            // 创建一个处理 HTTP GET 请求的上下文路由 (类似 Spring Boot 的 @GetMapping("/"))
            server.createContext("/", new RootHandler());

            // 设置一个固定大小的线程池来处理并发的浏览器请求，避免单线程阻塞
            // 推荐数值：由于是本地测试，2-4 个线程足矣。
            server.setExecutor(Executors.newFixedThreadPool(4));

            // 启动服务
            server.start();

            System.out.println("=========================================================");
            System.out.println("🚀 Java 原生 IPv6 Web 服务端已成功启动！");
            System.out.println("📡 监听地址: " + bindAddress.getHostAddress());
            System.out.println("🔌 监听端口: " + LISTEN_PORT);
            System.out.println("=========================================================\n");
            
            System.out.println("📱 【手机 5G 访问指南】 📱");
            System.out.println("请在您的手机浏览器地址栏中，严格按照以下格式输入：");
            System.out.println("http://[您的真实公网IPv6地址]:" + LISTEN_PORT + "/");
            System.out.println("\n⚠️ 极其重要的注意事项：");
            System.out.println("1. 必须是 http:// 开头，千万不要用 https://，因为我们没有配置 SSL 证书。");
            System.out.println("2. IPv6 地址本身包含了大量的冒号(:)，为了和端口号的冒号区分，");
            System.out.println("   浏览器标准强制要求必须用英文的中括号 [] 将 IPv6 地址包裹起来！");
            System.out.println("   错误写法：http://240e:abcd:1234::1:53128/  (浏览器会当成搜索词)");
            System.out.println("   正确写法：http://[240e:abcd:1234::1]:53128/ (直接进入网页)");

        } catch (Exception e) {
            System.err.println("❌ Web 服务器启动失败，请检查端口占用或地址绑定权限: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 处理浏览器 HTTP 请求的处理器
     */
    static class RootHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                // 仅响应 GET 请求
                if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                    
                    // 获取访客的 IP 地址，用于在控制台打印日志
                    InetAddress clientAddress = exchange.getRemoteAddress().getAddress();
                    String clientIP = clientAddress.getHostAddress();
                    
                    // 记录请求日志
                    System.out.println("[日志] " + LocalDateTime.now() + " - 收到来自外部的访问！");
                    System.out.println("      -> 访客 IP: " + clientIP);
                    if (clientAddress instanceof Inet6Address) {
                        System.out.println("      -> 协议确认: 这是一个纯正的 IPv6 连接！");
                    }

                    // 准备返回给手机浏览器的 HTML 网页内容
                    String htmlResponse = generateHtmlContent(clientIP);
                    byte[] responseBytes = htmlResponse.getBytes(StandardCharsets.UTF_8);

                    // 构造 HTTP 响应头
                    // 状态码 200 表示 OK，告知浏览器返回的是 HTML 格式，并指定 UTF-8 编码防止中文乱码
                    exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
                    // 发送响应头及数据长度
                    exchange.sendResponseHeaders(200, responseBytes.length);

                    // 将 HTML 数据写入底层 TCP 输出流，推送到手机端
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(responseBytes);
                        os.flush(); // 强制刷入内核发送缓冲区
                    }
                    System.out.println("      -> 状态: 网页内容已成功下发至手机端。\n");
                    
                } else {
                    // 如果手机浏览器发起了 POST 等其他请求，返回 405 Method Not Allowed
                    exchange.sendResponseHeaders(405, -1);
                }
            } catch (Exception e) {
                System.err.println("处理请求时发生异常: " + e.getMessage());
            } finally {
                // 确保请求上下文被正确关闭，释放底层的文件描述符
                exchange.close();
            }
        }

        private String generateHtmlContent(String clientIP) {
            // 使用原生的字符串拼接构建一个极简且美观的移动端适配网页
            String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            return "<!DOCTYPE html>\n" +
                   "<html lang=\"zh-CN\">\n" +
                   "<head>\n" +
                   "    <meta charset=\"UTF-8\">\n" +
                   "    <!-- 针对手机屏幕进行视口缩放适配，避免字体过小 -->\n" +
                   "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                   "    <title>IPv6 连通性测试成功</title>\n" +
                   "    <style>\n" +
                   "        body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; background-color: #f4f4f9; color: #333; padding: 20px; line-height: 1.6; }\n" +
                   "        .container { background: #fff; padding: 30px; border-radius: 12px; box-shadow: 0 4px 15px rgba(0,0,0,0.05); text-align: center; max-width: 600px; margin: 0 auto; }\n" +
                   "        h1 { color: #2ecc71; font-size: 24px; }\n" +
                   "        .info-box { background: #f8f9fa; border-left: 4px solid #3498db; padding: 15px; margin-top: 20px; text-align: left; border-radius: 0 8px 8px 0; word-break: break-all; }\n" +
                   "        .label { font-weight: bold; color: #555; }\n" +
                   "        .footer { margin-top: 30px; font-size: 12px; color: #888; }\n" +
                   "    </style>\n" +
                   "</head>\n" +
                   "<body>\n" +
                   "    <div class=\"container\">\n" +
                   "        <h1>🎉 恭喜！跨网访问成功</h1>\n" +
                   "        <p>您的 5G 手机已成功穿透广域网，与本地 Java 服务端建立了点对点的 IPv6 物理连接。</p>\n" +
                   "        \n" +
                   "        <div class=\"info-box\">\n" +
                   "            <p><span class=\"label\">服务端响应时间:</span><br>" + time + "</p>\n" +
                   "            <p><span class=\"label\">访客的公网 IP:</span><br>" + clientIP + "</p>\n" +
                   "            <p><span class=\"label\">底层处理引擎:</span><br>Java NIO HttpServer</p>\n" +
                   "        </div>\n" +
                   "        \n" +
                   "        <div class=\"footer\">\n" +
                   "            <p>第一性原理测试闭环已达成。您可以安心进行后续的高层业务开发了。</p>\n" +
                   "        </div>\n" +
                   "    </div>\n" +
                   "</body>\n" +
                   "</html>";
        }
    }
}