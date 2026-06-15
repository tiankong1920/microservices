package com.invoice.invoiceservice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.invoice.invoiceservice.entity.InvoiceProduct;

@Repository
public interface IInvoiceProductRepository extends JpaRepository<InvoiceProduct, Long> {

    Optional<InvoiceProduct> findByIdAndDeletedFalse(Long id);

    List<InvoiceProduct> findByDeletedFalse();

    List<InvoiceProduct> findByDeletedFalseAndStatus(InvoiceProduct.ProductStatus status);

    @Query("SELECT p FROM InvoiceProduct p WHERE p.deleted = false AND " +
           "(LOWER(p.productName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(p.specification) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<InvoiceProduct> searchByKeyword(@Param("keyword") String keyword);

    @Query("SELECT p FROM InvoiceProduct p WHERE p.deleted = false AND " +
           "(LOWER(p.productName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(p.specification) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY p.usageCount DESC, p.lastUsedAt DESC")
    List<InvoiceProduct> searchByKeywordOrderByUsage(@Param("keyword") String keyword);

    @Query("SELECT p FROM InvoiceProduct p WHERE p.deleted = false AND p.status = 'ENABLED' " +
           "ORDER BY p.usageCount DESC, p.lastUsedAt DESC")
    List<InvoiceProduct> findAllOrderByUsageDesc();
}
