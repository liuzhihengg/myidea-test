package javabase;

import org.apache.commons.collections.CollectionUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class PolicyAnalyzer {
    public static void main(String[] args) throws Exception {
        // 读取 XML 文件
        String basePath = "/Users/tiaojiheng/Downloads/";
        String fileName = "0-2.xml";
        File xmlFile = new File(basePath + fileName);

        // 初始化 DOM 解析器
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(xmlFile);
        doc.getDocumentElement().normalize();

        // 获取所有 <Policy>
        NodeList policyNodes = doc.getElementsByTagName("Policy");

        // key1 -> Set<key2>
        Map<String, Set<String>> policyMap = new HashMap<>();
        Map<String, List<int[]>> countMap = new HashMap<>();

        for (int i = 0; i < policyNodes.getLength(); i++) {
            Element policy = (Element) policyNodes.item(i);

            String dpt = policy.getAttribute("dpt");
            String arr = policy.getAttribute("arr");
            String dptDateStart = policy.getAttribute("dptDateStart");
            String dptDateEnd = policy.getAttribute("dptDateEnd");

            if (dpt.isEmpty() || arr.isEmpty() || dptDateStart.isEmpty() || dptDateEnd.isEmpty()) {
                continue; // skip invalid
            }

            String key1 = dpt + "_" + arr;
            String key2 = dptDateStart + "_" + dptDateEnd;


            policyMap.computeIfAbsent(key1, k -> new HashSet<>()).add(key2);
            countMap.computeIfAbsent(key1, k -> new ArrayList<>()).add(new int[]{parseDate(dptDateStart), parseDate(dptDateEnd)});
        }

        // 打印统计结果
        int allCount = 0;
        for (Map.Entry<String, Set<String>> entry : policyMap.entrySet()) {
            allCount += entry.getValue().size();
//            System.out.println("key1: " + entry.getKey() + " -> 有 " + entry.getValue().size() + " 个 key2");
            List<int[]> ints = countMap.get(entry.getKey());
            List<OverlapSegment> overlapSegments = computePointOnlyOverlap(ints);
            if (CollectionUtils.isNotEmpty(overlapSegments)) {
                System.out.println("key1: " + entry.getKey() + " -> 有 " + overlapSegments);
            }
        }
        System.out.println("多少条政策: " + policyNodes.getLength());
        System.out.println("多少个不同的起飞到达: " + policyMap.size());
        System.out.println("平均每个起飞到达有多少不同的: " + allCount / policyMap.size());
    }

    private static int parseDate(String date) {
        return Integer.parseInt(date.replace("-", ""));
    }

    static class OverlapSegment {
        int start, end, count;
        OverlapSegment(int start, int end, int count) {
            this.start = start;
            this.end = end;
            this.count = count;
        }

        @Override
        public String toString() {
            return String.valueOf(count);
        }
    }

    public static List<OverlapSegment> computeOverlapSegments(List<int[]> intervals) {
        TreeMap<Integer, Integer> sweep = new TreeMap<>();
        for (int[] interval : deduplicateSegments(intervals)) {
            sweep.put(interval[0], sweep.getOrDefault(interval[0], 0) + 1); // start +1
            sweep.put(interval[1], sweep.getOrDefault(interval[1], 0) - 1); // end -1
        }

        List<OverlapSegment> result = new ArrayList<>();
        int prevTime = -1, currentCount = 0;

        for (Map.Entry<Integer, Integer> entry : sweep.entrySet()) {
            int time = entry.getKey();
            if (prevTime != -1 && currentCount > 0) {
                result.add(new OverlapSegment(prevTime, time, currentCount));
            }
            currentCount += entry.getValue();
            prevTime = time;
        }

        result.sort((a, b) -> Integer.compare(b.count, a.count));
        return result;
    }

    static class Event implements Comparable<Event> {
        int time;
        int delta; // 1 = 开始, -1 = 结束

        public Event(int time, int delta) {
            this.time = time;
            this.delta = delta;
        }

        public int compareTo(Event o) {
            if (this.time != o.time) return Integer.compare(this.time, o.time);
            return Integer.compare(this.delta, o.delta); // 结束先于开始
        }
    }

    public static List<OverlapSegment> computeOverlapSegmentsV2(List<int[]> intervals) {
        List<Event> events = new ArrayList<>();

        for (int[] interval : intervals) {
            int start = interval[0];
            int end = interval[1];
            events.add(new Event(start, 1));       // start 加1
            events.add(new Event(end + 1, -1));    // end+1 减1（闭区间处理关键点）
        }

        Collections.sort(events);

        List<OverlapSegment> result = new ArrayList<>();
        int active = 0;
        Integer prev = null;

        for (Event e : events) {
            if (prev != null && prev < e.time && active > 0) {
                result.add(new OverlapSegment(prev, e.time - 1, active));  // 注意 e.time-1 是闭区间尾
            }
            active += e.delta;
            prev = e.time;
        }

        // 按重叠层数排序
        result.sort((a, b) -> Integer.compare(b.count, a.count));
        return result;
    }

    public static List<OverlapSegment> computeRangeOnlyOverlap(List<int[]> intervals) {
        List<Event> events = new ArrayList<>();

        for (int[] interval : deduplicateSegments(intervals)) {
            if (interval[0] == interval[1]) continue; // 忽略点
            events.add(new Event(interval[0], 1));
            events.add(new Event(interval[1] + 1, -1));
        }

        Collections.sort(events);
        List<OverlapSegment> result = new ArrayList<>();
        int active = 0;
        Integer prev = null;

        for (Event e : events) {
            if (prev != null && prev < e.time && active > 0) {
                result.add(new OverlapSegment(prev, e.time - 1, active));
            }
            active += e.delta;
            prev = e.time;
        }

        result.sort((a, b) -> Integer.compare(b.count, a.count));
        return result;
    }

    public static List<OverlapSegment> computePointSegmentOverlap(List<int[]> intervals) {
        List<int[]> points = new ArrayList<>();
        List<int[]> segments = new ArrayList<>();

        // 拆分为点和段
        for (int[] arr : deduplicateSegments(intervals)) {
            if (arr[0] == arr[1]) {
                points.add(arr);
            } else if (arr[0] < arr[1]) {
                segments.add(arr);
            }
        }

        // 提取唯一点（去重）
        Set<Integer> uniquePoints = points.stream()
                .map(p -> p[0])
                .collect(Collectors.toSet());

        List<OverlapSegment> result = new ArrayList<>();

        // 对每个段统计重叠点数
        for (int[] seg : segments) {
            int start = seg[0], end = seg[1];
            int count = 0;
            for (int point : uniquePoints) {
                if (point >= start && point <= end) {
                    count++;
                }
            }
            if (count > 0) {
                result.add(new OverlapSegment(start, end, count));
            }
        }

        // 按重叠点数量从高到低排序
        result.sort((a, b) -> Integer.compare(b.count, a.count));
        return result;
    }

    public static List<OverlapSegment> computePointOnlyOverlap(List<int[]> points) {
        Map<Integer, Integer> countMap = new HashMap<>();
        for (int[] point : deduplicateSegments(points)) {
            if (point[0] == point[1]) {
                countMap.put(point[0], countMap.getOrDefault(point[0], 0) + 1);
            }
        }

        List<OverlapSegment> result = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : countMap.entrySet()) {
            if (entry.getValue() > 1) {
                result.add(new OverlapSegment(entry.getKey(), entry.getKey(), entry.getValue()));
            }
        }
        result.sort((a, b) -> Integer.compare(b.count, a.count));
        return result;
    }

    public static List<int[]> deduplicateSegments(List<int[]> intervals) {
        Set<String> seen = new HashSet<>();
        List<int[]> result = new ArrayList<>();

        for (int[] interval : intervals) {
            int start = interval[0], end = interval[1];
            if (start < end) { // 只处理成段
                String key = start + "-" + end;
                if (seen.add(key)) {
                    result.add(new int[]{start, end});
                }
            }
        }
        return result;
    }
}
