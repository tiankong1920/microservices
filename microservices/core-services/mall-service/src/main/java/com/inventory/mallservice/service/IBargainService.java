package com.inventory.mallservice.service;

import com.inventory.mallservice.entity.BargainActivity;
import com.inventory.mallservice.entity.BargainDetail;
import com.inventory.mallservice.entity.BargainRecord;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

/**
 * 砍价服务接口.
 *
 * @author Inventory Team
 * @version 1.0
 * @since 3.0.0
 */
public interface IBargainService {

    BargainActivity createBargainActivity(BargainActivity activity);

    BargainDetail initiateBargain(Long activityId, Long userId);

    BargainRecord helpBargain(Long bargainDetailId, Long helperId);

    BargainDetail getBargainDetail(Long id);

    List<BargainDetail> getUserBargains(Long userId, String status);

    Page<BargainActivity> getBargainActivities(Pageable pageable);

    List<BargainActivity> getActiveBargainActivities();

    BigDecimal calculateBargainAmount(BargainDetail detail);
}
