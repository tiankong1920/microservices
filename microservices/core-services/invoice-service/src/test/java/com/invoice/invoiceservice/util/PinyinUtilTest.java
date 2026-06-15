package com.invoice.invoiceservice.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PinyinUtilTest {

    private PinyinUtil pinyinUtil;

    @BeforeEach
    void setUp() {
        pinyinUtil = new PinyinUtil();
    }

    @Test
    void testGetInitials() {
        String initials = pinyinUtil.getInitials("北京科技有限公司");
        assertNotNull(initials);
        assertTrue(initials.contains("b"));
    }

    @Test
    void testGetInitialsEmpty() {
        assertEquals("", pinyinUtil.getInitials(""));
        assertEquals("", pinyinUtil.getInitials(null));
    }

    @Test
    void testGetFullPinyin() {
        String fullPinyin = pinyinUtil.getFullPinyin("北京");
        assertNotNull(fullPinyin);
        assertTrue(fullPinyin.contains("bei"));
        assertTrue(fullPinyin.contains("jing"));
    }

    @Test
    void testGetFullPinyinEmpty() {
        assertEquals("", pinyinUtil.getFullPinyin(""));
        assertEquals("", pinyinUtil.getFullPinyin(null));
    }

    @Test
    void testGetInitialsWithEnglish() {
        String initials = pinyinUtil.getInitials("ABC公司");
        assertNotNull(initials);
        assertTrue(initials.startsWith("abc"));
    }

    @Test
    void testGetAllPossibleInitials() {
        var results = pinyinUtil.getAllPossibleInitials("重庆");
        assertNotNull(results);
        assertFalse(results.isEmpty());
    }

    @Test
    void testGetAllPossibleInitialsEmpty() {
        var results = pinyinUtil.getAllPossibleInitials("");
        assertTrue(results.isEmpty());
    }

    @Test
    void testSingleCharacter() {
        String initials = pinyinUtil.getInitials("中");
        assertEquals("z", initials);
    }
}
