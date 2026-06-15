package com.inventory.mallservice.controller;

import com.inventory.mallservice.entity.GroupBuy;
import com.inventory.mallservice.entity.GroupBuyDetail;
import com.inventory.mallservice.service.IGroupBuyService;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

import jakarta.validation.Valid;

/**
 * 拼团Controller.
 *
 * @author Inventory Team
 * @version 1.0
 * @since 3.0.0
 */
@RestController
@RequestMapping("/api/v1/mall/group-buys")
@Tag(name = "Group Buy", description = "团购活动管理接口")
@RequiredArgsConstructor
@Slf4j
@Validated
@SuppressWarnings("null")
public class GroupBuyController {

    private final IGroupBuyService groupBuyService;

    /**
     * 创建团购活动
     *
     * @param groupBuy 团购活动数据
     * @return 创建的团购活动信息
     */
    @PostMapping
    public ResponseEntity<GroupBuy> createGroupBuy(@Valid @RequestBody final GroupBuy groupBuy) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(groupBuyService.createGroupBuy(groupBuy));
    }

    /**
     * 获取团购活动详情
     *
     * @param id 团购活动ID
     * @return 团购活动信息
     */
    @GetMapping("/{id}")
    public ResponseEntity<GroupBuy> getGroupBuy(@PathVariable final Long id) {
        return ResponseEntity.ok(groupBuyService.getGroupBuyById(id));
    }

    /**
     * 分页获取团购活动列表
     *
     * @param page 页码
     * @param size 每页大小
     * @return 团购活动分页列表
     */
    @GetMapping
    public ResponseEntity<Page<GroupBuy>> getGroupBuys(
            @RequestParam(defaultValue = "0") final int page,
            @RequestParam(defaultValue = "20") final int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(groupBuyService.getGroupBuys(pageable));
    }

    /**
     * 获取正在进行的团购活动
     *
     * @return 正在进行的团购活动列表
     */
    @GetMapping("/active")
    public ResponseEntity<List<GroupBuy>> getActiveGroupBuys() {
        return ResponseEntity.ok(groupBuyService.getActiveGroupBuys());
    }

    /**
     * 加入团购
     *
     * @param groupBuyId 团购活动ID
     * @param request 包含userId的请求数据
     * @return 团购详情信息
     */
    @PostMapping("/{groupBuyId}/join")
    public ResponseEntity<GroupBuyDetail> joinGroupBuy(
            @PathVariable final Long groupBuyId,
            @Valid @RequestBody final Map<String, Object> request) {
        Long userId = Long.valueOf(request.get("userId").toString());
        return ResponseEntity.ok(groupBuyService.joinGroupBuy(groupBuyId, userId));
    }

    /**
     * 创建新团购
     *
     * @param groupBuyId 团购活动ID
     * @param request 包含userId的请求数据
     * @return 团购详情信息
     */
    @PostMapping("/{groupBuyId}/create-group")
    public ResponseEntity<GroupBuyDetail> createNewGroup(
            @PathVariable final Long groupBuyId,
            @Valid @RequestBody final Map<String, Object> request) {
        Long userId = Long.valueOf(request.get("userId").toString());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(groupBuyService.createNewGroup(groupBuyId, userId));
    }
}
