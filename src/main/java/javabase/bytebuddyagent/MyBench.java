package javabase.bytebuddyagent;

import org.openjdk.jmh.annotations.*;
import java.util.concurrent.TimeUnit;

import static javabase.bytebuddyagent.TestAgent.test;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 8, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Fork(1)
@State(Scope.Thread) // 或 Scope.Benchmark，看你的需求
public class MyBench {

    @Param({"false","true"}) // 在运行配置里也能覆盖
    public boolean instrumented;

    @Setup(Level.Trial)                 // 每个 trial 开始时执行一次
    public void setupTrial() {
        if (instrumented) {
            InAppAgent.install();           // 只有 instrumented=true 时才安装 agent
        }
    }

    @Benchmark
    public void baseline() {
        // 被测代码
        for (int i = 0; i < 1000; i++) {
            test(String.valueOf(1));
        }
    }
}
