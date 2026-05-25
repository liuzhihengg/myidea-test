package javabase.gcview;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 示例：从GC日志里解析 "GC(9150)" 里的9150作为X，
 *       解析耗时 315.729ms 作为Y，
 *       过滤掉>1秒(1000ms)，画 X=GC序号 vs Y=耗时 的折线图(不随自然时间)。
 *
 * 用法:
 *   java GCIndexDurationChart <gc.log>
 */
public class GCIndexDurationChart {

    /**
     * 匹配 "GC(9150)" => group(1) = 9150
     */
    private static final Pattern GC_INDEX_PATTERN = Pattern.compile("GC\\((\\d+)\\)");

    /**
     * 匹配 "315.729ms" => group(1)=315.729
     */
    private static final Pattern DURATION_PATTERN = Pattern.compile("\\s+(\\d+(?:\\.\\d+))ms");

    /**
     * 只扫描含 "Pause" (或你想要的关键字) 的行，
     * 解析 GC index + duration ms。
     * 过滤 >1000ms，然后返回列表。
     */
    public static List<GCIndexEvent> parseGCIndexDurations(String gcLogFile) throws Exception {
        List<GCIndexEvent> results = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(gcLogFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                // 只要含 "Pause" (你可改成只要 "Pause Young"等)
                if (!line.contains("Pause")) {
                    continue;
                }

                // 解析 GC(...) => GC index
                Matcher idxMatcher = GC_INDEX_PATTERN.matcher(line);
                if (!idxMatcher.find()) {
                    continue; // 找不到 "GC(...)"
                }
                int gcIndex;
                try {
                    gcIndex = Integer.parseInt(idxMatcher.group(1));
                } catch (NumberFormatException e) {
                    continue;
                }

                // 解析 duration => "xxx.xxxxms"
                Matcher durMatcher = DURATION_PATTERN.matcher(line);
                if (!durMatcher.find()) {
                    continue; // 没找到耗时
                }
                double durationMs = Double.parseDouble(durMatcher.group(1));
                if (durationMs > 1000.0) {
                    // 忽略超过1s
                    continue;
                }

                // 收集
                results.add(new GCIndexEvent(gcIndex, durationMs));
            }
        }
        // 有时想按 GC index 排序
        results.sort(Comparator.comparingInt(e -> e.gcIndex));
        return results;
    }

    /**
     * 使用 XYSeries 画 X=gcIndex, Y=duration
     */
    public static void showChart(List<GCIndexEvent> data) {
        XYSeries series = new XYSeries("GC Duration (<1s)");

        for (GCIndexEvent ev : data) {
            // 这里 X=ev.gcIndex, Y=ev.durationMs
            // XYSeries 允许重复X，但后加会覆盖。一般不会有重复 index
            series.add(ev.gcIndex, ev.durationMs);
        }

        XYSeriesCollection dataset = new XYSeriesCollection(series);

        // 使用 createXYLineChart
        JFreeChart chart = ChartFactory.createXYLineChart(
                "GC Duration by GC Index (under 1s)",
                "GC Index",
                "Duration (ms)",
                dataset
        );

        // 显示在一个窗口
        ChartFrame frame = new ChartFrame("GC Duration Chart", chart);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
//        String gcLogPath = "/Users/tiaojiheng/Downloads/gc (1).log";
        String gcLogPath = "/Users/tiaojiheng/Downloads/gc (16).log";
        try {
            List<GCIndexEvent> data = parseGCIndexDurations(gcLogPath);
            if (data.isEmpty()) {
                System.out.println("No GC events (under 1s) found in log?");
                return;
            }
            showChart(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * GCIndexEvent: x=gcIndex, y=durationMs
     */
    static class GCIndexEvent {
        int gcIndex;
        double durationMs;
        public GCIndexEvent(int idx, double dur) {
            this.gcIndex = idx;
            this.durationMs = dur;
        }
    }
}

