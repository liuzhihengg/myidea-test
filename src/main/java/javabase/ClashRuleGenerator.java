package javabase;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClashRuleGenerator {

    public static void main(String[] args) {
        String inputFile = "input.txt"; // 输入文件路径
        String outputFile = "output.txt"; // 输出文件路径

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {

            String line;
            Pattern pattern = Pattern.compile("((?:\\d{1,3}\\.){3}\\d{1,3})\\s+([\\w.-]+)");

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.startsWith("#") && !line.isEmpty()) {
                    Matcher matcher = pattern.matcher(line);
                    if (matcher.matches()) {
                        String domain = matcher.group(2);
                        writer.write("- DOMAIN," + domain + ",DIRECT");
                        writer.newLine();
                    }
                }
            }

            System.out.println("规则生成完毕，输出文件: " + outputFile);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
