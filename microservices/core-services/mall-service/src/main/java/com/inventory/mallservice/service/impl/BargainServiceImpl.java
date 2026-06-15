package com.inventory.mallservice.service.impl;

import com.inventory.common.core.exception.EntityNotFoundException;
import com.inventory.mallservice.entity.BargainActivity;
import com.inventory.mallservice.entity.BargainDetail;
import com.inventory.mallservice.entity.BargainRecord;
import com.inventory.mallservice.exception.BargainException;
import com.inventory.mallservice.repository.IBargainActivityRepository;
import com.inventory.mallservice.repository.IBargainDetailRepository;
import com.inventory.mallservice.repository.IBargainRecordRepository;
import com.inventory.mallservice.service.IBargainService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 砍价服务实现类.
 *
 * @author Inventory Team
 * @version 1.0
 * @since 3.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("null")
public class BargainServiceImpl implements IBargainService {

    private final IBargainActivityRepository activityRepository;
    private final IBargainDetailRepository detailRepository;
    private final IBargainRecordRepository recordRepository;

    @Override
    @Transactional
    public BargainActivity createBargainActivity(final BargainActivity activity) {
        log.info("Creating bargain activity: {}", activity.getName());
        return activityRepository.save(activity);
    }

    @Override
    @Transactional
    public BargainDetail initiateBargain(final Long activityId, final Long userId) {
        log.info("User {} initiating bargain for activity {}", userId, activityId);
        BargainActivity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> EntityNotFoundException.forEntity("BargainActivity", activityId));
        BargainDetail detail = new BargainDetail();
        detail.setActivity(activity);
        detail.setUserId(userId);
        detail.setCurrentPrice(activity.getOriginalPrice());
        detail.setBargainCount(0);
        detail.setStatus("ONGOING");
        detail.setExpiredAt(LocalDateTime.now().plusHours(24));
        return detailRepository.save(detail);
    }

    @Override
    @Transactional
    public BargainRecord helpBargain(final Long bargainDetailId, final Long helperId) {
        log.info("User {} helping bargain {}", helperId, bargainDetailId);
        BargainDetail detail = detailRepository.findById(bargainDetailId)
                .orElseThrow(() -> EntityNotFoundException.forEntity("BargainDetail", bargainDetailId));
        if (!"ONGOING".equals(detail.getStatus())) {
            throw BargainException.notOngoing();
        }
        BargainActivity activity = detail.getActivity();
        if (detail.getBargainCount() >= activity.getMaxBargainCount()) {
            throw BargainException.maxCountReached();
        }

        BigDecimal bargainAmount = calculateBargainAmount(detail);
        detail.setCurrentPrice(detail.getCurrentPrice().subtract(bargainAmount));
        detail.setBargainCount(detail.getBargainCount() + 1);

        if (detail.getCurrentPrice().compareTo(activity.getMinPrice()) <= 0) {
            detail.setCurrentPrice(activity.getMinPrice());
            detail.setStatus("SUCCESS");
            detail.setCompletedAt(LocalDateTime.now());
        }
        detailRepository.save(detail);

        BargainRecord record = new BargainRecord();
        record.setBargainDetail(detail);
        record.setHelperId(helperId);
        record.setDiscountAmount(bargainAmount);
        return recordRepository.save(record);
    }

    @Override
    public BargainDetail getBargainDetail(final Long id) {
        return detailRepository.findById(id)
                .orElseThrow(() -> EntityNotFoundException.forEntity("BargainDetail", id));
    }

    @Override
    public List<BargainDetail> getUserBargains(final Long userId, final String status) {
        if (status != null) {
            return detailRepository.findByUserIdAndStatus(userId, status);
        }
        return detailRepository.findByUserIdAndStatus(userId, "ONGOING");
    }

    @Override
    public Page<BargainActivity> getBargainActivities(final Pageable pageable) {
        return activityRepository.findAll(pageable);
    }

    @Override
    public List<BargainActivity> getActiveBargainActivities() {
        return activityRepository.findByStatus("ACTIVE");
    }

    @Override
    public BigDecimal calculateBargainAmount(final BargainDetail detail) {
        BargainActivity activity = detail.getActivity();
        BigDecimal remaining = detail.getCurrentPrice().subtract(activity.getMinPrice());
        int remainingCount = activity.getMaxBargainCount() - detail.getBargainCount();
        if (remainingCount <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal avgAmount = remaining.divide(BigDecimal.valueOf(remainingCount), 2, RoundingMode.HALF_UP);
        BigDecimal variance = avgAmount.multiply(BigDecimal.valueOf(0.5));
        double randomFactor = 0.5 + ThreadLocalRandom.current().nextDouble(1.0);
        BigDecimal amount = avgAmount.add(variance.multiply(BigDecimal.valueOf(randomFactor - 0.5)));
        if (amount.compareTo(remaining) > 0) {
            amount = remaining;
        }
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            amount = BigDecimal.ZERO;
        }
        return amount.setScale(2, RoundingMode.HALF_UP);
    }
}
