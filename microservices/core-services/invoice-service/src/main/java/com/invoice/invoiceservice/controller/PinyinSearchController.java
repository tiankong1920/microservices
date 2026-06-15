package com.invoice.invoiceservice.controller;

import java.util.List;

import com.invoice.invoiceservice.dto.CustomerInfoDTO;
import com.invoice.invoiceservice.dto.SearchResultDTO;
import com.invoice.invoiceservice.service.PinyinSearchService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/invoice/pinyin")
@RequiredArgsConstructor
@Tag(name = "Pinyin Search", description = "拼音首字母检索API")
@Validated
public class PinyinSearchController {

    private final PinyinSearchService pinyinSearchService;

    /**
     * 首字母检索客户信息
     *
     * @param initials 拼音首字母
     * @return 符合条件的客户信息列表
     */
    @GetMapping("/initials")
    @Operation(summary = "首字母检索客户信息")
    public ResponseEntity<List<SearchResultDTO<CustomerInfoDTO>>> searchByInitials(
            @RequestParam String initials) {
        List<SearchResultDTO<CustomerInfoDTO>> results = pinyinSearchService.searchByInitials(initials);
        return ResponseEntity.ok(results);
    }

    /**
     * 全拼检索客户信息
     *
     * @param pinyin 拼音全拼
     * @return 符合条件的客户信息列表
     */
    @GetMapping("/full")
    @Operation(summary = "全拼检索客户信息")
    public ResponseEntity<List<SearchResultDTO<CustomerInfoDTO>>> searchByFullPinyin(
            @RequestParam String pinyin) {
        List<SearchResultDTO<CustomerInfoDTO>> results = pinyinSearchService.searchByFullPinyin(pinyin);
        return ResponseEntity.ok(results);
    }

    /**
     * 混拼检索客户信息（支持简拼+全拼）
     *
     * @param input 混拼输入
     * @return 符合条件的客户信息列表
     */
    @GetMapping("/mixed")
    @Operation(summary = "混拼检索客户信息（支持简拼+全拼）")
    public ResponseEntity<List<SearchResultDTO<CustomerInfoDTO>>> searchByMixedPinyin(
            @RequestParam String input) {
        List<SearchResultDTO<CustomerInfoDTO>> results = pinyinSearchService.searchByMixedPinyin(input);
        return ResponseEntity.ok(results);
    }

    /**
     * 手动触发拼音索引重建
     *
     * @return 重建结果信息
     */
    @PostMapping("/rebuild-index")
    @Operation(summary = "手动触发拼音索引重建")
    public ResponseEntity<String> rebuildPinyinIndex() {
        pinyinSearchService.rebuildPinyinIndex();
        return ResponseEntity.ok("拼音索引重建已触发");
    }
}
