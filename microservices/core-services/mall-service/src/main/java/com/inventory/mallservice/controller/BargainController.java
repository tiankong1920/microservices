package com.inventory.mallservice.controller;

import com.inventory.mallservice.entity.BargainActivity;
import com.inventory.mallservice.entity.BargainDetail;
import com.inventory.mallservice.entity.BargainRecord;
import com.inventory.mallservice.service.IBargainService;

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
 * 砍价Controller.
 *
 * @author Inventory Team
 * @version 1.0
 * @since 3.0.0
 */
@RestController
@RequestMapping("/api/v1/mall/bargains")
@Tag(name = "Bargain", description = "砍价活动管理接口")
@RequiredArgsConstructor
@Slf4j
@Validated
@SuppressWarnings("null")
public class BargainController {

    private final IBargainService bargainService;

    /**
     * 创建砍价活动
     *
     * @param activity 砍价活动数据
     * @return 创建的砍价活动信息
     */
    @PostMapping("/activities")
    public ResponseEntity<BargainActivity> createBargainActivity(
            @Valid @RequestBody final BargainActivity activity) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(bargainService.createBargainActivity(activity));
    }

    /**
     * 分页获取砍价活动列表
     *
     * @param page 页码
     * @param size 每页大小
     * @return 砍价活动分页列表
     */
    @GetMapping("/activities")
    public ResponseEntity<Page<BargainActivity>> getBargainActivities(
            @RequestParam(defaultValue = "0") final int page,
            @RequestParam(defaultValue = "20") final int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(bargainService.getBargainActivities(pageable));
    }

    /**
     * 获取正在进行的砍价活动
     *
     * @return 正在进行的砍价活动列表
     */
    @GetMapping("/activities/active")
    public ResponseEntity<List<BargainActivity>> getActiveBargainActivities() {
        return ResponseEntity.ok(bargainService.getActiveBargainActivities());
    }

    /**
     * 发起砍价
     *
     * @param request 包含activityId和userId的请求数据
     * @return 砍价详情信息
     */
    @PostMapping("/initiate")
    public ResponseEntity<BargainDetail> initiateBargain(
            @Valid @RequestBody final Map<String, Object> request) {
        Long activityId = Long.valueOf(request.get("activityId").toString());
        Long userId = Long.valueOf(request.get("userId").toString());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(bargainService.initiateBargain(activityId, userId));
    }

    /**
     * 帮助砍价
     *
     * @param request 包含bargainDetailId和helperId的请求数据
     * @return 砍价记录信息
     */
    @PostMapping("/help")
    public ResponseEntity<BargainRecord> helpBargain(
            @Valid @RequestBody final Map<String, Object> request) {
        Long bargainDetailId = Long.valueOf(request.get("bargainDetailId").toString());
        Long helperId = Long.valueOf(request.get("helperId").toString());
        return ResponseEntity.ok(bargainService.helpBargain(bargainDetailId, helperId));
    }

    /**
     * 获取砍价详情
     *
     * @param id 砍价详情ID
     * @return 砍价详情信息
     */
    @GetMapping("/details/{id}")
    public ResponseEntity<BargainDetail> getBargainDetail(@PathVariable final Long id) {
        return ResponseEntity.ok(bargainService.getBargainDetail(id));
    }

    /**
     * 获取用户的砍价记录
     *
     * @param userId 用户ID
     * @param status 状态（可选）
     * @return 用户的砍价详情列表
     */
    @GetMapping("/users/{userId}")
    public ResponseEntity<List<BargainDetail>> getUserBargains(
            @PathVariable final Long userId,
            @RequestParam(required = false) final String status) {
        return ResponseEntity.ok(bargainService.getUserBargains(userId, status));
    }
}
