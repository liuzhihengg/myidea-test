package javabase;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 用法：
 *   java -jar pmap-to-excel.jar /path/pmap.txt /path/out.xlsx
 * 说明：
 *   - 输入是 `pmap -x $PID > pmap.txt` 的文本
 *   - 自动过滤 `(deleted)` 行；Sheet1：按 Mapping 聚合并按 RSS(KB) 降序；Sheet2：去掉 deleted 的原始段明细
 */
public class PmapToExcel {

    // 解析 pmap -x：Address Kbytes RSS Dirty Mode Mapping
    private static final Pattern LINE = Pattern.compile(
            "^([0-9a-fA-F]+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+([rwxsp-]{5})\\s+(.*)$");

    // —— 数据模型（避免与 POI Row 冲突）——
    private static final class Segment {
        String address, mode, mapping;
        long kbytes, rss, dirty;
    }
    private static final class AggMapping {
        String mapping;
        long kbytes, rss, dirty, segments;
    }

    public static void main(String[] args) throws Exception {
        Path in = Path.of("/Users/tiaojiheng/Downloads/pmap.log");
        Path out = Path.of("/Users/tiaojiheng/Downloads/pmap.xlsx");

        // 解析
        List<String> lines = Files.readAllLines(in, StandardCharsets.UTF_8);
        List<Segment> segs = parse(lines);

        long all = 0;
        for (Segment seg : segs) {
            all += seg.rss;
        }
        System.out.println("ALL: " + all);
        // 过滤掉 (deleted)
        List<Segment> segsNoDel = new ArrayList<>(segs.size());
        for (Segment s : segs) {
            if (!s.mapping.endsWith("(deleted)")) segsNoDel.add(s);
        }
        all = 0;
        for (Segment seg : segsNoDel) {
            all += seg.rss;
        }
        System.out.println("ALL: " + all);
        if (segsNoDel.size() > 0) {
            return;
        }


        // 聚合
        Map<String, AggMapping> aggMap = new HashMap<>();
        for (Segment s : segsNoDel) {
            AggMapping a = aggMap.computeIfAbsent(s.mapping, k -> {
                AggMapping x = new AggMapping();
                x.mapping = k;
                return x;
            });
            a.kbytes += s.kbytes;
            a.rss    += s.rss;
            a.dirty  += s.dirty;
            a.segments++;
        }
        List<AggMapping> aggs = new ArrayList<>(aggMap.values());
        aggs.sort(Comparator.comparingLong((AggMapping a) -> a.rss).reversed());

        // 导出（流式）
        try (SXSSFWorkbook wb = new SXSSFWorkbook(1000)) {
            wb.setCompressTempFiles(true);

            // 样式
            CellStyle head = wb.createCellStyle();
            Font bold = wb.createFont(); bold.setBold(true); head.setFont(bold);
            head.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            head.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            head.setBorderBottom(BorderStyle.THIN);

            // Sheet1: 聚合
            Sheet s1 = wb.createSheet("AggByMapping");
            int r = 0;
            r = writeRow(s1, r, head, "#","Mapping","Segments","RSS(KB)","RSS(MB)","Kbytes(KB)","Kbytes(MB)","Dirty(KB)","Dirty(MB)");
            long totR=0, totK=0, totD=0; int idx=1;
            for (AggMapping a : aggs) {
                org.apache.poi.ss.usermodel.Row row = s1.createRow(r++);
                int c=0;
                row.createCell(c++).setCellValue(idx++);
                row.createCell(c++).setCellValue(a.mapping);
                row.createCell(c++).setCellValue(a.segments);
                row.createCell(c++).setCellValue(a.rss);
                row.createCell(c++).setCellValue(kbToMb(a.rss));
                row.createCell(c++).setCellValue(a.kbytes);
                row.createCell(c++).setCellValue(kbToMb(a.kbytes));
                row.createCell(c++).setCellValue(a.dirty);
                row.createCell(c++).setCellValue(kbToMb(a.dirty));
                totR+=a.rss; totK+=a.kbytes; totD+=a.dirty;
            }
            // total
            org.apache.poi.ss.usermodel.Row total = s1.createRow(r++);
            total.createCell(0).setCellValue("TOTAL");
            total.createCell(3).setCellValue(totR);
            total.createCell(4).setCellValue(kbToMb(totR));
            total.createCell(5).setCellValue(totK);
            total.createCell(6).setCellValue(kbToMb(totK));
            total.createCell(7).setCellValue(totD);
            total.createCell(8).setCellValue(kbToMb(totD));
            // 设定列宽（避免 autoSize 处理海量行）
            setWidths(s1, new int[]{6, 60, 10, 12, 10, 14, 10, 12, 10});

            // Sheet2: 明细
            Sheet s2 = wb.createSheet("RawSegments");
            r = 0;
            r = writeRow(s2, r, head, "Address","Kbytes","RSS","Dirty","Mode","Mapping");
            for (Segment x : segsNoDel) {
                org.apache.poi.ss.usermodel.Row row = s2.createRow(r++);
                int c=0;
                row.createCell(c++).setCellValue(x.address);
                row.createCell(c++).setCellValue(x.kbytes);
                row.createCell(c++).setCellValue(x.rss);
                row.createCell(c++).setCellValue(x.dirty);
                row.createCell(c++).setCellValue(x.mode);
                row.createCell(c++).setCellValue(x.mapping);
            }
            setWidths(s2, new int[]{20, 10, 10, 10, 8, 60});

            // 写文件
            try (OutputStream os = Files.newOutputStream(out)) {
                wb.write(os);
            }
            wb.dispose(); // 释放临时文件
        }

        System.out.println("OK → " + out.toAbsolutePath());
    }

    private static List<Segment> parse(List<String> lines) {
        List<Segment> out = new ArrayList<>();
        for (String s : lines) {
            if (s == null) continue;
            s = s.strip();
            if (s.isEmpty()) continue;
            if (s.startsWith("Address") || s.startsWith("total kB") || s.startsWith("---")) continue;

            Matcher m = LINE.matcher(s);
            if (!m.matches()) continue;

            Segment seg = new Segment();
            seg.address = m.group(1);
            seg.kbytes  = parseLong(m.group(2));
            seg.rss     = parseLong(m.group(3));
            seg.dirty   = parseLong(m.group(4));
            seg.mode    = m.group(5);
            seg.mapping = m.group(6);
            out.add(seg);
        }
        return out;
    }

    private static int writeRow(Sheet sh, int r, CellStyle head, String... vals) {
        org.apache.poi.ss.usermodel.Row row = sh.createRow(r++);
        for (int i = 0; i < vals.length; i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(vals[i]);
            cell.setCellStyle(head);
        }
        return r;
    }

    private static void setWidths(Sheet sh, int[] widthsChars) {
        for (int i = 0; i < widthsChars.length; i++) {
            sh.setColumnWidth(i, widthsChars[i] * 256);
        }
    }

    private static long parseLong(String s) {
        try { return Long.parseLong(s); } catch (Exception e) { return 0L; }
    }
    private static double kbToMb(long kb) {
        return Math.round((kb / 1024.0) * 10.0) / 10.0; // 1位小数
    }
}