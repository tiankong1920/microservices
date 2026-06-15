package com.inventory.supplierservice.service;

import com.inventory.supplierservice.dto.SupplierDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ISupplierService {

    List<SupplierDTO> getAllSuppliers();

    Page<SupplierDTO> getAllSuppliers(Pageable pageable);

    SupplierDTO getSupplierById(Long id);

    List<SupplierDTO> getActiveSuppliers();

    Page<SupplierDTO> getActiveSuppliers(Pageable pageable);

    List<SupplierDTO> getSuppliersByProductCategory(String productCategory);

    Page<SupplierDTO> getSuppliersByProductCategory(String productCategory, Pageable pageable);

    Page<SupplierDTO> searchSuppliers(String keyword, Pageable pageable);

    Page<SupplierDTO> searchSuppliersByName(String name, Pageable pageable);

    SupplierDTO createSupplier(SupplierDTO supplierDTO);

    SupplierDTO updateSupplier(Long id, SupplierDTO supplierDTO);

    void deleteSupplier(Long id);

    SupplierDTO activateSupplier(Long id);

    SupplierDTO deactivateSupplier(Long id);

    boolean existsByEmail(String email);
}
