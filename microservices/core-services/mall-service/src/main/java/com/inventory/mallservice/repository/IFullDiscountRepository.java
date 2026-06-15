package com.inventory.mallservice.repository;

import com.inventory.mallservice.entity.FullDiscount;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 满减Repository.
 *
 * @author Inventory Team
 * @version 1.0
 * @since 3.0.0
 */
@Repository
@SuppressWarnings("null")
public interface IFullDiscountRepository extends JpaRepository<FullDiscount, Long> {

    List<FullDiscount> findByStatus(String status);
}
