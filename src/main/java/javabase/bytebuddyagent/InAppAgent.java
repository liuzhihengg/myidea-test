package javabase.bytebuddyagent;

import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;

import static net.bytebuddy.matcher.ElementMatchers.*;

public final class InAppAgent {
    public static void install() {
        // JDK9+ 推荐在 JVM 参数里加：-Djdk.attach.allowAttachSelf=true
        // 若做不到，可在最早期：System.setProperty("jdk.attach.allowAttachSelf","true");
        var inst = ByteBuddyAgent.install();

        new AgentBuilder.Default()
                .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
                .ignore(nameStartsWith("net.bytebuddy.")
                        .or(isSynthetic()))
                .type(named("javabase.bytebuddyagent.TestAgent"))
                .transform((builder, type, cl, module, pd) ->
                        builder.visit(Advice.to(RecordOneAdvice.class)
                                .on(named("test").and(takesArguments(1)))) // 按需放宽匹配
                )
                .installOn(inst);
    }

    public static class RecordOneAdvice {
        @Advice.OnMethodEnter
        public static void onEnter(@Advice.AllArguments Object[] arr) {
//            System.out.println("执行");
//            Counters.inc( "xx" + key);
        }
    }
}
