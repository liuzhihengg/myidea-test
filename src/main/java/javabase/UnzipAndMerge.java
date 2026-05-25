package javabase;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.xz.XZCompressorInputStream;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class UnzipAndMerge {

    public static void main(String[] args) {
//        if (args.length < 2) {
//            System.out.println("Usage: java UnzipAndMerge <directory> <prefix>");
//            return;
//        }

        String directory = "/Users/tiaojiheng/去哪儿/问题排查/中转搜索单机与cpu问题/heap文件";
        String prefix = "dump.hprof";

        try {
            List<File> files = getSortedFiles(directory, prefix);
            if (files.isEmpty()) {
                System.out.println("No matching files found.");
                return;
            }

            // 解压每个文件
            for (File file : files) {
                new Thread(() -> {
                    try {
                        decompress(file, directory);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).start();
            }

            // 等待所有线程完成解压
            Thread.sleep(5000); // 调整这个时间来确保解压完成（或者改用更精确的线程同步机制）

            // 合并文件
            mergeFiles(directory, prefix);

            System.out.println("Extraction and merging completed.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 获取并按顺序排序分割文件
    private static List<File> getSortedFiles(String directory, String prefix) {
        File dir = new File(directory);
        File[] files = dir.listFiles((dir1, name) -> name.matches(prefix + "_part_\\d{2}\\.tar\\.xz"));
        if (files == null) return Collections.emptyList();

        // 按照文件名排序
        Arrays.sort(files, Comparator.comparing(File::getName));
        return Arrays.asList(files);
    }

    // 解压 tar.xz 文件
    private static void decompress(File file, String destination) throws IOException {
        System.out.println("Decompressing: " + file.getName());

        try (BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file));
             XZCompressorInputStream xzInputStream = new XZCompressorInputStream(inputStream);
             TarArchiveInputStream tarInputStream = new TarArchiveInputStream(xzInputStream)) {

            TarArchiveEntry entry;
            while ((entry = tarInputStream.getNextTarEntry()) != null) {
                File outputFile = new File(destination, entry.getName());
                if (entry.isDirectory()) {
                    outputFile.mkdirs();
                } else {
                    try (BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outputFile))) {
                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = tarInputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                        }
                    }
                }
            }
        }

        System.out.println("Decompression completed: " + file.getName());
    }

    // 合并文件
    private static void mergeFiles(String directory, String prefix) throws IOException {
        int index = 0;
        File mergedFile = new File(directory, prefix + "_merged");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(mergedFile))) {
            while (true) {
                String partFileName = String.format("%s/%s_part_%02d", directory, prefix, index);
                File partFile = new File(partFileName);

                if (!partFile.exists()) {
                    break;
                }

                // 合并文件
                try (BufferedReader reader = new BufferedReader(new FileReader(partFile))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        writer.write(line);
                        writer.newLine();
                    }
                }

                System.out.println("Merged: " + partFileName);
                index++;
            }
        }

        System.out.println("Merging completed. Output file: " + mergedFile.getAbsolutePath());
    }
}
