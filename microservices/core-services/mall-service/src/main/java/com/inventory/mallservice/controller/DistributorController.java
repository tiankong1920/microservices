package com.inventory.mallservice.controller;

import com.inventory.mallservice.entity.Distributor;
import com.inventory.mallservice.service.IDistributorService;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import jakarta.validation.Valid;

/**
 * 分销Controller.
 *
 * @author Inventory Team
 * @version 1.0
 * @since 3.0.0
 */
@RestController
@RequestMapping("/api/v1/mall/distributors")
@Tag(name = "Distributor", description = "经销商管理接口")
@RequiredArgsConstructor
@Slf4j
@Validated
@SuppressWarnings("null")
public class DistributorController {

    private final IDistributorService distributorService;

    /**
     * 注册分销商
     *
     * @param request 包含userId和可选parentId的请求数据
     * @return 注册的分销商信息
     */
    @PostMapping("/register")
    public ResponseEntity<Distributor> register(@Valid @RequestBody final Map<String, Object> request) {
        Long userId = Long.valueOf(request.get("userId").toString());
        Long parentId = request.get("parentId") != null
                ? Long.valueOf(request.get("parentId").toString()) : null;
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(distributorService.registerDistributor(userId, parentId));
    }

    /**
     * 根据ID获取分销商
     *
     * @param id 分销商ID
     * @return 分销商信息
     */
    @GetMapping("/{id}")
    public ResponseEntity<Distributor> getDistributor(@PathVariable final Long id) {
        return ResponseEntity.ok(distributorService.getDistributorById(id));
    }

    /**
     * 根据用户ID获取分销商
     *
     * @param userId 用户ID
     * @return 分销商信息
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Distributor> getDistributorByUser(@PathVariable final Long userId) {
        return ResponseEntity.ok(distributorService.getDistributorByUserId(userId));
    }

    /**
     * 根据分销码获取分销商
     *
     * @param code 分销码
     * @return 分销商信息
     */
    @GetMapping("/code/{code}")
    public ResponseEntity<Distributor> getDistributorByCode(@PathVariable final String code) {
        return ResponseEntity.ok(distributorService.getDistributorByCode(code));
    }

    /**
     * 分页获取分销商列表
     *
     * @param page 页码
     * @param size 每页大小
     * @return 分销商分页列表
     */
    @GetMapping
    public ResponseEntity<Page<Distributor>> getDistributors(
            @RequestParam(defaultValue = "0") final int page,
            @RequestParam(defaultValue = "20") final int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(distributorService.getDistributors(pageable));
    }

    /**
     * 获取下级分销商
     *
     * @param id 分销商ID
     * @return 下级分销商列表
     */
    @GetMapping("/{id}/sub")
    public ResponseEntity<List<Distributor>> getSubDistributors(@PathVariable final Long id) {
        return ResponseEntity.ok(distributorService.getSubDistributors(id));
    }

    /**
     * 更新分销商状态
     *
     * @param id 分销商ID
     * @param request 状态更新请求
     * @return 更新后的分销商信息
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<Distributor> updateStatus(
            @PathVariable final Long id, @Valid @RequestBody final Map<String, String> request) {
        return ResponseEntity.ok(distributorService.updateDistributorStatus(id, request.get("status")));
    }

    /**
     * 更新分销商佣金比例
     *
     * @param id 分销商ID
     * @param request 佣金比例更新请求
     * @return 更新后的分销商信息
     */
    @PutMapping("/{id}/commission-rate")
    public ResponseEntity<Distributor> updateCommissionRate(
            @PathVariable final Long id, @Valid @RequestBody final Map<String, BigDecimal> request) {
        return ResponseEntity.ok(distributorService.updateCommissionRate(id, request.get("rate")));
    }

    /**
     * 获取分销商链条
     *
     * @param userId 用户ID
     * @return 分销商链条列表
     */
    @GetMapping("/chain/{userId}")
    public ResponseEntity<List<Distributor>> getDistributorChain(@PathVariable final Long userId) {
        return ResponseEntity.ok(distributorService.getDistributorChain(userId));
    }

    /**
     * 结算分销商佣金
     *
     * @param id 分销商ID
     * @return 无内容响应
     */
    @PostMapping("/{id}/settle")
    public ResponseEntity<Void> settleCommissions(@PathVariable final Long id) {
        distributorService.settleCommissions(id);
        return ResponseEntity.ok().build();
    }
}
