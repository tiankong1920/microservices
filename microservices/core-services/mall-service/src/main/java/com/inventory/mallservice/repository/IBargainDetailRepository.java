package com.inventory.mallservice.repository;

import com.inventory.mallservice.entity.BargainDetail;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 砍价详情Repository.
 *
 * @author Inventory Team
 * @version 1.0
 * @since 3.0.0
 */
@Repository
@SuppressWarnings("null")
public interface IBargainDetailRepository extends JpaRepository<BargainDetail, Long> {

    List<BargainDetail> findByActivityId(Long activityId);

    List<BargainDetail> findByUserIdAndStatus(Long userId, String status);
}
