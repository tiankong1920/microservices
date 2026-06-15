package com.inventory.mallservice.repository;

import com.inventory.mallservice.entity.GroupBuyDetail;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 拼团详情Repository.
 *
 * @author Inventory Team
 * @version 1.0
 * @since 3.0.0
 */
@Repository
@SuppressWarnings("null")
public interface IGroupBuyDetailRepository extends JpaRepository<GroupBuyDetail, Long> {

    List<GroupBuyDetail> findByGroupBuyId(Long groupBuyId);

    List<GroupBuyDetail> findByGroupBuyIdAndStatus(Long groupBuyId, String status);

    List<GroupBuyDetail> findByLeaderIdAndStatus(Long leaderId, String status);
}
