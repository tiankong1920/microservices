package com.inventory.datasourceservice.repository;

import com.inventory.datasourceservice.entity.DatasourceConfig;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface IDatasourceConfigRepository extends JpaRepository<DatasourceConfig, Long> {

    Page<DatasourceConfig> findByTenantId(String tenantId, Pageable pageable);

    Optional<DatasourceConfig> findByIdAndTenantId(Long id, String tenantId);

    List<DatasourceConfig> findByTenantIdAndStatus(String tenantId, DatasourceConfig.DatasourceStatus status);

    List<DatasourceConfig> findByTenantIdAndType(String tenantId, DatasourceConfig.DatasourceType type);

    boolean existsByNameAndTenantId(String name, String tenantId);

    @Query("SELECT d FROM DatasourceConfig d WHERE d.tenantId = :tenantId AND " +
           "(:name IS NULL OR d.name LIKE CONCAT('%', :name, '%')) AND " +
           "(:type IS NULL OR d.type = :type) AND " +
           "(:status IS NULL OR d.status = :status)")
    Page<DatasourceConfig> search(@Param("tenantId") String tenantId,
                                   @Param("name") String name,
                                   @Param("type") DatasourceConfig.DatasourceType type,
                                   @Param("status") DatasourceConfig.DatasourceStatus status,
                                   Pageable pageable);

    @Query("SELECT COUNT(d) FROM DatasourceConfig d WHERE d.tenantId = :tenantId AND d.status = :status")
    long countByTenantIdAndStatus(@Param("tenantId") String tenantId, 
                                   @Param("status") DatasourceConfig.DatasourceStatus status);

    @Transactional
    void deleteByIdAndTenantId(Long id, String tenantId);
}
