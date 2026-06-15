package com.inventory.supplierservice.repository;

import com.inventory.supplierservice.entity.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ISupplierRepository extends JpaRepository<Supplier, Long> {

    Optional<Supplier> findByEmail(String email);

    Page<Supplier> findByIsActive(boolean isActive, Pageable pageable);

    Page<Supplier> findByProductCategory(String productCategory, Pageable pageable);

    @Query("SELECT s FROM Supplier s WHERE " +
           "LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(s.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(s.contactPerson) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Supplier> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    Page<Supplier> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
