package javabase;

import org.junit.Test;

import java.util.Arrays;

public class PolicyAnalyzerTest {

    @Test
    public void test() {
        System.out.println(PolicyAnalyzer.computeOverlapSegmentsV2(Arrays.asList(new int[]{20250519, 20250519}, new int[]{20250519, 20250519}, new int[]{20250518, 20250520})));
    }

    @Test
    public void testComputePointSegmentOverlap() {
        System.out.println(PolicyAnalyzer.computePointSegmentOverlap(Arrays.asList(
                new int[]{20250519, 20250519},  // 点
                new int[]{20250519, 20250519},  // 点（重复）
                new int[]{20250517, 20250517},  // 点（重复）
                new int[]{20250518, 20250518},  // 点
                new int[]{20250510, 20250519},  // 段
                new int[]{20250510, 20250517}   // 段
        )));
    }

    @Test
    public void testComputeRangeOnlyOverlap() {
        System.out.println(PolicyAnalyzer.computeRangeOnlyOverlap(Arrays.asList(
                new int[]{20250519, 20250519},  // 点
                new int[]{20250519, 20250519},  // 点（重复）
                new int[]{20250518, 20250523},  // 点
                new int[]{20250515, 20250520},  // 段
                new int[]{20250510, 20250519}   // 段
        )));
    }

    @Test
    public void testComputeRangeOnlyPoint() {
        System.out.println(PolicyAnalyzer.computePointOnlyOverlap(Arrays.asList(
                new int[]{20250519, 20250519},  // 点
                new int[]{20250519, 20250519},  // 点（重复）
                new int[]{20250518, 20250523},  // 点
                new int[]{20250515, 20250520},  // 段
                new int[]{20250510, 20250519},   // 段
                new int[]{20250519, 20250519}
        )));
    }
}