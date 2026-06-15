package com.inventory.mallservice.service;

import com.inventory.mallservice.entity.GroupBuy;
import com.inventory.mallservice.entity.GroupBuyDetail;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 拼团服务接口.
 *
 * @author Inventory Team
 * @version 1.0
 * @since 3.0.0
 */
public interface IGroupBuyService {

    GroupBuy createGroupBuy(GroupBuy groupBuy);

    GroupBuy getGroupBuyById(Long id);

    Page<GroupBuy> getGroupBuys(Pageable pageable);

    List<GroupBuy> getActiveGroupBuys();

    GroupBuyDetail joinGroupBuy(Long groupBuyId, Long userId);

    GroupBuyDetail createNewGroup(Long groupBuyId, Long userId);

    void checkAndCompleteGroups();

    void cancelExpiredGroups();
}
