package com.inventory.templateservice.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VersionUtilsTest {

    @Test
    @DisplayName("解析版本号")
    void parseVersion() {
        int[] v1 = VersionUtils.parseVersion("1.2.3");
        assertArrayEquals(new int[]{1, 2, 3}, v1);

        int[] v2 = VersionUtils.parseVersion("v2.0.0");
        assertArrayEquals(new int[]{2, 0, 0}, v2);

        int[] v3 = VersionUtils.parseVersion("10.20.30");
        assertArrayEquals(new int[]{10, 20, 30}, v3);

        int[] v4 = VersionUtils.parseVersion(null);
        assertArrayEquals(new int[]{0, 0, 0}, v4);

        int[] v5 = VersionUtils.parseVersion("");
        assertArrayEquals(new int[]{0, 0, 0}, v5);
    }

    @Test
    @DisplayName("格式化版本号")
    void formatVersion() {
        assertEquals("1.2.3", VersionUtils.formatVersion(1, 2, 3));
        assertEquals("0.0.0", VersionUtils.formatVersion(0, 0, 0));
        assertEquals("10.20.30", VersionUtils.formatVersion(10, 20, 30));
    }

    @Test
    @DisplayName("比较版本号")
    void compareVersions() {
        assertTrue(VersionUtils.compareVersions("2.0.0", "1.0.0") > 0);
        assertTrue(VersionUtils.compareVersions("1.0.0", "2.0.0") < 0);
        assertEquals(0, VersionUtils.compareVersions("1.0.0", "1.0.0"));
        assertTrue(VersionUtils.compareVersions("1.10.0", "1.9.0") > 0);
        assertTrue(VersionUtils.compareVersions("1.0.10", "1.0.9") > 0);
    }

    @Test
    @DisplayName("版本号比较方法")
    void versionComparisonMethods() {
        assertTrue(VersionUtils.isGreaterThan("2.0.0", "1.0.0"));
        assertFalse(VersionUtils.isGreaterThan("1.0.0", "2.0.0"));

        assertTrue(VersionUtils.isGreaterThanOrEqual("1.0.0", "1.0.0"));
        assertTrue(VersionUtils.isGreaterThanOrEqual("2.0.0", "1.0.0"));

        assertTrue(VersionUtils.isLessThan("1.0.0", "2.0.0"));
        assertFalse(VersionUtils.isLessThan("2.0.0", "1.0.0"));

        assertTrue(VersionUtils.isLessThanOrEqual("1.0.0", "1.0.0"));
        assertTrue(VersionUtils.isLessThanOrEqual("1.0.0", "2.0.0"));
    }

    @Test
    @DisplayName("版本号递增")
    void versionIncrement() {
        assertEquals("2.0.0", VersionUtils.incrementMajor("1.2.3"));
        assertEquals("1.3.0", VersionUtils.incrementMinor("1.2.3"));
        assertEquals("1.2.4", VersionUtils.incrementPatch("1.2.3"));
    }

    @Test
    @DisplayName("获取下一个版本号")
    void nextVersion() {
        assertEquals("2.0.0", VersionUtils.nextVersion("1.2.3", "major"));
        assertEquals("1.3.0", VersionUtils.nextVersion("1.2.3", "minor"));
        assertEquals("1.2.4", VersionUtils.nextVersion("1.2.3", "patch"));
        assertEquals("1.2.4", VersionUtils.nextVersion("1.2.3", "unknown"));
        assertEquals("1.0.0", VersionUtils.nextVersion(null, "major"));
        assertEquals("1.0.0", VersionUtils.nextVersion("", "major"));
    }

    @Test
    @DisplayName("验证版本号格式")
    void isValidVersion() {
        assertTrue(VersionUtils.isValidVersion("1.0.0"));
        assertTrue(VersionUtils.isValidVersion("v1.0.0"));
        assertTrue(VersionUtils.isValidVersion("10.20.30"));
        assertTrue(VersionUtils.isValidVersion("1.0.0-alpha"));
        assertTrue(VersionUtils.isValidVersion("1.0.0-beta.1"));
        assertFalse(VersionUtils.isValidVersion(null));
        assertFalse(VersionUtils.isValidVersion(""));
        assertFalse(VersionUtils.isValidVersion("1"));
        assertFalse(VersionUtils.isValidVersion("1.0"));
    }

    @Test
    @DisplayName("获取主版本号")
    void getMajorVersion() {
        assertEquals("1", VersionUtils.getMajorVersion("1.2.3"));
        assertEquals("2", VersionUtils.getMajorVersion("2.0.0"));
    }

    @Test
    @DisplayName("获取次版本号")
    void getMinorVersion() {
        assertEquals("1.2", VersionUtils.getMinorVersion("1.2.3"));
        assertEquals("2.0", VersionUtils.getMinorVersion("2.0.0"));
    }
}
