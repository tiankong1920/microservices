package com.inventory.supplierservice.service.impl;

import com.inventory.supplierservice.dto.SupplierDTO;
import com.inventory.supplierservice.entity.Supplier;
import com.inventory.supplierservice.exception.SupplierNotFoundException;
import com.inventory.supplierservice.repository.ISupplierRepository;
import com.inventory.supplierservice.service.ISupplierService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class SupplierServiceImpl implements ISupplierService {

    private final ISupplierRepository supplierRepository;
    private final ModelMapper modelMapper;

    @Override
    public List<SupplierDTO> getAllSuppliers() {
        log.debug("Getting all suppliers");
        return supplierRepository.findAll().stream()
                .map(supplier -> modelMapper.map(supplier, SupplierDTO.class))
                .toList();
    }

    @Override
    public Page<SupplierDTO> getAllSuppliers(Pageable pageable) {
        log.debug("Getting all suppliers with pagination: {}", pageable);
        return supplierRepository.findAll(pageable)
                .map(supplier -> modelMapper.map(supplier, SupplierDTO.class));
    }

    @Override
    @Cacheable(value = "suppliers", key = "#id")
    public SupplierDTO getSupplierById(final Long id) {
        log.debug("Getting supplier by id: {}", id);
        final Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new SupplierNotFoundException(id));
        return modelMapper.map(supplier, SupplierDTO.class);
    }

    @Override
    public List<SupplierDTO> getActiveSuppliers() {
        log.debug("Getting active suppliers");
        return supplierRepository.findByIsActive(true, Pageable.unpaged()).stream()
                .map(supplier -> modelMapper.map(supplier, SupplierDTO.class))
                .toList();
    }

    @Override
    public Page<SupplierDTO> getActiveSuppliers(Pageable pageable) {
        log.debug("Getting active suppliers with pagination: {}", pageable);
        return supplierRepository.findByIsActive(true, pageable)
                .map(supplier -> modelMapper.map(supplier, SupplierDTO.class));
    }

    @Override
    public List<SupplierDTO> getSuppliersByProductCategory(final String productCategory) {
        log.debug("Getting suppliers by product category: {}", productCategory);
        return supplierRepository.findByProductCategory(productCategory, Pageable.unpaged()).stream()
                .map(supplier -> modelMapper.map(supplier, SupplierDTO.class))
                .toList();
    }

    @Override
    public Page<SupplierDTO> getSuppliersByProductCategory(final String productCategory, final Pageable pageable) {
        log.debug("Getting suppliers by product category with pagination: {}", productCategory);
        return supplierRepository.findByProductCategory(productCategory, pageable)
                .map(supplier -> modelMapper.map(supplier, SupplierDTO.class));
    }

    @Override
    public Page<SupplierDTO> searchSuppliers(final String keyword, final Pageable pageable) {
        log.debug("Searching suppliers with keyword: {}", keyword);
        return supplierRepository.searchByKeyword(keyword, pageable)
                .map(supplier -> modelMapper.map(supplier, SupplierDTO.class));
    }

    @Override
    public Page<SupplierDTO> searchSuppliersByName(final String name, final Pageable pageable) {
        log.debug("Searching suppliers by name: {}", name);
        return supplierRepository.findByNameContainingIgnoreCase(name, pageable)
                .map(supplier -> modelMapper.map(supplier, SupplierDTO.class));
    }

    @Override
    @CacheEvict(value = "suppliers", allEntries = true)
    @Transactional
    public SupplierDTO createSupplier(final SupplierDTO supplierDTO) {
        log.debug("Creating supplier: {}", supplierDTO.getName());

        if (supplierRepository.findByEmail(supplierDTO.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Supplier with email " + supplierDTO.getEmail() + " already exists");
        }

        final Supplier supplier = modelMapper.map(supplierDTO, Supplier.class);
        supplier.setActive(true);
        final Supplier savedSupplier = supplierRepository.save(supplier);
        log.info("Supplier created successfully with id: {}", savedSupplier.getId());
        return modelMapper.map(savedSupplier, SupplierDTO.class);
    }

    @Override
    @CacheEvict(value = "suppliers", allEntries = true)
    @Transactional
    public SupplierDTO updateSupplier(final Long id, final SupplierDTO supplierDTO) {
        log.debug("Updating supplier with id: {}", id);

        final Supplier existingSupplier = supplierRepository.findById(id)
                .orElseThrow(() -> new SupplierNotFoundException(id));

        if (supplierDTO.getEmail() != null &&
            !supplierDTO.getEmail().equals(existingSupplier.getEmail()) &&
            supplierRepository.findByEmail(supplierDTO.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Supplier with email " + supplierDTO.getEmail() + " already exists");
        }

        modelMapper.map(supplierDTO, existingSupplier);
        final Supplier updatedSupplier = supplierRepository.save(existingSupplier);
        log.info("Supplier updated successfully with id: {}", updatedSupplier.getId());
        return modelMapper.map(updatedSupplier, SupplierDTO.class);
    }

    @Override
    @CacheEvict(value = "suppliers", allEntries = true)
    @Transactional
    public void deleteSupplier(final Long id) {
        log.debug("Deleting supplier with id: {}", id);
        if (!supplierRepository.existsById(id)) {
            throw new SupplierNotFoundException(id);
        }
        supplierRepository.deleteById(id);
        log.info("Supplier deleted successfully with id: {}", id);
    }

    @Override
    @CacheEvict(value = "suppliers", allEntries = true)
    @Transactional
    public SupplierDTO activateSupplier(final Long id) {
        log.debug("Activating supplier with id: {}", id);
        final Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new SupplierNotFoundException(id));
        supplier.setActive(true);
        final Supplier updatedSupplier = supplierRepository.save(supplier);
        log.info("Supplier activated with id: {}", id);
        return modelMapper.map(updatedSupplier, SupplierDTO.class);
    }

    @Override
    @CacheEvict(value = "suppliers", allEntries = true)
    @Transactional
    public SupplierDTO deactivateSupplier(final Long id) {
        log.debug("Deactivating supplier with id: {}", id);
        final Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new SupplierNotFoundException(id));
        supplier.setActive(false);
        final Supplier updatedSupplier = supplierRepository.save(supplier);
        log.info("Supplier deactivated with id: {}", id);
        return modelMapper.map(updatedSupplier, SupplierDTO.class);
    }

    @Override
    public boolean existsByEmail(final String email) {
        return supplierRepository.findByEmail(email).isPresent();
    }
}
