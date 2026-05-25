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
 * 根据GC日志，统计指定区域(Eden/Survivor/Old)在GC之后的region使用量，随时间变化的曲线
 * 用法：
 *   java GCRegionStatsChart <gc.log> <regionName>
 * 其中 <regionName> 可取 "Eden" "Survivor" 或 "Old"
 */
public class GCRegionStatsChart {

    // 1) 匹配日志时间戳的正则
    //   示例: [2025-03-16T09:52:02.784+0800][info][gc,heap     ] ...
    //   抽取 "2025-03-16T09:52:02.784+0800"
    private static final Pattern TIME_PATTERN = Pattern.compile(
            "(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}\\+\\d{4})"
    );

    // 2) 匹配"区域名 + regions: "并抓取  "before->after(...)"
    //    我们主要关心 "after" 这部分 (\\d+)->(\\d+)(?:\\((\\d+)\\))?
    //    其中第三组 (\\d+) 可选，用于 Eden/Survivor
    //    注意: "Old regions" 没有括号的第三数
    //    这条正则会捕获:
    //       group(1) = before usage
    //       group(2) = after usage
    //       group(3) = optional max/target usage (若有)
    private static final Pattern REGION_PATTERN = Pattern.compile(
            "\\s+(\\d+)->(\\d+)(?:\\((\\d+)\\))?"
    );

    // 将时间解析到毫秒，去掉 +0800
    private static final SimpleDateFormat TIME_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

    /**
     * 解析日志，获取 "指定区域" 的 “GC后region使用量” 随时间。
     * 返回 (time, usageAfterGC) 列表，时间按升序。
     */
    public static List<TimeUsage> parseRegionUsage(String gcLogPath, String regionName) throws Exception {
        List<TimeUsage> data = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(gcLogPath))) {
            String line;
            while ((line = br.readLine()) != null) {
                // 首先必须包含 [gc,heap]，以及 `<regionName> regions:`
                // 例如 "Eden regions:" or "Survivor regions:" or "Old regions:"
                if (!line.contains("[gc,heap")) {
                    continue;
                }
                String searchKey = regionName + " regions:";
                if (!line.contains(searchKey)) {
                    continue;
                }
                // 解析时间
                Matcher tm = TIME_PATTERN.matcher(line);
                if (!tm.find()) {
                    continue;
                }
                String timeStr = tm.group(1); // e.g. 2025-03-16T09:52:02.784+0800
                // 去掉 +0800
                String[] parts = timeStr.split("\\+");
                String dtPart = parts[0]; // e.g. 2025-03-16T09:52:02.784

                Date date = null;
                try {
                    date = TIME_FORMAT.parse(dtPart);
                } catch (ParseException e) {
                    continue; // 如果时间解析失败就跳过
                }

                // 解析region "after" usage
                // 例如 "...Eden regions: 2253->0(853)"
                //       or "...Old regions: 1405->1405"
                int idx = line.indexOf(searchKey);
                if (idx < 0) {
                    continue;
                }
                String segment = line.substring(idx + searchKey.length());
                // e.g. " 2253->0(853)"
                // or " 1405->1405"
                Matcher mm = REGION_PATTERN.matcher(segment);
                if (mm.find()) {
                    // group(1) = before, group(2) = after, group(3)=optional
                    int index = regionName.contains("Old") ? 2 : 3;
                    String afterStr = mm.group(index);
                    int afterUsage = Integer.parseInt(afterStr);

                    data.add(new TimeUsage(date, afterUsage));
                }
            }
        }
        // 按时间排序
        data.sort(Comparator.comparing(t -> t.time));
        return data;
    }

    /**
     * 用 TimeSeries 画 (time -> usage) 的折线图
     */
    public static void showChart(List<TimeUsage> usageList, String regionName) {
        // 构造 TimeSeries
        TimeSeries series = new TimeSeries(regionName + " region usage ");

        for (TimeUsage tu : usageList) {
            // 同一毫秒多次 "gc,heap" 也可能出现 => 用 addOrUpdate 防止 duplicates 错误
            series.addOrUpdate(new Millisecond(tu.time), tu.usage);
        }

        TimeSeriesCollection dataset = new TimeSeriesCollection(series);

        // 创建图表
        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                regionName + " Region数量随时间变化图",
                "时间",
                "Region数量",
                dataset,
                true,
                true,
                false
        );

        // 设置X轴时间格式到毫秒
        XYPlot plot = (XYPlot) chart.getPlot();
        DateAxis domainAxis = (DateAxis) plot.getDomainAxis();
        domainAxis.setDateFormatOverride(new SimpleDateFormat("HH:mm:ss.SSS"));

        // 显示
        ChartFrame frame = new ChartFrame(regionName + " Chart", chart);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        /*
         * 用法:
         *   java GCRegionStatsChart <gc.log> <regionName>
         * regionName 可是 "Eden", "Survivor", or "Old"
         */
//        String gcLogPath = "/Users/tiaojiheng/Downloads/gc (1).log";
        String gcLogPath = "/Users/tiaojiheng/Downloads/gc (16).log";
        String regionName = "Eden"; // "Eden"/"Survivor"/"Old"

        // 解析
        try {
            List<TimeUsage> usageData = parseRegionUsage(gcLogPath, regionName);
            if (usageData.isEmpty()) {
                System.out.println("No matching data for region \"" + regionName + "\" found!");
                return;
            }
            // 画图
            showChart(usageData, regionName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 封装: time + usage
     */
    static class TimeUsage {
        Date time;
        int usage;
        public TimeUsage(Date t, int u) {
            this.time = t;
            this.usage = u;
        }
    }
}

