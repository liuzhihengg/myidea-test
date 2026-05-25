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
 * 演示：横坐标是实际时间(到毫秒)，
 *      纵坐标是在该时刻前后 0.5秒内 发生的 GC 事件数。
 */
public class GCWindowCountChart {

    // 这里假设 GC 日志包含形如 "2025-03-16T09:52:04.010+0800" 的时间戳。
    private static final Pattern TIME_PATTERN = Pattern.compile(
            "(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}\\+\\d{4})"
    );

    // 忽略 +0800，简单处理成 yyyy-MM-dd'T'HH:mm:ss.SSS
    private static final SimpleDateFormat TIME_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

    /**
     * 从GC日志中解析所有 GC 时间，返回List<Date>，并按时间排序。
     */
    public static List<Date> parseGCTimes(String gcLogFile) throws Exception {
        List<Date> gcTimes = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(gcLogFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                // 1) 检查是否含有 "gc,start"
                if (!line.contains("gc,start")) {
                    // 跳过
                    continue;
                }
                Matcher m = TIME_PATTERN.matcher(line);
                if (m.find()) {
                    String timeStr = m.group(1);
                    // 切掉 +0800
                    String[] parts = timeStr.split("\\+");
                    String dtPart = parts[0]; // e.g. "2025-03-16T09:52:04.010"
                    try {
                        Date date = TIME_FORMAT.parse(dtPart);
                        gcTimes.add(date);
                    } catch (ParseException e) {
                        // log or ignore
                    }
                }
            }
        }
        // 按时间排序
        gcTimes.sort(Comparator.naturalOrder());
        return gcTimes;
    }

    /**
     * 对于每个 GC 时间 t_i，计算 [t_i - 500ms, t_i + 500ms] 区间内有多少 GC 事件。
     * 返回一个 (time, count) 列表。
     *
     * 实现思路：双指针
     */
    public static List<TimeCount> computeWindowCounts(List<Date> gcTimes) {
        List<TimeCount> results = new ArrayList<>();
        if (gcTimes.isEmpty()) return results;

        final long window = 500; // half window (ms)
        // 两个指针 j, k 用于圈定 [t_i - 500ms, t_i + 500ms] 范围
        int n = gcTimes.size();
        int j = 0;
        int k = 0;

        for (int i = 0; i < n; i++) {
            Date current = gcTimes.get(i);
            long tCur = current.getTime();

            long start = tCur - window;
            long end   = tCur + window;

            // 移动 j 使得 gcTimes[j] >= start
            while (j < n && gcTimes.get(j).getTime() < start) {
                j++;
            }
            // 移动 k 使得 gcTimes[k] <= end
            // 这里我们先保证 k >= i, 也可以不做，但为了效率我们不会回退k
            while (k < n && gcTimes.get(k).getTime() <= end) {
                k++;
            }
            // 此时 [j, k-1] 都在区间内
            int count = k - j;
            results.add(new TimeCount(current, count));
        }
        return results;
    }

    /**
     * 将 (time, count) 数据绘制成 TimeSeries 图：
     *    X = time (毫秒精度)
     *    Y = count
     */
    public static void showChart(List<TimeCount> dataPoints) {
        // 用 TimeSeries + Millisecond 表示毫秒精度
        TimeSeries series = new TimeSeries("GC freq (±0.5s)");

        for (TimeCount tc : dataPoints) {
            Millisecond ms = new Millisecond(tc.time);
            // 用 addOrUpdate 避免 "duplicate time period" 异常
            series.addOrUpdate(ms, tc.count);
        }

        TimeSeriesCollection dataset = new TimeSeriesCollection(series);

        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                "GC频率变化图",
                "时间",
                "每秒GC次数（默认为1，方便展示问题",
                dataset,
                true,
                true,
                false
        );

        // 设置X轴的日期格式到毫秒
        XYPlot plot = (XYPlot) chart.getPlot();
        DateAxis domainAxis = (DateAxis) plot.getDomainAxis();
        domainAxis.setDateFormatOverride(new SimpleDateFormat("HH:mm:ss.SSS"));

        ChartFrame frame = new ChartFrame("GC Window Chart", chart);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public static void showChartWithDuan(List<TimeCount> dataPoints) {
        TimeSeries series = new TimeSeries("GC freq (±0.5s)");

        // 将时间点统一转换为秒级时间戳（向下取整），统计频次
        Map<Long, Integer> timeToCount = new HashMap<>();
        long min = Long.MAX_VALUE, max = Long.MIN_VALUE;
        for (TimeCount tc : dataPoints) {
            long sec = tc.time.getTime() / 1000;
            timeToCount.put(sec, timeToCount.getOrDefault(sec, 0) + tc.count);
            min = Math.min(min, sec);
            max = Math.max(max, sec);
        }

        // 补齐每秒的数据
        for (long sec = min; sec <= max; sec++) {
            int count = timeToCount.getOrDefault(sec, 0);
            // 构造该秒钟的起始时间（00毫秒）
            Millisecond ms = new Millisecond(new Date(sec * 1000));
            series.addOrUpdate(ms, count);
        }

        TimeSeriesCollection dataset = new TimeSeriesCollection(series);
        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                "GC频率变化图",
                "Time",
                "每秒GC次数",
                dataset,
                true,
                true,
                false
        );

        XYPlot plot = (XYPlot) chart.getPlot();
        DateAxis domainAxis = (DateAxis) plot.getDomainAxis();
        domainAxis.setDateFormatOverride(new SimpleDateFormat("HH:mm:ss"));

        ChartFrame frame = new ChartFrame("GC Window Chart", chart);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
//        String gcLogPath = "/Users/tiaojiheng/Downloads/gc (1).log";
        String gcLogPath = "/Users/tiaojiheng/Downloads/gc (14).log";

        try {
            // 1) 解析 GC 时间
            List<Date> gcTimes = parseGCTimes(gcLogPath);
            if (gcTimes.size() == 0) {
                System.out.println("No GC events found in log.");
                return;
            }
            // 2) 对于每个GC事件，计算 [t-0.5s, t+0.5s] 内有多少事件
            List<TimeCount> freqData = computeWindowCounts(gcTimes);
            // 3) 画图
            showChartWithDuan(freqData);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 简单封装：在某个 time 发生时，对应 count 次 GC（在滑窗内）。
     */
    static class TimeCount {
        Date time;
        int count;
        public TimeCount(Date time, int count) {
            this.time = time;
            this.count = count;
        }
    }
}

