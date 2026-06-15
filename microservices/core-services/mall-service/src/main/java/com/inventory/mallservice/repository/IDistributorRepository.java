package com.inventory.mallservice.repository;

import com.inventory.mallservice.entity.Distributor;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 分销Repository.
 *
 * @author Inventory Team
 * @version 1.0
 * @since 3.0.0
 */
@Repository
@SuppressWarnings("null")
public interface IDistributorRepository extends JpaRepository<Distributor, Long> {

    Optional<Distributor> findByUserId(Long userId);

    Optional<Distributor> findByDistributorCode(String code);

    List<Distributor> findByParentId(Long parentId);
}
