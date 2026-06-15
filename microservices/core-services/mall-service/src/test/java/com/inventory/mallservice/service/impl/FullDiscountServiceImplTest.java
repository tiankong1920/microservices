package com.inventory.mallservice.service.impl;

import com.inventory.mallservice.entity.FullDiscount;
import com.inventory.mallservice.entity.FullDiscountRule;
import com.inventory.mallservice.repository.IFullDiscountRepository;
import com.inventory.mallservice.repository.IFullDiscountRuleRepository;

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
@DisplayName("满减服务测试")
@SuppressWarnings("null")
class FullDiscountServiceImplTest {

    @Mock
    private IFullDiscountRepository fullDiscountRepository;

    @Mock
    private IFullDiscountRuleRepository fullDiscountRuleRepository;

    private FullDiscountServiceImpl fullDiscountService;

    private FullDiscount testDiscount;
    private FullDiscountRule testRule;

    @BeforeEach
    void setUp() {
        fullDiscountService = new FullDiscountServiceImpl(fullDiscountRepository, fullDiscountRuleRepository);

        testDiscount = new FullDiscount();
        testDiscount.setId(1L);
        testDiscount.setName("满100减20");
        testDiscount.setStatus("ACTIVE");
        testDiscount.setStartTime(LocalDateTime.now().minusDays(1));
        testDiscount.setEndTime(LocalDateTime.now().plusDays(7));

        testRule = new FullDiscountRule();
        testRule.setId(1L);
        testRule.setFullDiscount(testDiscount);
        testRule.setMinAmount(new BigDecimal("100.00"));
        testRule.setDiscountAmount(new BigDecimal("20.00"));
        testRule.setSortOrder(0);
    }

    @Test
    @DisplayName("创建满减活动")
    void createFullDiscount() {
        when(fullDiscountRepository.save(any(FullDiscount.class))).thenReturn(testDiscount);

        FullDiscount result = fullDiscountService.createFullDiscount(testDiscount);

        assertNotNull(result);
        assertEquals("满100减20", result.getName());
        verify(fullDiscountRepository).save(any(FullDiscount.class));
    }

    @Test
    @DisplayName("根据ID获取满减活动")
    void getFullDiscountById() {
        when(fullDiscountRepository.findById(1L)).thenReturn(Optional.of(testDiscount));

        FullDiscount result = fullDiscountService.getFullDiscountById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    @DisplayName("根据ID获取不存在的满减活动应抛出异常")
    void getFullDiscountByIdNotFound() {
        when(fullDiscountRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> fullDiscountService.getFullDiscountById(999L));
    }

    @Test
    @DisplayName("添加满减规则")
    void addRule() {
        when(fullDiscountRepository.findById(1L)).thenReturn(Optional.of(testDiscount));
        when(fullDiscountRuleRepository.save(any(FullDiscountRule.class))).thenReturn(testRule);

        FullDiscountRule result = fullDiscountService.addRule(1L, testRule);

        assertNotNull(result);
        assertEquals(new BigDecimal("20.00"), result.getDiscountAmount());
        verify(fullDiscountRuleRepository).save(any(FullDiscountRule.class));
    }

    @Test
    @DisplayName("计算满减折扣 - 满足条件")
    void calculateDiscountMet() {
        when(fullDiscountRuleRepository.findByFullDiscountIdOrderByMinAmountAsc(1L))
                .thenReturn(List.of(testRule));

        BigDecimal discount = fullDiscountService.calculateDiscount(1L, new BigDecimal("150.00"));

        assertEquals(new BigDecimal("20.00"), discount);
    }

    @Test
    @DisplayName("计算满减折扣 - 不满足条件")
    void calculateDiscountNotMet() {
        when(fullDiscountRuleRepository.findByFullDiscountIdOrderByMinAmountAsc(1L))
                .thenReturn(List.of(testRule));

        BigDecimal discount = fullDiscountService.calculateDiscount(1L, new BigDecimal("50.00"));

        assertEquals(BigDecimal.ZERO, discount);
    }

    @Test
    @DisplayName("计算最优满减折扣")
    void calculateBestDiscount() {
        when(fullDiscountRepository.findByStatus("ACTIVE")).thenReturn(List.of(testDiscount));
        when(fullDiscountRuleRepository.findByFullDiscountIdOrderByMinAmountAsc(1L))
                .thenReturn(List.of(testRule));

        BigDecimal discount = fullDiscountService.calculateBestDiscount(new BigDecimal("150.00"));

        assertTrue(discount.compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    @DisplayName("获取活跃满减活动")
    void getActiveFullDiscounts() {
        when(fullDiscountRepository.findByStatus("ACTIVE")).thenReturn(List.of(testDiscount));

        List<FullDiscount> result = fullDiscountService.getActiveFullDiscounts();

        assertNotNull(result);
        assertTrue(result.size() > 0);
    }
}
