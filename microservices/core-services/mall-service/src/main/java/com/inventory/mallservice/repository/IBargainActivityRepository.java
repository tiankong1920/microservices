package com.inventory.mallservice.repository;

import com.inventory.mallservice.entity.BargainActivity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 砍价活动Repository.
 *
 * @author Inventory Team
 * @version 1.0
 * @since 3.0.0
 */
@Repository
@SuppressWarnings("null")
public interface IBargainActivityRepository extends JpaRepository<BargainActivity, Long> {

    List<BargainActivity> findByStatus(String status);
}
