package com.inventory.mallservice.repository;

import com.inventory.mallservice.entity.ShoppingCart;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 购物车Repository.
 *
 * @author Inventory Team
 * @version 1.0
 * @since 3.0.0
 */
@Repository
@SuppressWarnings("null")
public interface IShoppingCartRepository extends JpaRepository<ShoppingCart, Long> {

    List<ShoppingCart> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<ShoppingCart> findByUserIdAndCheckedTrue(Long userId);

    @Transactional
    void deleteByUserId(Long userId);

    Integer countByUserId(Long userId);
}
