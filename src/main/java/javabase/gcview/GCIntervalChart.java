package javabase.gcview;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.io.BufferedReader;
import java.io.FileReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Date;

/**
 * 示例：读取 GC 日志，提取相邻 GC 间隔并绘制折线图
 */
public class GCIntervalChart {

    // 根据你的日志实际格式，调整此正则。
    // 示例：匹配形如 "2025-03-16T09:52:04.010+0800" 的时间戳
    private static final Pattern TIME_PATTERN = Pattern.compile(
            "(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}\\+\\d{4})"
    );

    // 根据你的日志具体格式，可以需修改此 parse 方法
    // 这里简单演示只取到 "2025-03-16T09:52:04.010"，忽略时区
    private static final SimpleDateFormat TIME_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

    /**
     * 从GC日志解析每次GC事件发生的时间（Date对象）
     */
    public static List<Date> parseGCTimes(String gcLogPath) throws Exception {
        List<Date> gcTimes = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(gcLogPath))) {
            String line;
            while ((line = br.readLine()) != null) {
                // 1) 检查是否含有 "gc,start"
                if (!line.contains("gc,start")) {
                    // 跳过
                    continue;
                }
                Matcher m = TIME_PATTERN.matcher(line);
                if (m.find()) {
                    String timeStr = m.group(1); // e.g. "2025-03-16T09:52:04.010+0800"
                    // 简单去掉 +0800 部分：
                    String[] parts = timeStr.split("\\+");
                    String dtPart = parts[0]; // "2025-03-16T09:52:04.010"

                    try {
                        Date date = TIME_FORMAT.parse(dtPart);
                        gcTimes.add(date);
                    } catch (ParseException e) {
                        // 如果格式不匹配，可根据需要处理
                    }
                }
            }
        }
        return gcTimes;
    }

    /**
     * 计算相邻两次GC之间的间隔（秒）
     */
    public static List<Double> computeIntervals(List<Date> gcTimes) {
        List<Double> intervals = new ArrayList<>();
        for (int i = 0; i < gcTimes.size() - 1; i++) {
            Date t1 = gcTimes.get(i);
            Date t2 = gcTimes.get(i + 1);
            double diffSec = (t2.getTime() - t1.getTime()) / 1000.0;
            intervals.add(diffSec);
        }
        return intervals;
    }

    /**
     * 使用 JFreeChart 画一条折线图：X轴为GC事件序号，Y轴为间隔秒数
     */
    public static void showChart(List<Double> intervals) {
        // 创建一个XYSeries
        XYSeries series = new XYSeries("GC Intervals (sec)");

        // X轴从1开始，Y轴是interval值
        for (int i = 0; i < intervals.size(); i++) {
            series.add(i + 8908, intervals.get(i));
        }

        // 放进数据集
        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(series);

        // 使用 JFreeChart 工厂方法创建一个简单的折线图
        JFreeChart chart = ChartFactory.createXYLineChart(
                "GC Intervals Over Time",
                "GC event index",
                "Interval (seconds)",
                dataset
        );

        // 用 ChartFrame 显示
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
                System.out.println("Not enough GC events found.");
                return;
            }

            // 计算相邻 GC 时间差
            List<Double> intervals = computeIntervals(gcTimes);
            // 显示图表
            showChart(intervals);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

