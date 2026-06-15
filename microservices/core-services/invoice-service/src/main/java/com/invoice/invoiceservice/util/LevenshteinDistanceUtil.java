package com.invoice.invoiceservice.util;

public class LevenshteinDistanceUtil {

    private LevenshteinDistanceUtil() {
    }

    public static int calculate(String s1, String s2) {
        if (s1 == null || s2 == null) {
            return Math.max(
                    s1 == null ? 0 : s1.length(),
                    s2 == null ? 0 : s2.length()
            );
        }
        int len1 = s1.length();
        int len2 = s2.length();
        int[][] dp = new int[len1 + 1][len2 + 1];

        for (int i = 0; i <= len1; i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= len2; j++) {
            dp[0][j] = j;
        }

        for (int i = 1; i <= len1; i++) {
            for (int j = 1; j <= len2; j++) {
                int cost = (s1.charAt(i - 1) == s2.charAt(j - 1)) ? 0 : 1;
                dp[i][j] = Math.min(
                        Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                        dp[i - 1][j - 1] + cost
                );
            }
        }
        return dp[len1][len2];
    }

    public static double similarity(String s1, String s2) {
        if (s1 == null && s2 == null) {
            return 1.0;
        }
        if (s1 == null || s2 == null) {
            return 0.0;
        }
        int maxLen = Math.max(s1.length(), s2.length());
        if (maxLen == 0) {
            return 1.0;
        }
        int distance = calculate(s1, s2);
        return 1.0 - (double) distance / maxLen;
    }
}
