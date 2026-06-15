package com.inventory.datasourceservice.repository;

import com.inventory.datasourceservice.entity.AlertConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface IAlertConfigRepository extends JpaRepository<AlertConfig, Long> {

    List<AlertConfig> findByTenantId(String tenantId);

    List<AlertConfig> findByTenantIdAndEnabled(String tenantId, Boolean enabled);

    Optional<AlertConfig> findByIdAndTenantId(Long id, String tenantId);

    @Query("SELECT ac FROM AlertConfig ac WHERE ac.tenantId = :tenantId " +
           "AND ac.enabled = true " +
           "AND (:datasourceId IS NULL OR JSON_CONTAINS(ac.datasourceIds, CAST(:datasourceId AS JSON)))")
    List<AlertConfig> findActiveByTenantIdAndDatasourceId(@Param("tenantId") String tenantId,
                                                           @Param("datasourceId") Long datasourceId);

    @Transactional
    void deleteByIdAndTenantId(Long id, String tenantId);
}
