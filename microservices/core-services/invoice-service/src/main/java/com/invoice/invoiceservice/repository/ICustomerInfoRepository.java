package com.invoice.invoiceservice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.invoice.invoiceservice.entity.CustomerInfo;

@Repository
public interface ICustomerInfoRepository extends JpaRepository<CustomerInfo, Long> {

    Optional<CustomerInfo> findByIdAndDeletedFalse(Long id);

    List<CustomerInfo> findByDeletedFalse();

    List<CustomerInfo> findByDeletedFalseAndStatus(CustomerInfo.CustomerStatus status);

    boolean existsByTaxNumberAndDeletedFalse(String taxNumber);

    boolean existsByTaxNumberAndDeletedFalseAndIdNot(String taxNumber, Long id);

    @Query("SELECT c FROM CustomerInfo c WHERE c.deleted = false AND " +
           "(LOWER(c.customerName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR c.taxNumber LIKE CONCAT('%', :keyword, '%') " +
           "OR LOWER(c.registeredAddress) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<CustomerInfo> searchByKeyword(@Param("keyword") String keyword);

    @Query("SELECT c FROM CustomerInfo c WHERE c.deleted = false AND " +
           "c.pinyinInitials LIKE CONCAT(:initials, '%')")
    List<CustomerInfo> findByPinyinInitials(@Param("initials") String initials);

    @Query("SELECT c FROM CustomerInfo c WHERE c.deleted = false AND " +
           "c.pinyinFull LIKE LOWER(CONCAT('%', :pinyin, '%'))")
    List<CustomerInfo> findByPinyinFull(@Param("pinyin") String pinyin);

    @Query("SELECT c FROM CustomerInfo c WHERE c.deleted = false AND c.status = 'ENABLED' " +
           "ORDER BY c.usageCount DESC, c.lastUsedAt DESC")
    List<CustomerInfo> findAllOrderByUsageDesc();

    @Query("SELECT c FROM CustomerInfo c WHERE c.deleted = false AND c.status = 'ENABLED' AND " +
           "(LOWER(c.customerName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR c.taxNumber LIKE CONCAT('%', :keyword, '%')) " +
           "ORDER BY c.usageCount DESC, c.lastUsedAt DESC")
    List<CustomerInfo> searchByKeywordOrderByUsage(@Param("keyword") String keyword);

    @Query("SELECT c FROM CustomerInfo c WHERE c.deleted = false AND c.tenantId = :tenantId")
    List<CustomerInfo> findByTenantId(@Param("tenantId") String tenantId);

    List<CustomerInfo> findByCustomerNameInAndDeletedFalse(List<String> names);

    List<CustomerInfo> findByIdInAndDeletedFalseAndStatus(List<Long> ids, CustomerInfo.CustomerStatus status);
}
