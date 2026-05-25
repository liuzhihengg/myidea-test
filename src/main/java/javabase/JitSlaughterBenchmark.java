package javabase;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * 物理级性能宣判实验：上帝面条代码 VS 分形生命周期代码
 */
public class JitSlaughterBenchmark {

    public static void main(String[] args) throws Exception {
        System.out.println(">>> 正在生成 2000 行面条代码 (GodMethod.java)...");
        generateGodMethod();

        System.out.println(">>> 正在生成 40 个子生命周期的分形代码 (CleanMethod.java)...");
        generateCleanMethod();

        System.out.println(">>> 正在调用 JDK 底层编译器进行动态编译...");
        compile("GodMethod.java");
        compile("CleanMethod.java");

        System.out.println(">>> 编译完成，准备进入 JIT 热身与极限压测...");
        runBenchmark();
    }

    // ==========================================
    // 1. 生成面条代码 (把所有的逻辑铺平在一个 2000 行的方法里)
    // ==========================================
    private static void generateGodMethod() throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("public class GodMethod {\n");
        sb.append("    public long process(long state) {\n");
        // 强行塞入 2000 行没有任何生命周期切分的连续指令
        for (int i = 0; i < 2000; i++) {
            sb.append("        state ^= (state << 13); state ^= (state >>> 17); state ^= (state << 5);\n");
            sb.append("        state += ").append(i).append("L;\n");
        }
        sb.append("        return state;\n");
        sb.append("    }\n");
        sb.append("}\n");
        try (FileWriter writer = new FileWriter("GodMethod.java")) { writer.write(sb.toString()); }
    }

    // ==========================================
    // 2. 生成分形代码 (将 2000 行指令切分为 40 个子生命周期，每个 50 行)
    // ==========================================
    private static void generateCleanMethod() throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("public class CleanMethod {\n");

        // 宏观生命周期：负责调度
        sb.append("    public long process(long state) {\n");
        for (int i = 0; i < 40; i++) {
            sb.append("        state = subLifecycle_").append(i).append("(state);\n");
        }
        sb.append("        return state;\n");
        sb.append("    }\n\n");

        // 微观生命周期：40 个短小精悍的子方法，严格遵守米勒定律，绝不臃肿
        for (int i = 0; i < 200; i++) {
            sb.append("    private long subLifecycle_").append(i).append("(long state) {\n");
            for (int j = 0; j < 10; j++) {
                int index = i * 10 + j;
                sb.append("        state ^= (state << 13); state ^= (state >>> 17); state ^= (state << 5);\n");
                sb.append("        state += ").append(index).append("L;\n");
            }
            sb.append("        return state;\n");
            sb.append("    }\n");
        }
        sb.append("}\n");
        try (FileWriter writer = new FileWriter("CleanMethod.java")) { writer.write(sb.toString()); }
    }

    private static void compile(String fileName) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        compiler.run(null, null, null, fileName);
    }

    // ==========================================
    // 3. 执行基准测试对比
    // ==========================================
    private static void runBenchmark() throws Exception {
        URLClassLoader classLoader = URLClassLoader.newInstance(new URL[]{new File(".").toURI().toURL()});

        Object godInstance = classLoader.loadClass("GodMethod").getDeclaredConstructor().newInstance();
        Method godProcess = godInstance.getClass().getMethod("process", long.class);

        Object cleanInstance = classLoader.loadClass("CleanMethod").getDeclaredConstructor().newInstance();
        Method cleanProcess = cleanInstance.getClass().getMethod("process", long.class);

        int warmupIterations = 20000; // 必须热身，让 JVM 触发 JIT 编译
        int testIterations = 10_000_000; // 一千万次高频调用

        System.out.println("--- 热身中 (Warming up JIT Compiler) ---");
        for (int i = 0; i < warmupIterations; i++) {
            godProcess.invoke(godInstance, 1L);
            cleanProcess.invoke(cleanInstance, 1L);
        }

        System.out.println("--- 开始物理极限压测 (10,000,000 次调用) ---");

        // 测面条代码
        long startGod = System.currentTimeMillis();
        long godResult = 0;
        for (int i = 0; i < testIterations; i++) {
            godResult = (long) godProcess.invoke(godInstance, godResult);
        }
        long godTime = System.currentTimeMillis() - startGod;

        // 测分形代码
        long startClean = System.currentTimeMillis();
        long cleanResult = 0;
        for (int i = 0; i < testIterations; i++) {
            cleanResult = (long) cleanProcess.invoke(cleanInstance, cleanResult);
        }
        long cleanTime = System.currentTimeMillis() - startClean;

        System.out.println("\n====== 最终物理宣判结果 ======");
        System.out.println("面条代码 (2000行一镜到底) 耗时: " + godTime + " ms");
        System.out.println("分形代码 (40个生命周期细化) 耗时: " + cleanTime + " ms");

        double speedup = (double) godTime / cleanTime;
        System.out.printf("性能差距: 分形代码比面条代码快了 %.2f 倍！\n", speedup);
    }
}
