package com.inventory.templateservice.repository;

import com.inventory.common.template.BusinessDomain;
import com.inventory.common.template.TemplateStatus;
import com.inventory.templateservice.entity.Template;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ITemplateRepository extends JpaRepository<Template, Long> {

    Optional<Template> findByTemplateCode(String templateCode);

    Optional<Template> findByTemplateCodeAndTenantId(String templateCode, String tenantId);

    List<Template> findByBusinessDomain(BusinessDomain domain);

    List<Template> findByBusinessDomainAndTenantId(BusinessDomain domain, String tenantId);

    List<Template> findByStatus(TemplateStatus status);

    List<Template> findByStatusAndTenantId(TemplateStatus status, String tenantId);

    List<Template> findByCategory(String category);

    List<Template> findByCategoryAndTenantId(String category, String tenantId);

    Page<Template> findByTenantId(String tenantId, Pageable pageable);

    boolean existsByTemplateCode(String templateCode);

    boolean existsByTemplateCodeAndTenantId(String templateCode, String tenantId);

    @Query("SELECT t FROM Template t WHERE t.tenantId = :tenantId AND " +
           "(:keyword IS NULL OR t.name LIKE CONCAT('%', :keyword, '%') OR t.templateCode LIKE CONCAT('%', :keyword, '%')) AND " +
           "(:domain IS NULL OR t.businessDomain = :domain) AND " +
           "(:status IS NULL OR t.status = :status)")
    Page<Template> search(@Param("tenantId") String tenantId,
                          @Param("keyword") String keyword,
                          @Param("domain") BusinessDomain domain,
                          @Param("status") TemplateStatus status,
                          Pageable pageable);

    @Query("SELECT COUNT(t) FROM Template t WHERE t.tenantId = :tenantId AND t.status = :status")
    long countByTenantIdAndStatus(@Param("tenantId") String tenantId, @Param("status") TemplateStatus status);

    List<Template> findByStatusIn(List<TemplateStatus> statuses);

    List<Template> findByStatusInAndTenantId(List<TemplateStatus> statuses, String tenantId);

    long countByStatus(TemplateStatus status);

    long countByBusinessDomain(BusinessDomain domain);

    long countByTenantId(String tenantId);
}
