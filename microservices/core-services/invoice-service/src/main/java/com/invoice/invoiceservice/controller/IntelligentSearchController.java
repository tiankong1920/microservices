package com.invoice.invoiceservice.controller;

import java.util.List;

import com.invoice.invoiceservice.dto.CustomerInfoDTO;
import com.invoice.invoiceservice.dto.SearchResultDTO;
import com.invoice.invoiceservice.service.IntelligentSearchService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/invoice/search")
@RequiredArgsConstructor
@Tag(name = "Intelligent Search", description = "智能检索API")
@Validated
public class IntelligentSearchController {

    private final IntelligentSearchService searchService;

    /**
     * 模糊搜索客户信息
     *
     * @param keyword 搜索关键字
     * @param threshold 相似度阈值
     * @return 符合条件的客户信息列表
     */
    @GetMapping("/fuzzy")
    @Operation(summary = "模糊搜索客户信息（Levenshtein距离算法）")
    public ResponseEntity<List<SearchResultDTO<CustomerInfoDTO>>> fuzzySearch(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0.85") double threshold) {
        List<SearchResultDTO<CustomerInfoDTO>> results = searchService.fuzzySearchCustomers(keyword, threshold);
        return ResponseEntity.ok(results);
    }

    /**
     * 拼音全拼搜索客户信息
     *
     * @param pinyin 拼音全拼
     * @return 符合条件的客户信息列表
     */
    @GetMapping("/pinyin")
    @Operation(summary = "拼音全拼搜索客户信息")
    public ResponseEntity<List<SearchResultDTO<CustomerInfoDTO>>> searchByPinyin(
            @RequestParam String pinyin) {
        List<SearchResultDTO<CustomerInfoDTO>> results = searchService.searchByPinyin(pinyin);
        return ResponseEntity.ok(results);
    }

    /**
     * 拼音首字母搜索客户信息
     *
     * @param initials 拼音首字母
     * @return 符合条件的客户信息列表
     */
    @GetMapping("/pinyin/initials")
    @Operation(summary = "拼音首字母搜索客户信息")
    public ResponseEntity<List<SearchResultDTO<CustomerInfoDTO>>> searchByPinyinInitials(
            @RequestParam String initials) {
        List<SearchResultDTO<CustomerInfoDTO>> results = searchService.searchByPinyinInitials(initials);
        return ResponseEntity.ok(results);
    }

    /**
     * 复合搜索（关键字+拼音）
     *
     * @param keyword 搜索关键字
     * @param pinyin 拼音全拼
     * @param threshold 相似度阈值
     * @return 符合条件的客户信息列表
     */
    @GetMapping("/composite")
    @Operation(summary = "复合搜索（关键字+拼音）")
    public ResponseEntity<List<SearchResultDTO<CustomerInfoDTO>>> compositeSearch(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String pinyin,
            @RequestParam(defaultValue = "0.85") double threshold) {
        List<SearchResultDTO<CustomerInfoDTO>> results = searchService.compositeSearch(keyword, pinyin, threshold);
        return ResponseEntity.ok(results);
    }

    /**
     * 获取高频使用客户
     *
     * @param limit 返回数量限制
     * @return 高频使用的客户列表
     */
    @GetMapping("/frequent")
    @Operation(summary = "获取高频使用客户")
    public ResponseEntity<List<CustomerInfoDTO>> getFrequentCustomers(
            @RequestParam(defaultValue = "10") int limit) {
        List<CustomerInfoDTO> results = searchService.getFrequentlyUsedCustomers(limit);
        return ResponseEntity.ok(results);
    }

    /**
     * 获取最近使用客户
     *
     * @param limit 返回数量限制
     * @return 最近使用的客户列表
     */
    @GetMapping("/recent")
    @Operation(summary = "获取最近使用客户")
    public ResponseEntity<List<CustomerInfoDTO>> getRecentCustomers(
            @RequestParam(defaultValue = "10") int limit) {
        List<CustomerInfoDTO> results = searchService.getRecentlyUsedCustomers(limit);
        return ResponseEntity.ok(results);
    }
}
