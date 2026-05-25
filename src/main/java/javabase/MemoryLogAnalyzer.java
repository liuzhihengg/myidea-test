package javabase;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MemoryLogAnalyzer {
    public static void main(String[] args) {
        String logFilePath = "/Users/tiaojiheng/study/sshgcc/output.txt";  // 适当更改日志文件的路径
        long totalMmapAllocated = 0;
        long totalMmapFreed = 0;
        long totalSbrkAllocated = 0;
        long totalSbrkFreed = 0;

        Pattern mmapPattern = Pattern.compile("mmap called: .* length=(\\d+).* result=(0x[0-9a-f]+)");
        Pattern munmapPattern = Pattern.compile("munmap called: .* length=(\\d+)");
        Pattern sbrkPattern = Pattern.compile("sbrk called: increment=([+-]?\\d+).* result=(0x[0-9a-f]+)");

        try (BufferedReader reader = Files.newBufferedReader(Paths.get(logFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Matcher mmapMatcher = mmapPattern.matcher(line);
                Matcher munmapMatcher = munmapPattern.matcher(line);
                Matcher sbrkMatcher = sbrkPattern.matcher(line);

                if (mmapMatcher.find()) {
                    long size = Long.parseLong(mmapMatcher.group(1));
                    String result = mmapMatcher.group(2);
                    if (!result.equals("0x0")) { // Assuming 0x0 is the failure case
                        totalMmapAllocated += size;
                    }
                } else if (munmapMatcher.find()) {
                    long size = Long.parseLong(munmapMatcher.group(1));
                    totalMmapFreed += size;
                } else if (sbrkMatcher.find()) {
                    long increment = Long.parseLong(sbrkMatcher.group(1));
                    String result = sbrkMatcher.group(2);
                    if (!result.equals("0x0")) { // Assuming 0x0 is the failure case
                        if (increment > 0) {
                            totalSbrkAllocated += increment;
                        } else {
                            totalSbrkFreed -= increment; // increment is negative, so subtract to add the absolute value
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading the log file: " + e.getMessage());
        }

        System.out.println("Total Memory Allocated via mmap: " + totalMmapAllocated + " bytes");
        System.out.println("Total Memory Freed via mmap: " + totalMmapFreed + " bytes");
        System.out.println("Total Memory Allocated via sbrk: " + totalSbrkAllocated + " bytes");
        System.out.println("Total Memory Freed via sbrk: " + totalSbrkFreed + " bytes");
    }
}

