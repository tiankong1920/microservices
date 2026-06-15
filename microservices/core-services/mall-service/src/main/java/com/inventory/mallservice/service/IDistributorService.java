package com.inventory.mallservice.service;

import com.inventory.mallservice.entity.Distributor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

/**
 * 分销服务接口.
 *
 * @author Inventory Team
 * @version 1.0
 * @since 3.0.0
 */
public interface IDistributorService {

    Distributor registerDistributor(Long userId, Long parentId);

    Distributor getDistributorById(Long id);

    Distributor getDistributorByUserId(Long userId);

    Distributor getDistributorByCode(String code);

    Page<Distributor> getDistributors(Pageable pageable);

    List<Distributor> getSubDistributors(Long parentId);

    BigDecimal calculateCommission(Long distributorId, Long orderId);

    void settleCommissions(Long distributorId);

    Distributor updateDistributorStatus(Long id, String status);

    Distributor updateCommissionRate(Long id, BigDecimal rate);

    List<Distributor> getDistributorChain(Long userId);
}
