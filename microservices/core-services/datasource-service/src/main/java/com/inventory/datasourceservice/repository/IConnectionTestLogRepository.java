package com.inventory.datasourceservice.repository;

import com.inventory.datasourceservice.entity.ConnectionTestLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface IConnectionTestLogRepository extends JpaRepository<ConnectionTestLog, Long> {

    Page<ConnectionTestLog> findByDatasourceId(Long datasourceId, Pageable pageable);

    List<ConnectionTestLog> findByDatasourceIdAndTestedAtAfter(Long datasourceId, LocalDateTime after);

    @Query("SELECT ctl FROM ConnectionTestLog ctl WHERE ctl.datasourceId = :datasourceId " +
           "AND (:result IS NULL OR ctl.result = :result) " +
           "AND (:testType IS NULL OR ctl.testType = :testType) " +
           "AND (:startTime IS NULL OR ctl.testedAt >= :startTime) " +
           "AND (:endTime IS NULL OR ctl.testedAt <= :endTime)")
    Page<ConnectionTestLog> search(@Param("datasourceId") Long datasourceId,
                                    @Param("result") ConnectionTestLog.TestResult result,
                                    @Param("testType") ConnectionTestLog.TestType testType,
                                    @Param("startTime") LocalDateTime startTime,
                                    @Param("endTime") LocalDateTime endTime,
                                    Pageable pageable);

    @Query("SELECT COUNT(ctl) FROM ConnectionTestLog ctl WHERE ctl.result = :result " +
           "AND ctl.testedAt >= :since")
    long countByResultSince(@Param("result") ConnectionTestLog.TestResult result,
                            @Param("since") LocalDateTime since);

    @Query("SELECT AVG(ctl.responseTime) FROM ConnectionTestLog ctl WHERE ctl.datasourceId = :datasourceId " +
           "AND ctl.testedAt >= :since AND ctl.result = 'SUCCESS'")
    Double getAverageResponseTime(@Param("datasourceId") Long datasourceId,
                                   @Param("since") LocalDateTime since);

    @Transactional
    void deleteByTestedAtBefore(LocalDateTime before);
}
