package javabase.gcview;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
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
 * 演示：从GC日志解析时间戳，并绘制一条
 * "X=实际发生时间" vs. "Y=相邻GC间隔(毫秒)" 的时间序列图。
 */
public class GCIntervalTimeSeries {

    // 根据你的GC日志具体格式，调整正则
    // 假设形如：2025-03-16T09:52:04.010+0800
    private static final Pattern TIME_PATTERN = Pattern.compile(
            "(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}\\+\\d{4})"
    );

    // 简化：只解析到毫秒，忽略 +0800
    private static final SimpleDateFormat TIME_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

    /**
     * 解析日志文件，找出每次 GC 的时间（Date对象）。
     */
    public static List<Date> parseGCTimes(String gcLogPath) throws Exception {
        List<Date> gcTimes = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(gcLogPath))) {
            String line;
            while ((line = br.readLine()) != null) {
                Matcher m = TIME_PATTERN.matcher(line);
                if (m.find()) {
                    String timeStr = m.group(1);  // e.g. "2025-03-16T09:52:04.010+0800"
                    // 简单去掉 +0800
                    String[] parts = timeStr.split("\\+");
                    String dtPart = parts[0]; // "2025-03-16T09:52:04.010"

                    try {
                        Date date = TIME_FORMAT.parse(dtPart);
                        gcTimes.add(date);
                    } catch (ParseException e) {
                        // 忽略或打印错误
                    }
                }
            }
        }
        return gcTimes;
    }

    /**
     * 用 JFreeChart 的 TimeSeries 来绘制:
     *   X轴: GC事件发生的时间 (Date)
     *   Y轴: 与上次GC的间隔 (ms)
     */
    public static void showTimeSeries(List<Date> gcTimes) {
        // 创建一个TimeSeries
        TimeSeries series = new TimeSeries("GC Interval (ms)");

        // 计算相邻GC的间隔(毫秒)，并把第i次GC的时间当作 X 坐标
        // interval = gcTimes[i] - gcTimes[i-1]
        for (int i = 1; i < gcTimes.size(); i++) {
            Date current = gcTimes.get(i);
            Date prev = gcTimes.get(i - 1);
            long intervalMs = current.getTime() - prev.getTime();

            // 将 (currentTime, intervalMs) 加入TimeSeries
            // 用 Millisecond 这个类包装Date
            series.addOrUpdate(new Millisecond(current), intervalMs);
        }

        // 放进数据集
        TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries(series);

        // 创建时间序列图
        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                "GC Interval Over Time",
                "Time",
                "Interval (ms)",
                dataset,
                true,  // legend
                true,  // tooltips
                false  // urls
        );

        // 放进一个窗口显示
        ChartFrame frame = new ChartFrame("GC Interval Chart", chart);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
//        String gcLogPath = "/Users/tiaojiheng/Downloads/gc (1).log";
        String gcLogPath = "/Users/tiaojiheng/Downloads/gc (16).log";
        try {
            List<Date> gcTimes = parseGCTimes(gcLogPath);
            if (gcTimes.size() < 2) {
                System.out.println("Not enough GC events found in log.");
                return;
            }
            showTimeSeries(gcTimes);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

