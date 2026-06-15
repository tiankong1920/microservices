package com.inventory.mallservice.service.impl;

import com.inventory.common.core.exception.EntityNotFoundException;
import com.inventory.mallservice.entity.GroupBuy;
import com.inventory.mallservice.entity.GroupBuyDetail;
import com.inventory.mallservice.repository.IGroupBuyDetailRepository;
import com.inventory.mallservice.repository.IGroupBuyRepository;
import com.inventory.mallservice.service.IGroupBuyService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 拼团服务实现类.
 *
 * @author Inventory Team
 * @version 1.0
 * @since 3.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("null")
public class GroupBuyServiceImpl implements IGroupBuyService {

    private final IGroupBuyRepository groupBuyRepository;
    private final IGroupBuyDetailRepository groupBuyDetailRepository;

    @Override
    @Transactional
    public GroupBuy createGroupBuy(final GroupBuy groupBuy) {
        log.info("Creating group buy: {}", groupBuy.getName());
        return groupBuyRepository.save(groupBuy);
    }

    @Override
    public GroupBuy getGroupBuyById(final Long id) {
        return groupBuyRepository.findById(id)
                .orElseThrow(() -> EntityNotFoundException.forEntity("GroupBuy", id));
    }

    @Override
    public Page<GroupBuy> getGroupBuys(final Pageable pageable) {
        return groupBuyRepository.findAll(pageable);
    }

    @Override
    public List<GroupBuy> getActiveGroupBuys() {
        return groupBuyRepository.findByStatus("ACTIVE");
    }

    @Override
    @Transactional
    public GroupBuyDetail joinGroupBuy(final Long groupBuyId, final Long userId) {
        log.info("User {} joining group buy {}", userId, groupBuyId);
        List<GroupBuyDetail> pendingGroups = groupBuyDetailRepository
                .findByGroupBuyIdAndStatus(groupBuyId, "PENDING");
        if (!pendingGroups.isEmpty()) {
            GroupBuyDetail detail = pendingGroups.get(0);
            detail.setCurrentMembers(detail.getCurrentMembers() + 1);
            GroupBuy groupBuy = getGroupBuyById(groupBuyId);
            if (detail.getCurrentMembers() >= groupBuy.getGroupSize()) {
                detail.setStatus("SUCCESS");
                detail.setCompletedAt(LocalDateTime.now());
            }
            return groupBuyDetailRepository.save(detail);
        }
        return createNewGroup(groupBuyId, userId);
    }

    @Override
    @Transactional
    public GroupBuyDetail createNewGroup(final Long groupBuyId, final Long userId) {
        log.info("User {} creating new group for group buy {}", userId, groupBuyId);
        GroupBuy groupBuy = getGroupBuyById(groupBuyId);
        GroupBuyDetail detail = new GroupBuyDetail();
        detail.setGroupBuy(groupBuy);
        detail.setGroupNo("GRP" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        detail.setLeaderId(userId);
        detail.setCurrentMembers(1);
        detail.setStatus("PENDING");
        detail.setExpiredAt(LocalDateTime.now().plusHours(24));
        return groupBuyDetailRepository.save(detail);
    }

    @Override
    @Transactional
    public void checkAndCompleteGroups() {
        log.info("Checking and completing group buys");
    }

    @Override
    @Transactional
    public void cancelExpiredGroups() {
        log.info("Cancelling expired groups");
    }
}
