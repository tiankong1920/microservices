package com.invoice.invoiceservice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.invoice.invoiceservice.entity.UnitInfo;

@Repository
public interface IUnitInfoRepository extends JpaRepository<UnitInfo, Long> {

    Optional<UnitInfo> findByIdAndDeletedFalse(Long id);

    List<UnitInfo> findByDeletedFalse();

    Optional<UnitInfo> findByUnitNameAndDeletedFalse(String unitName);

    @Query("SELECT u FROM UnitInfo u WHERE u.deleted = false AND " +
           "(LOWER(u.unitName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(u.aliases) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<UnitInfo> searchByKeyword(String keyword);

    @Query("SELECT u FROM UnitInfo u WHERE u.deleted = false ORDER BY u.usageCount DESC")
    List<UnitInfo> findAllOrderByUsageDesc();
}
