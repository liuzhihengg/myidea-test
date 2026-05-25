package javabase;

import java.io.*;
import java.util.*;
import java.util.regex.*;

public class LogAnalyzerGPT {

    public static void main(String[] args) {
        String logFilePath = "/Users/tiaojiheng/Downloads/search.2025-03-06-13.log"; // 替换为实际日志文件路径
        Map<String, Integer> errorCountMap = new HashMap<>();

        // 正则表达式，匹配 "ERROR" 日志中的标题部分
        String errorPattern = "\\[ERROR\\].*?\\] .*?-.*?\\s([a-zA-Z0-9_\\s]+(?:\\s+[a-zA-Z0-9_]+)*)\\s*$";

        try (BufferedReader br = new BufferedReader(new FileReader(logFilePath))) {
            String line;
            Pattern pattern = Pattern.compile(errorPattern);
            while ((line = br.readLine()) != null) {
                Matcher matcher = pattern.matcher(line);
                if (matcher.find()) {
                    String errorMessage = matcher.group(1); // 获取错误标题
                    errorCountMap.put(errorMessage, errorCountMap.getOrDefault(errorMessage, 0) + 1);
                }
            }

            // 按出现次数降序排序
            List<Map.Entry<String, Integer>> sortedList = new ArrayList<>(errorCountMap.entrySet());
            sortedList.sort((entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue())); // 按数量降序

            // 输出结果
            System.out.println("Error Message Titles and their Count:");
            for (Map.Entry<String, Integer> entry : sortedList) {
                System.out.println(entry.getKey() + ": " + entry.getValue());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
