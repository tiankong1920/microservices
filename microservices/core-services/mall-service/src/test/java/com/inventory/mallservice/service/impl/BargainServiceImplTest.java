package com.inventory.mallservice.service.impl;

import com.inventory.mallservice.entity.BargainActivity;
import com.inventory.mallservice.entity.BargainDetail;
import com.inventory.mallservice.repository.IBargainActivityRepository;
import com.inventory.mallservice.repository.IBargainDetailRepository;
import com.inventory.mallservice.repository.IBargainRecordRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("砍价服务测试")
@SuppressWarnings("null")
class BargainServiceImplTest {

    @Mock
    private IBargainActivityRepository activityRepository;

    @Mock
    private IBargainDetailRepository detailRepository;

    @Mock
    private IBargainRecordRepository recordRepository;

    private BargainServiceImpl bargainService;

    private BargainActivity testActivity;
    private BargainDetail testDetail;

    @BeforeEach
    void setUp() {
        bargainService = new BargainServiceImpl(activityRepository, detailRepository, recordRepository);

        testActivity = new BargainActivity();
        testActivity.setId(1L);
        testActivity.setName("砍价拿好物");
        testActivity.setOriginalPrice(new BigDecimal("200.00"));
        testActivity.setMinPrice(new BigDecimal("50.00"));
        testActivity.setMaxBargainCount(10);
        testActivity.setStatus("ACTIVE");

        testDetail = new BargainDetail();
        testDetail.setId(1L);
        testDetail.setActivity(testActivity);
        testDetail.setUserId(100L);
        testDetail.setCurrentPrice(new BigDecimal("200.00"));
        testDetail.setBargainCount(0);
        testDetail.setStatus("ONGOING");
        testDetail.setExpiredAt(LocalDateTime.now().plusHours(24));
    }

    @Test
    @DisplayName("创建砍价活动")
    void createBargainActivity() {
        when(activityRepository.save(any(BargainActivity.class))).thenReturn(testActivity);

        BargainActivity result = bargainService.createBargainActivity(testActivity);

        assertNotNull(result);
        assertEquals("砍价拿好物", result.getName());
        verify(activityRepository).save(any(BargainActivity.class));
    }

    @Test
    @DisplayName("发起砍价")
    void initiateBargain() {
        when(activityRepository.findById(1L)).thenReturn(Optional.of(testActivity));
        when(detailRepository.save(any(BargainDetail.class))).thenReturn(testDetail);

        BargainDetail result = bargainService.initiateBargain(1L, 100L);

        assertNotNull(result);
        assertEquals("ONGOING", result.getStatus());
        verify(detailRepository).save(any(BargainDetail.class));
    }

    @Test
    @DisplayName("发起砍价 - 活动不存在应抛出异常")
    void initiateBargainActivityNotFound() {
        when(activityRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> bargainService.initiateBargain(999L, 100L));
    }

    @Test
    @DisplayName("计算砍价金额应在合理范围内")
    void calculateBargainAmount() {
        BigDecimal amount = bargainService.calculateBargainAmount(testDetail);

        assertNotNull(amount);
        assertTrue(amount.compareTo(BigDecimal.ZERO) >= 0);
        assertTrue(amount.compareTo(new BigDecimal("200.00")) <= 0);
    }

    @Test
    @DisplayName("获取活跃砍价活动")
    void getActiveBargainActivities() {
        when(activityRepository.findByStatus("ACTIVE")).thenReturn(List.of(testActivity));

        List<BargainActivity> result = bargainService.getActiveBargainActivities();

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("获取用户砍价详情")
    void getUserBargains() {
        when(detailRepository.findByUserIdAndStatus(100L, "ONGOING"))
                .thenReturn(List.of(testDetail));

        List<BargainDetail> result = bargainService.getUserBargains(100L, "ONGOING");

        assertNotNull(result);
        assertEquals(1, result.size());
    }
}
