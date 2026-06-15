package com.invoice.invoiceservice.util;

import java.util.HashSet;
import java.util.Set;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import org.springframework.stereotype.Component;

@Component
public class PinyinUtil {

    private final HanyuPinyinOutputFormat format;

    public PinyinUtil() {
        format = new HanyuPinyinOutputFormat();
        format.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        format.setVCharType(HanyuPinyinVCharType.WITH_V);
    }

    public String getInitials(String chinese) {
        if (chinese == null || chinese.isEmpty()) {
            return "";
        }
        StringBuilder initials = new StringBuilder();
        for (char c : chinese.toCharArray()) {
            if (isChinese(c)) {
                String[] pinyinArray = getPinyinArray(c);
                if (pinyinArray != null && pinyinArray.length > 0) {
                    initials.append(Character.toLowerCase(pinyinArray[0].charAt(0)));
                }
            } else if (Character.isLetter(c)) {
                initials.append(Character.toLowerCase(c));
            }
        }
        return initials.toString();
    }

    public String getFullPinyin(String chinese) {
        if (chinese == null || chinese.isEmpty()) {
            return "";
        }
        StringBuilder fullPinyin = new StringBuilder();
        for (char c : chinese.toCharArray()) {
            if (isChinese(c)) {
                String[] pinyinArray = getPinyinArray(c);
                if (pinyinArray != null && pinyinArray.length > 0) {
                    if (!fullPinyin.isEmpty()) {
                        fullPinyin.append(" ");
                    }
                    fullPinyin.append(pinyinArray[0]);
                }
            } else if (Character.isLetter(c) || Character.isDigit(c)) {
                fullPinyin.append(Character.toLowerCase(c));
            }
        }
        return fullPinyin.toString();
    }

    public Set<String> getAllPossibleInitials(String chinese) {
        Set<String> results = new HashSet<>();
        if (chinese == null || chinese.isEmpty()) {
            return results;
        }
        generateInitialsCombinations(chinese, 0, new StringBuilder(), results);
        return results;
    }

    private void generateInitialsCombinations(String chinese, int index, StringBuilder current, Set<String> results) {
        if (index >= chinese.length()) {
            if (!current.isEmpty()) {
                results.add(current.toString());
            }
            return;
        }
        char c = chinese.charAt(index);
        if (isChinese(c)) {
            String[] pinyinArray = getPinyinArray(c);
            if (pinyinArray != null && pinyinArray.length > 0) {
                Set<Character> firstChars = new HashSet<>();
                for (String py : pinyinArray) {
                    firstChars.add(Character.toLowerCase(py.charAt(0)));
                }
                for (Character fc : firstChars) {
                    current.append(fc);
                    generateInitialsCombinations(chinese, index + 1, current, results);
                    current.deleteCharAt(current.length() - 1);
                }
            } else {
                generateInitialsCombinations(chinese, index + 1, current, results);
            }
        } else if (Character.isLetter(c)) {
            current.append(Character.toLowerCase(c));
            generateInitialsCombinations(chinese, index + 1, current, results);
            current.deleteCharAt(current.length() - 1);
        } else {
            generateInitialsCombinations(chinese, index + 1, current, results);
        }
    }

    private String[] getPinyinArray(char c) {
        try {
            return PinyinHelper.toHanyuPinyinStringArray(c, format);
        } catch (BadHanyuPinyinOutputFormatCombination e) {
            return null;
        }
    }

    private boolean isChinese(char c) {
        return c >= '\u4e00' && c <= '\u9fa5';
    }
}
