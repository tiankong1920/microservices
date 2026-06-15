package com.inventory.mallservice.repository;

import com.inventory.mallservice.entity.FlashSale;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 秒杀Repository.
 *
 * @author Inventory Team
 * @version 1.0
 * @since 3.0.0
 */
@Repository
@SuppressWarnings("null")
public interface IFlashSaleRepository extends JpaRepository<FlashSale, Long> {

    List<FlashSale> findByStatus(String status);
}
