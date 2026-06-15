package com.inventory.mallservice.repository;

import com.inventory.mallservice.entity.BargainRecord;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 砍价记录Repository.
 *
 * @author Inventory Team
 * @version 1.0
 * @since 3.0.0
 */
@Repository
@SuppressWarnings("null")
public interface IBargainRecordRepository extends JpaRepository<BargainRecord, Long> {

    List<BargainRecord> findByBargainDetailId(Long bargainDetailId);

    List<BargainRecord> findByHelperId(Long helperId);
}
