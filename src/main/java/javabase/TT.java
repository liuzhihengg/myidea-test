package javabase;

import org.slf4j.MDC;

import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

public class TT {
    public static void main(String[] args) throws ParseException {
//        long current = System.currentTimeMillis();
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//        int minUpdateTime = (int)(sdf.parse(sdf.format(new java.util.Date(current))).getTime() / 1000);
//
//        System.out.println((int) System.currentTimeMillis() / 1000);
//        System.out.println((int) (System.currentTimeMillis() / 1000));
//        System.out.println((int) ((System.currentTimeMillis() -500) / 1000));
//        // 转换为北京时间
//        LocalDateTime beijingTime = Instant.ofEpochMilli((int)(System.currentTimeMillis() / 1000) * 1000L)
//                .atZone(ZoneId.of("Asia/Shanghai"))
//                .toLocalDateTime();
//
//        // 格式化输出
//        String formattedTime = beijingTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
//        System.out.println("北京时间：" + formattedTime);
//        DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
//        System.out.println(DATE_TIME_FORMATTER.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(System.currentTimeMillis()), ZoneId.systemDefault())));
//        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(8, 32, 1000, TimeUnit.MILLISECONDS,
//                new LinkedBlockingQueue<Runnable>(10));
//        MDC.put("test", "tt");
//        System.out.println("主线程, " + MDC.get("test"));
//        ExecutorService ttlExecutorService = MdcAwareExecutors.wrap(threadPoolExecutor);
//        extracted(ttlExecutorService);
//
//        LongOpenHashSet tags = new LongOpenHashSet();
//        for (long i = 0; i < 1_000_000; i++) tags.add(i);
//
//        Thread reader = new Thread(() -> {
//            while (true) {
//                for (LongIterator it = tags.iterator(); it.hasNext(); ) {
//                    it.nextLong();      // 这里会随机 NPE
//                }
//            }
//        });
//
//        Thread remover = new Thread(() -> {
//            while (true) {
//                for (long i = 0; i < 1_000_000; i++) tags.remove(i);
//                for (long i = 0; i < 1_000_000; i++) tags.add(i);
//            }
//        });
//
//        reader.start();
//        remover.start();
//        System.out.println(String.format("s%,%s"));
//        System.out.println("340KG340KG".replace("KG", ""));
//        System.out.println(pairSet);
        //        test1();
//        List<String> lines = List.of("A", "B", "C");
//        System.out.println(lines.subList(0, 5));
//
//// 不带结尾换行
//        String s1 = String.join(System.lineSeparator(), lines);
//
//// 带结尾换行（很多文本工具喜欢最后一行也以换行结尾）
//        String s2 = lines.stream()
//                .collect(Collectors.joining(System.lineSeparator(), "", System.lineSeparator()));
//        System.out.println(s1);
//        System.out.println(s2);
//        Object a = Double.valueOf(1.0);
//        System.out.println(String.valueOf(a));
//        System.out.println((String) a);
//        System.out.println(Arrays.asList(1, 2, 3).stream().filter(k -> k > 5).noneMatch(k -> k == 4));

        String pre = "http://ai-toolbox.corp.qunar.com/analysis/ai-code-report?branch=";
        List<String> list = new ArrayList<>();
        list.add("r-260428-145312-zhiheng.liu");
        list.add("r-260428-105219-zhiheng.liu");
        list.add("r-260428-094841-zhiheng.liu");
        list.add("r-260427-220257-zhiheng.liu");
        list.add("r-260427-113418-leyiz.zhang");
        list.add("r-260424-193840-leyiz.zhang");
        list.add("r-260421-161344-zhiheng.liu");
        list.add("r-260409-215149-zhiheng.liu");
        list.add("r-260330-150555-zhiheng.liu");
        for (String s : list) {
            System.out.println(pre + s);
        }
    }

    private static void extracted(ExecutorService ttlExecutorService) {
        ttlExecutorService.submit(() -> System.out.println("子线程" + MDC.get("test")));
    }

    public static void test1() {
        CopyOnWriteArrayList<String> list = new CopyOnWriteArrayList<>(List.of("a", "b", "c"));

        for (Iterator<String> it = list.iterator(); it.hasNext(); ) {
            String val = it.next();
            if (val.equals("b")) {
                list.remove(val);  // ❌ UnsupportedOperationException
            }
        }
        System.out.println(list);
    }

    public static void timeCompare() {
        // 给定的时间字符串
        String dateStr = "2025-03-31 20:00:00.423";

        // 定义格式（注意带上毫秒的.SSS）
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

        // 将字符串解析成LocalDateTime对象
        LocalDateTime localDateTime = LocalDateTime.parse(dateStr, formatter);

        // 指定时区（北京时间）
        ZonedDateTime zonedDateTime = localDateTime.atZone(ZoneId.of("Asia/Shanghai"));

        // 获取毫秒数
        long millis = zonedDateTime.toInstant().toEpochMilli();

        System.out.println("毫秒数：" + millis);

        System.out.println((int) millis / 1000);
        System.out.println((int) (millis / 1000));
        System.out.println((int) ((millis -500) / 1000));

        long millis1 = millis;
        long millis2 = millis1 + 300; // 假设差距500毫秒（不到1秒）

        LocalDateTime time1 = Instant.ofEpochMilli(millis1)
                .atZone(ZoneId.of("Asia/Shanghai"))
                .toLocalDateTime()
                .truncatedTo(ChronoUnit.SECONDS);

        LocalDateTime time2 = Instant.ofEpochMilli(millis2)
                .atZone(ZoneId.of("Asia/Shanghai"))
                .toLocalDateTime()
                .truncatedTo(ChronoUnit.SECONDS);

        if (time1.isEqual(time2)) {
            System.out.println("两个时间相同（精确到秒）");
        } else if (time1.isBefore(time2)) {
            System.out.println("time1 早于 time2");
        } else {
            System.out.println("time1 晚于 time2");
        }
    }

    public static void inl() {
        List<Integer> collect = Arrays.asList(1, 2).stream().collect(Collectors.toList());
        collect.add(3);
        System.out.println(collect);
    }

    public static void cal() {
        String format = "http://av.corp.qunar.com/search/nation?dpt=%s&arr=%s&date=20250503&carrier=ALL&type=default&resultType=avdata&direct=true&client=f_transfer_provider";
        Set<String> dpt = new HashSet<>(Arrays.asList("XIY"));
        Set<String> arr = new HashSet<>(Arrays.asList("XMN"));
        System.out.println(String.format(format, dpt, arr));
        System.out.println(Float.parseFloat(""));
    }


}
