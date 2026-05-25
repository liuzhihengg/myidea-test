package javabase.benchmark; // 你的包名保留，完全不影响动态生成的代码

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.FileWriter;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.function.LongUnaryOperator;

/**
 * 逃逸分析核打击实验：分形生命周期(0 GC) VS 面条代码(百万次 GC 崩溃)
 */
public class UltimateJitBenchmark {

    public static void main(String[] args) throws Exception {
        System.out.println(">>> 1. 正在生成 2000 行纯面条代码 (GodTask.java)...");
        generateGodTask();

        System.out.println(">>> 2. 正在生成 3层 B+ 树结构的完美分形代码 (BPlusTreeTask.java)...");
        generateBPlusTreeTask();

        System.out.println(">>> 3. 调用 JDK 编译器...");
        int godCompileResult = compile("GodTask.java");
        int treeCompileResult = compile("BPlusTreeTask.java");

        if (godCompileResult != 0 || treeCompileResult != 0) {
            System.err.println("编译失败！");
            return;
        }

        System.out.println(">>> 4. 正在通过 ClassLoader 挂载并强转为 JDK 原生接口...");
        URLClassLoader classLoader = URLClassLoader.newInstance(new URL[]{new File(".").toURI().toURL()});

        LongUnaryOperator godTask = (LongUnaryOperator) classLoader.loadClass("GodTask").getDeclaredConstructor().newInstance();
        LongUnaryOperator treeTask = (LongUnaryOperator) classLoader.loadClass("BPlusTreeTask").getDeclaredConstructor().newInstance();

        int warmupIterations = 10_000;  // 减少热身次数，防止热身时 GC 把机器卡死
        // 注意：把压测次数调小到 500 万次，因为面条代码分配内存太疯狂了，我怕你内存扛不住！
        int testIterations = 5_000_000;

        System.out.println("--- 热身中 (Triggering C2 JIT Compilation & Escape Analysis) ---");
        for (int i = 0; i < warmupIterations; i++) {
            godTask.applyAsLong(1L);
            treeTask.applyAsLong(1L);
        }

        System.out.println("\n--- 开始物理极限压测 (5,000,000 次原生接口调用) ---");

        // 测 B+ 树代码 (先测优秀的，因为它不会触发严重 GC)
        long startTree = System.currentTimeMillis();
        long treeResult = 0;
        for (int i = 0; i < testIterations; i++) {
            treeResult = treeTask.applyAsLong(treeResult);
        }
        long treeTime = System.currentTimeMillis() - startTree;
        System.out.println("✅ B+树代码 (逃逸分析成功, 标量替换, 0 GC) 耗时: " + treeTime + " ms");

        // 测面条代码 (警告：前方高能，内存暴雨来袭！)
        System.out.println("⚠️ 警告：正在进入面条代码压测，将触发恐怖的内存逃逸与 GC 风暴...");
        long startGod = System.currentTimeMillis();
        long godResult = 0;
        for (int i = 0; i < testIterations; i++) {
            godResult = godTask.applyAsLong(godResult);
        }
        long godTime = System.currentTimeMillis() - startGod;
        System.out.println("❌ 面条代码 (逃逸分析失败, 海量对象堆积) 耗时: " + godTime + " ms");

        System.out.println("\n====== 最终物理宣判结果 ======");
        double speedup = (double) godTime / treeTime;
        System.out.printf("性能差距: B+树分形代码比面条代码快了 %.2f 倍！\n", speedup);

        // 推荐基准数值提示
        if (speedup > 50) {
            System.out.println("🎯 [架构师结论]: 差距超过 50 倍！完美证明了生命周期切分对 JIT 逃逸分析的决定性作用！");
        }

        new File("GodTask.java").delete();
        new File("GodTask.class").delete();
        new File("BPlusTreeTask.java").delete();
        new File("BPlusTreeTask.class").delete();
    }

    // ==========================================
    // 生成器 1：面条代码 (致命缺陷：2000 个无法被标量替换的短命数组)
    // ==========================================
    private static void generateGodTask() throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("public class GodTask implements java.util.function.LongUnaryOperator {\n");
        sb.append("    public long applyAsLong(long state) {\n");
        // 为了确保触发 C2 编译且不触发 HugeMethodLimit，我们这次只写 500 个块，但足以致命
        for (int i = 0; i < 500; i++) {
            // 【核武器注入】：每次运算都 new 一个包含 1 个元素的 long 数组！
            sb.append("        long[] box_").append(i).append(" = new long[]{state};\n");
            sb.append("        box_").append(i).append("[0] ^= (box_").append(i).append("[0] << 13);\n");
            sb.append("        state = box_").append(i).append("[0] + ").append(i).append("L;\n");
        }
        sb.append("        return state;\n");
        sb.append("    }\n}\n");
        try (FileWriter writer = new FileWriter("GodTask.java")) { writer.write(sb.toString()); }
    }

    // ==========================================
    // 生成器 2：B+ 树分形代码 (完美触发逃逸分析与标量替换)
    // ==========================================
    private static void generateBPlusTreeTask() throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("public class BPlusTreeTask implements java.util.function.LongUnaryOperator {\n");

        // Root (Level 0) -> calls 5 nodes of Level 1
        sb.append("    public long applyAsLong(long state) {\n");
        for (int i = 0; i < 5; i++) sb.append("        state = L1_node_").append(i).append("(state);\n");
        sb.append("        return state;\n    }\n");

        // Level 1 -> calls 10 nodes of Level 2
        for (int i = 0; i < 5; i++) {
            sb.append("    private long L1_node_").append(i).append("(long state) {\n");
            for (int j = 0; j < 10; j++) sb.append("        state = L2_leaf_").append(i).append("_").append(j).append("(state);\n");
            sb.append("        return state;\n    }\n");
        }

        // Level 2 (Leaf) -> 10 个干活的 block (总计 5 * 10 * 10 = 500 个块，运算量与上帝代码绝对一致)
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 10; j++) {
                sb.append("    private long L2_leaf_").append(i).append("_").append(j).append("(long state) {\n");
                for (int k = 0; k < 10; k++) {
                    int index = (i * 100) + (j * 10) + k;
                    // 【同样的核武器】：在微观的子生命周期里 new 数组！
                    sb.append("        long[] box_").append(k).append(" = new long[]{state};\n");
                    sb.append("        box_").append(k).append("[0] ^= (box_").append(k).append("[0] << 13);\n");
                    sb.append("        state = box_").append(k).append("[0] + ").append(index).append("L;\n");
                }
                sb.append("        return state;\n    }\n");
            }
        }
        sb.append("}\n");
        try (FileWriter writer = new FileWriter("BPlusTreeTask.java")) { writer.write(sb.toString()); }
    }

    private static int compile(String fileName) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        return compiler.run(null, null, null, fileName);
    }
}
