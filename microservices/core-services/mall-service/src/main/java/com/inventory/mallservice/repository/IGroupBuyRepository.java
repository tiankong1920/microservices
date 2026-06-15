package com.inventory.mallservice.repository;

import com.inventory.mallservice.entity.GroupBuy;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 拼团Repository.
 *
 * @author Inventory Team
 * @version 1.0
 * @since 3.0.0
 */
@Repository
@SuppressWarnings("null")
public interface IGroupBuyRepository extends JpaRepository<GroupBuy, Long> {

    List<GroupBuy> findByStatus(String status);
}
