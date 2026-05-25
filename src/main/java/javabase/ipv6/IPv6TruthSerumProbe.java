package javabase.ipv6;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.Enumeration;

public class IPv6TruthSerumProbe {

    public static void main(String[] args) {
        // 强制使用 IPv6 协议栈，排除 IPv4 干扰
        System.setProperty("java.net.preferIPv4Stack", "false");
        System.setProperty("java.net.preferIPv6Addresses", "true");

        System.out.println("=========================================================================");
        System.out.println("  [Java 视角的网络接口与 IPv6 地址全景透视探针]  ");
        System.out.println("=========================================================================\n");

        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            if (interfaces == null) {
                System.err.println("当前 Java 进程无法获取任何网络接口信息！可能是严重的权限问题或 JVM 环境异常。");
                return;
            }

            int testPort = 53128; // 使用一个冷门端口进行绑定探测测试
            boolean foundGlobalIPv6 = false;

            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                
                // 跳过未激活的网卡和回环网卡 (127.0.0.1 / ::1)
                if (!networkInterface.isUp() || networkInterface.isLoopback()) {
                    continue;
                }

                System.out.println("-------------------------------------------------------------------------");
                System.out.println("网卡名称 (Name)       : " + networkInterface.getName());
                System.out.println("网卡显示名称 (Display): " + networkInterface.getDisplayName());
                
                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                boolean hasIPv6OnThisInterface = false;

                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();

                    // 我们只关心 IPv6 地址
                    if (address instanceof Inet6Address) {
                        hasIPv6OnThisInterface = true;
                        String hostAddress = address.getHostAddress();
                        
                        // 移除可能存在的 Scope ID (如 %wlan0) 以保证字符串纯净
                        if (hostAddress.contains("%")) {
                            hostAddress = hostAddress.substring(0, hostAddress.indexOf("%"));
                        }

                        System.out.println("\n  -> 发现 IPv6 地址: " + hostAddress);
                        
                        // 地址分类判定
                        if (address.isLinkLocalAddress()) {
                            System.out.println("     [类型] 链路本地地址 (fe80::) - 仅限局域网内使用，不可用于外网服务端绑定。");
                        } else if (address.isSiteLocalAddress()) {
                            System.out.println("     [类型] 站点本地地址 - 不具备全球路由能力。");
                        } else {
                            System.out.println("     [类型] ★ 全球单播地址 (公网 IPv6) - 这是潜在可以用于外网绑定的目标地址！");
                            foundGlobalIPv6 = true;
                            
                            // 【核心动作：立即尝试进行内核级的真实绑定测试】
                            System.out.print("     [连通性物理测试] 正在尝试将 ServerSocket 绑定到此地址的 " + testPort + " 端口... ");
                            try (ServerSocket testSocket = new ServerSocket()) {
                                testSocket.setReuseAddress(true);
                                testSocket.bind(new InetSocketAddress(address, testPort));
                                System.out.println("成功！(SUCCESS)");
                                System.out.println("     ✅ 结论：这个地址是完全合法的，可以直接复制到您的服务端代码中替换 SPECIFIC_IPV6_ADDRESS。");
                            } catch (java.net.BindException e) {
                                System.out.println("失败！(FAILED)");
                                System.out.println("     ❌ 诊断：即使 Java 能枚举出这个地址，内核依然拒绝绑定。异常信息：" + e.getMessage());
                                System.out.println("     ❌ 可能原因：地址处于 Deprecated 弃用状态、系统安全策略拦截，或被其他虚拟网卡软件劫持路由。请勿使用此地址。");
                            } catch (Exception e) {
                                System.out.println("发生未知异常: " + e.getMessage());
                            }
                        }
                    }
                }
                
                if (!hasIPv6OnThisInterface) {
                    System.out.println("  -> (此网卡下未检测到任何 IPv6 地址)");
                }
            }

            System.out.println("\n=========================================================================");
            if (!foundGlobalIPv6) {
                System.out.println("⚠️ 严重警告：在所有激活的网卡中，没有找到任何公网 IPv6 地址。");
                System.out.println("如果您确认电脑连了 Wi-Fi 且有 IPv6，这几乎 100% 意味着您当前的 Java 运行环境（如 WSL/Docker/VM）与宿主机物理网络是隔离的。");
                System.out.println("您必须在纯正的宿主机 Windows 环境下（比如直接用 cmd 运行 java -jar）重新运行程序。");
            } else {
                System.out.println("排查完成。请仔细查看上方带有 '✅ 结论' 的绿色输出，挑选并使用那些测试绑定成功的 IPv6 地址。");
            }
            System.out.println("=========================================================================");

        } catch (SocketException e) {
            System.err.println("获取网络接口列表失败: " + e.getMessage());
        }
    }
}