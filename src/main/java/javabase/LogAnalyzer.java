package javabase;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogAnalyzer {

    public static void main(String[] args) {
        String logFilePath = "/Users/tiaojiheng/Downloads/search.2025-03-06-13.log"; // 替换为你的日志文件路径
        Map<String, Integer> errorCountMap = new HashMap<>();

        // 正则表达式匹配ERROR日志行并提取标题
        Pattern errorPattern = Pattern.compile("\\[\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2} ERROR .*?\\] .*?\\] (.*)");

        try (BufferedReader br = new BufferedReader(new FileReader(logFilePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                Matcher matcher = errorPattern.matcher(line);
                if (matcher.find()) {
                    String errorTitle = matcher.group(1).trim();
                    errorCountMap.put(errorTitle, errorCountMap.getOrDefault(errorTitle, 0) + 1);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 将Map转换为List并按值排序
        List<Map.Entry<String, Integer>> sortedEntries = new ArrayList<>(errorCountMap.entrySet());
        sortedEntries.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));

        // 输出排序后的结果
        for (Map.Entry<String, Integer> entry : sortedEntries) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
    }
}
