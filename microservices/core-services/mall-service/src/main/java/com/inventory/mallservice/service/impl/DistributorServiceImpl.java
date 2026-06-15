package com.inventory.mallservice.service.impl;

import com.inventory.common.core.exception.EntityNotFoundException;
import com.inventory.mallservice.entity.CommissionRecord;
import com.inventory.mallservice.entity.Distributor;
import com.inventory.mallservice.exception.DistributorException;
import com.inventory.mallservice.repository.ICommissionRecordRepository;
import com.inventory.mallservice.repository.IDistributorRepository;
import com.inventory.mallservice.service.IDistributorService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 分销服务实现类.
 *
 * @author Inventory Team
 * @version 1.0
 * @since 3.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("null")
public class DistributorServiceImpl implements IDistributorService {

    private final IDistributorRepository distributorRepository;
    private final ICommissionRecordRepository commissionRecordRepository;

    @Override
    @Transactional
    public Distributor registerDistributor(final Long userId, final Long parentId) {
        log.info("Registering distributor for user {} with parent {}", userId, parentId);
        if (distributorRepository.findByUserId(userId).isPresent()) {
            throw DistributorException.alreadyDistributor();
        }

        Distributor distributor = new Distributor();
        distributor.setUserId(userId);
        distributor.setDistributorCode("DIST" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        distributor.setStatus("ACTIVE");
        distributor.setTotalCommission(BigDecimal.ZERO);
        distributor.setAvailableCommission(BigDecimal.ZERO);

        if (parentId != null) {
            Distributor parent = distributorRepository.findById(parentId)
                    .orElseThrow(() -> EntityNotFoundException.forEntity("Distributor", parentId));
            distributor.setParentId(parentId);
            distributor.setLevel(parent.getLevel() + 1);
            distributor.setCommissionRate(parent.getCommissionRate()
                    .multiply(BigDecimal.valueOf(0.5))
                    .setScale(2, RoundingMode.HALF_UP));
        } else {
            distributor.setLevel(1);
            distributor.setCommissionRate(new BigDecimal("15.00"));
        }

        return distributorRepository.save(distributor);
    }

    @Override
    public Distributor getDistributorById(final Long id) {
        return distributorRepository.findById(id)
                .orElseThrow(() -> EntityNotFoundException.forEntity("Distributor", id));
    }

    @Override
    public Distributor getDistributorByUserId(final Long userId) {
        return distributorRepository.findByUserId(userId)
                .orElseThrow(() -> EntityNotFoundException.forEntityWithField("Distributor", "userId", userId));
    }

    @Override
    public Distributor getDistributorByCode(final String code) {
        return distributorRepository.findByDistributorCode(code)
                .orElseThrow(() -> EntityNotFoundException.forEntityWithField("Distributor", "distributorCode", code));
    }

    @Override
    public Page<Distributor> getDistributors(final Pageable pageable) {
        return distributorRepository.findAll(pageable);
    }

    @Override
    public List<Distributor> getSubDistributors(final Long parentId) {
        return distributorRepository.findByParentId(parentId);
    }

    @Override
    public BigDecimal calculateCommission(final Long distributorId, final Long orderId) {
        log.info("Calculating commission for distributor {} on order {}", distributorId, orderId);
        Distributor distributor = getDistributorById(distributorId);
        BigDecimal rate = distributor.getCommissionRate().divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        return BigDecimal.ZERO.multiply(rate);
    }

    @Override
    @Transactional
    public void settleCommissions(final Long distributorId) {
        log.info("Settling commissions for distributor {}", distributorId);
        Distributor distributor = getDistributorById(distributorId);
        List<CommissionRecord> pendingRecords = commissionRecordRepository
                .findByDistributorIdAndStatus(distributorId, "PENDING");

        BigDecimal totalToSettle = BigDecimal.ZERO;
        for (CommissionRecord record : pendingRecords) {
            record.setStatus("SETTLED");
            record.setSettledAt(LocalDateTime.now());
            totalToSettle = totalToSettle.add(record.getAmount());
        }
        commissionRecordRepository.saveAll(pendingRecords);

        distributor.setAvailableCommission(distributor.getAvailableCommission().subtract(totalToSettle));
        distributorRepository.save(distributor);
    }

    @Override
    @Transactional
    public Distributor updateDistributorStatus(final Long id, final String status) {
        log.info("Updating distributor {} status to {}", id, status);
        Distributor distributor = getDistributorById(id);
        distributor.setStatus(status);
        return distributorRepository.save(distributor);
    }

    @Override
    @Transactional
    public Distributor updateCommissionRate(final Long id, final BigDecimal rate) {
        log.info("Updating distributor {} commission rate to {}", id, rate);
        Distributor distributor = getDistributorById(id);
        distributor.setCommissionRate(rate);
        return distributorRepository.save(distributor);
    }

    @Override
    public List<Distributor> getDistributorChain(final Long userId) {
        log.info("Getting distributor chain for user {}", userId);
        List<Distributor> chain = new ArrayList<>();
        Distributor current = distributorRepository.findByUserId(userId).orElse(null);
        while (current != null) {
            chain.add(current);
            if (current.getParentId() != null) {
                current = distributorRepository.findById(current.getParentId()).orElse(null);
            } else {
                current = null;
            }
        }
        return chain;
    }
}
