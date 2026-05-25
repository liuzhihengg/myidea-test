package javabase.bytebuddyagent;

public class Counters {

    private static final java.util.concurrent.ConcurrentHashMap<String, java.util.concurrent.atomic.LongAdder> MAP =
            new java.util.concurrent.ConcurrentHashMap<>();

    public static void inc(String k) {
        MAP.computeIfAbsent(k, x -> new java.util.concurrent.atomic.LongAdder()).increment();
    }

    public static int getSize() {
        return MAP.size();
    }

    public static java.util.Map<String, Long> snapshot() {
        return MAP.entrySet().stream().collect(java.util.stream.Collectors.toMap(
                java.util.Map.Entry::getKey, e -> e.getValue().sum()));
    }
}
