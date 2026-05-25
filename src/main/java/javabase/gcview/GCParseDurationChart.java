package javabase.gcview;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import java.io.BufferedReader;
import java.io.FileReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 示例：从 GC 日志中解析每次 "Pause" 的总时间(如 315.729ms)，
 *       并在 JFreeChart 中绘制 X=发生时间, Y=GC耗时(ms) 的折线图。
 *       同时过滤掉耗时 > 1000ms(1s) 的点。
 *
 * 用法:
 *   java GCParseDurationChart <gc.log>
 */
public class GCParseDurationChart {

    /**
     * 匹配形如 "2025-03-16T09:52:04.325+0800" 的时间戳
     */
    private static final Pattern TIME_PATTERN = Pattern.compile(
            "(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}\\+\\d{4})"
    );

    /**
     * 匹配 GC 耗时(毫秒) 形如 "315.729ms", "229.791ms", "1500.123ms" 等
     *  - group(1) = 耗时(小数)
     */
    private static final Pattern DURATION_PATTERN = Pattern.compile(
            "\\s+(\\d+(?:\\.\\d+))ms"
    );

    // 简化，把 +0800 去掉，再用这个格式解析
    private static final SimpleDateFormat TIME_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

    /**
     * 解析 GC 日志，找到含 "Pause" 的行，然后提取 (time, durationMs)，
     * 并过滤掉 duration > 1000ms 的记录。
     */
    public static List<GCEvent> parseGCDurations(String gcLogFile) throws Exception {
        List<GCEvent> results = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(gcLogFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                // 只要含 "Pause" 关键字的行（你可自行加 "Young" / "Normal" / "Evacuation"等判断）
                if (!line.contains("Pause")) {
                    continue;
                }

                // 匹配时间戳
                Matcher tm = TIME_PATTERN.matcher(line);
                if (!tm.find()) {
                    continue;
                }
                String timeStr = tm.group(1); // e.g. "2025-03-16T09:52:04.325+0800"
                // 去掉 +0800
                String[] parts = timeStr.split("\\+");
                String dtPart = parts[0]; // "2025-03-16T09:52:04.325"
                Date date = null;
                try {
                    date = TIME_FORMAT.parse(dtPart);
                } catch (ParseException e) {
                    continue;
                }

                // 匹配 GC 耗时
                Matcher dm = DURATION_PATTERN.matcher(line);
                if (!dm.find()) {
                    continue; // 如果找不到形如 "315.729ms" 就跳过
                }
                String durStr = dm.group(1); // e.g. "315.729"
                double durValue = Double.parseDouble(durStr); // 315.729

                // 如果duration > 1000ms, skip
                if (durValue > 1000.0) {
                    continue;
                }

                results.add(new GCEvent(date, durValue));
            }
        }
        // 按时间排序
        results.sort(Comparator.comparing(g -> g.time));
        return results;
    }

    /**
     * 用JFreeChart画 X=时间, Y=duration 的折线图
     */
    public static void showChart(List<GCEvent> gcData) {
        TimeSeries series = new TimeSeries("GC时间随次数的变化 (ms)");

        for (GCEvent event : gcData) {
            // 避免 duplicates not permitted
            series.addOrUpdate(new Millisecond(event.time), event.duration);
        }

        TimeSeriesCollection dataset = new TimeSeriesCollection(series);

        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                "GC消耗时间的变化图",
                "时间",
                "GC时间（ms）",
                dataset,
                true,
                true,
                false
        );

        // 设置x轴格式
        XYPlot plot = (XYPlot) chart.getPlot();
        DateAxis domainAxis = (DateAxis) plot.getDomainAxis();
        domainAxis.setDateFormatOverride(new SimpleDateFormat("HH:mm:ss.SSS"));

        ChartFrame frame = new ChartFrame("GC Duration Chart", chart);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
//        String gcLogPath = "/Users/tiaojiheng/Downloads/gc (1).log";
        String gcLogPath = "/Users/tiaojiheng/Downloads/gc (16).log";
        try {
            List<GCEvent> data = parseGCDurations(gcLogPath);
            if (data.isEmpty()) {
                System.out.println("No GC event or all >1s in the log?");
                return;
            }
            showChart(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static class GCEvent {
        Date time;
        double duration; // in ms
        public GCEvent(Date t, double d) {
            this.time = t;
            this.duration = d;
        }
    }
}

