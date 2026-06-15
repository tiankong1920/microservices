package com.invoice.invoiceservice.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LevenshteinDistanceUtilTest {

    @Test
    void testIdenticalStrings() {
        assertEquals(0, LevenshteinDistanceUtil.calculate("hello", "hello"));
        assertEquals(1.0, LevenshteinDistanceUtil.similarity("hello", "hello"));
    }

    @Test
    void testEmptyStrings() {
        assertEquals(0, LevenshteinDistanceUtil.calculate("", ""));
        assertEquals(1.0, LevenshteinDistanceUtil.similarity("", ""));
    }

    @Test
    void testNullStrings() {
        assertEquals(0, LevenshteinDistanceUtil.calculate(null, null));
        assertEquals(1.0, LevenshteinDistanceUtil.similarity(null, null));
        assertEquals(5, LevenshteinDistanceUtil.calculate("hello", null));
        assertEquals(0.0, LevenshteinDistanceUtil.similarity("hello", null));
    }

    @Test
    void testSingleEdit() {
        assertEquals(1, LevenshteinDistanceUtil.calculate("kitten", "sitten"));
        assertEquals(1, LevenshteinDistanceUtil.calculate("sitten", "sittin"));
    }

    @Test
    void testCompleteDifference() {
        assertEquals(3, LevenshteinDistanceUtil.calculate("abc", "xyz"));
    }

    @Test
    void testChineseStrings() {
        int dist = LevenshteinDistanceUtil.calculate("北京科技有限公司", "北京科技有限公司");
        assertEquals(0, dist);

        double sim = LevenshteinDistanceUtil.similarity("北京科技有限公司", "北京科技有限公");
        assertTrue(sim > 0.85);
    }

    @Test
    void testSimilarityThreshold() {
        double sim = LevenshteinDistanceUtil.similarity("北京科技有限公司", "北京科技有限责任公司");
        assertTrue(sim > 0.7);
    }

    @Test
    void testInsertionAndDeletion() {
        assertEquals(1, LevenshteinDistanceUtil.calculate("abc", "abdc"));
        assertEquals(1, LevenshteinDistanceUtil.calculate("abdc", "abc"));
    }
}
