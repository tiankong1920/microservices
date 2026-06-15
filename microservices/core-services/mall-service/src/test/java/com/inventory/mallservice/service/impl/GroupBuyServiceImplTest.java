package com.inventory.mallservice.service.impl;

import com.inventory.mallservice.entity.GroupBuy;
import com.inventory.mallservice.entity.GroupBuyDetail;
import com.inventory.mallservice.repository.IGroupBuyDetailRepository;
import com.inventory.mallservice.repository.IGroupBuyRepository;

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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("拼团服务测试")
@SuppressWarnings("null")
class GroupBuyServiceImplTest {

    @Mock
    private IGroupBuyRepository groupBuyRepository;

    @Mock
    private IGroupBuyDetailRepository groupBuyDetailRepository;

    private GroupBuyServiceImpl groupBuyService;

    private GroupBuy testGroupBuy;
    private GroupBuyDetail testDetail;

    @BeforeEach
    void setUp() {
        groupBuyService = new GroupBuyServiceImpl(groupBuyRepository, groupBuyDetailRepository);

        testGroupBuy = new GroupBuy();
        testGroupBuy.setId(1L);
        testGroupBuy.setName("3人团优惠");
        testGroupBuy.setGroupSize(3);
        testGroupBuy.setGroupPrice(new BigDecimal("99.00"));
        testGroupBuy.setSinglePrice(new BigDecimal("129.00"));
        testGroupBuy.setStatus("ACTIVE");

        testDetail = new GroupBuyDetail();
        testDetail.setId(1L);
        testDetail.setGroupBuy(testGroupBuy);
        testDetail.setGroupNo("GRP12345678");
        testDetail.setLeaderId(100L);
        testDetail.setCurrentMembers(1);
        testDetail.setStatus("PENDING");
        testDetail.setExpiredAt(LocalDateTime.now().plusHours(24));
    }

    @Test
    @DisplayName("创建拼团活动")
    void createGroupBuy() {
        when(groupBuyRepository.save(any(GroupBuy.class))).thenReturn(testGroupBuy);

        GroupBuy result = groupBuyService.createGroupBuy(testGroupBuy);

        assertNotNull(result);
        assertEquals("3人团优惠", result.getName());
        verify(groupBuyRepository).save(any(GroupBuy.class));
    }

    @Test
    @DisplayName("根据ID获取拼团活动")
    void getGroupBuyById() {
        when(groupBuyRepository.findById(1L)).thenReturn(Optional.of(testGroupBuy));

        GroupBuy result = groupBuyService.getGroupBuyById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    @DisplayName("获取不存在的拼团活动应抛出异常")
    void getGroupBuyByIdNotFound() {
        when(groupBuyRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> groupBuyService.getGroupBuyById(999L));
    }

    @Test
    @DisplayName("加入已有拼团")
    void joinExistingGroup() {
        when(groupBuyDetailRepository.findByGroupBuyIdAndStatus(1L, "PENDING"))
                .thenReturn(List.of(testDetail));
        when(groupBuyRepository.findById(1L)).thenReturn(Optional.of(testGroupBuy));
        when(groupBuyDetailRepository.save(any(GroupBuyDetail.class))).thenReturn(testDetail);

        GroupBuyDetail result = groupBuyService.joinGroupBuy(1L, 200L);

        assertNotNull(result);
        verify(groupBuyDetailRepository).save(any(GroupBuyDetail.class));
    }

    @Test
    @DisplayName("无待成团时创建新团")
    void joinGroupCreateNewWhenNoPending() {
        when(groupBuyDetailRepository.findByGroupBuyIdAndStatus(1L, "PENDING"))
                .thenReturn(List.of());
        when(groupBuyRepository.findById(1L)).thenReturn(Optional.of(testGroupBuy));
        when(groupBuyDetailRepository.save(any(GroupBuyDetail.class))).thenReturn(testDetail);

        GroupBuyDetail result = groupBuyService.joinGroupBuy(1L, 200L);

        assertNotNull(result);
        verify(groupBuyDetailRepository).save(any(GroupBuyDetail.class));
    }

    @Test
    @DisplayName("获取活跃拼团活动")
    void getActiveGroupBuys() {
        when(groupBuyRepository.findByStatus("ACTIVE")).thenReturn(List.of(testGroupBuy));

        List<GroupBuy> result = groupBuyService.getActiveGroupBuys();

        assertNotNull(result);
        assertEquals(1, result.size());
    }
}
