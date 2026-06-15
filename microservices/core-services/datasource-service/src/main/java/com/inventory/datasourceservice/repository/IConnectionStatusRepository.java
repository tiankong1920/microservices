package com.inventory.datasourceservice.repository;

import com.inventory.datasourceservice.entity.ConnectionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface IConnectionStatusRepository extends JpaRepository<ConnectionStatus, Long> {

    Optional<ConnectionStatus> findFirstByDatasourceIdOrderByCheckedAtDesc(Long datasourceId);

    List<ConnectionStatus> findByDatasourceId(Long datasourceId);

    List<ConnectionStatus> findByStatus(ConnectionStatus.ConnectionStatusEnum status);

    @Query("SELECT cs FROM ConnectionStatus cs WHERE cs.datasourceId IN :datasourceIds " +
           "AND cs.checkedAt = (SELECT MAX(cs2.checkedAt) FROM ConnectionStatus cs2 WHERE cs2.datasourceId = cs.datasourceId)")
    List<ConnectionStatus> findLatestByDatasourceIds(@Param("datasourceIds") List<Long> datasourceIds);

    @Query("SELECT COUNT(cs) FROM ConnectionStatus cs WHERE cs.status = :status " +
           "AND cs.checkedAt >= :since")
    long countByStatusSince(@Param("status") ConnectionStatus.ConnectionStatusEnum status,
                            @Param("since") LocalDateTime since);

    @Transactional
    void deleteByDatasourceId(Long datasourceId);

    @Transactional
    void deleteByCheckedAtBefore(LocalDateTime before);
}
