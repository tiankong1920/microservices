package com.inventory.templateservice.util;

public final class VersionUtils {

    private VersionUtils() {
    }

    public static int[] parseVersion(String version) {
        if (version == null || version.isEmpty()) {
            return new int[]{0, 0, 0};
        }
        
        String normalized = version.startsWith("v") ? version.substring(1) : version;
        String[] parts = normalized.split("\\.");
        
        int[] result = new int[3];
        for (int i = 0; i < Math.min(parts.length, 3); i++) {
            try {
                result[i] = Integer.parseInt(parts[i].replaceAll("[^0-9].*", ""));
            } catch (NumberFormatException e) {
                result[i] = 0;
            }
        }
        
        return result;
    }

    public static String formatVersion(int major, int minor, int patch) {
        return String.format("%d.%d.%d", major, minor, patch);
    }

    public static int compareVersions(String version1, String version2) {
        int[] v1 = parseVersion(version1);
        int[] v2 = parseVersion(version2);
        
        for (int i = 0; i < 3; i++) {
            int compare = Integer.compare(v1[i], v2[i]);
            if (compare != 0) {
                return compare;
            }
        }
        
        return 0;
    }

    public static boolean isGreaterThan(String version1, String version2) {
        return compareVersions(version1, version2) > 0;
    }

    public static boolean isGreaterThanOrEqual(String version1, String version2) {
        return compareVersions(version1, version2) >= 0;
    }

    public static boolean isLessThan(String version1, String version2) {
        return compareVersions(version1, version2) < 0;
    }

    public static boolean isLessThanOrEqual(String version1, String version2) {
        return compareVersions(version1, version2) <= 0;
    }

    public static String incrementMajor(String version) {
        int[] parts = parseVersion(version);
        return formatVersion(parts[0] + 1, 0, 0);
    }

    public static String incrementMinor(String version) {
        int[] parts = parseVersion(version);
        return formatVersion(parts[0], parts[1] + 1, 0);
    }

    public static String incrementPatch(String version) {
        int[] parts = parseVersion(version);
        return formatVersion(parts[0], parts[1], parts[2] + 1);
    }

    public static String nextVersion(String currentVersion, String incrementType) {
        if (currentVersion == null || currentVersion.isEmpty()) {
            return "1.0.0";
        }
        
        return switch (incrementType.toLowerCase()) {
            case "major" -> incrementMajor(currentVersion);
            case "minor" -> incrementMinor(currentVersion);
            case "patch" -> incrementPatch(currentVersion);
            default -> incrementPatch(currentVersion);
        };
    }

    public static boolean isValidVersion(String version) {
        if (version == null || version.isEmpty()) {
            return false;
        }
        return ValidationPatterns.VERSION.matcher(version).matches();
    }

    public static String getMajorVersion(String version) {
        int[] parts = parseVersion(version);
        return String.valueOf(parts[0]);
    }

    public static String getMinorVersion(String version) {
        int[] parts = parseVersion(version);
        return parts[0] + "." + parts[1];
    }
}
