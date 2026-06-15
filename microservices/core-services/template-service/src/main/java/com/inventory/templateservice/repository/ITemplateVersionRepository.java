package com.inventory.templateservice.repository;

import com.inventory.templateservice.entity.TemplateVersion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ITemplateVersionRepository extends JpaRepository<TemplateVersion, Long> {

    List<TemplateVersion> findByTemplate_IdOrderByChangedAtDesc(Long templateId);

    Page<TemplateVersion> findByTemplate_IdOrderByChangedAtDesc(Long templateId, Pageable pageable);

    Optional<TemplateVersion> findByTemplate_IdAndVersionNumber(Long templateId, String versionNumber);

    Optional<TemplateVersion> findFirstByTemplate_IdOrderByChangedAtDesc(Long templateId);

    @Query("SELECT v FROM TemplateVersion v WHERE v.template.id = :templateId " +
           "AND v.changedAt BETWEEN :startDate AND :endDate ORDER BY v.changedAt DESC")
    List<TemplateVersion> findByTemplateIdAndDateRange(@Param("templateId") Long templateId,
                                                        @Param("startDate") LocalDateTime startDate,
                                                        @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(v) FROM TemplateVersion v WHERE v.template.id = :templateId")
    long countByTemplateId(@Param("templateId") Long templateId);

    @Query("SELECT v.changeType, COUNT(v) FROM TemplateVersion v WHERE v.template.id = :templateId GROUP BY v.changeType")
    List<Object[]> countByChangeType(@Param("templateId") Long templateId);

    boolean existsByTemplate_IdAndVersionNumber(Long templateId, String versionNumber);
}
