package com.inventory.inventoryservice.service.impl;

import com.inventory.inventoryservice.dto.WarehouseDTO;
import com.inventory.inventoryservice.entity.Warehouse;
import com.inventory.inventoryservice.exception.WarehouseNotFoundException;
import com.inventory.inventoryservice.repository.IWarehouseRepository;
import com.inventory.inventoryservice.service.IWarehouseService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class WarehouseServiceImpl implements IWarehouseService {

    private static final org.slf4j.Logger LOG =
            org.slf4j.LoggerFactory.getLogger(WarehouseServiceImpl.class);

    private final IWarehouseRepository warehouseRepository;
    private final ModelMapper modelMapper;

    private static final String ERROR_WAREHOUSE_NOT_FOUND = "Warehouse not found with id: ";
    private static final String ERROR_WAREHOUSE_NOT_FOUND_CODE = "Warehouse not found with code: ";
    private static final String ERROR_WAREHOUSE_CODE_EXISTS = "Warehouse with code ";
    private static final String LOG_WAREHOUSE_NOT_FOUND_ID = ERROR_WAREHOUSE_NOT_FOUND;

    @Override
    @Cacheable(value = "warehouses")
    public List<WarehouseDTO> getAllWarehouses() {
        LOG.info("Getting all warehouses");
        final List<Warehouse> warehouses = warehouseRepository.findAll();
        LOG.info("Found {} warehouses", warehouses.size());
        return warehouses.stream()
                .map(warehouse -> modelMapper.map(warehouse, WarehouseDTO.class))
                .toList();
    }

    @Override
    @Cacheable(value = "warehouses", key = "#id")
    public WarehouseDTO getWarehouseById(Long id) {
        LOG.info("Getting warehouse by id: {}", id);
        final Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> {
                    LOG.error(LOG_WAREHOUSE_NOT_FOUND_ID, id);
                    return new WarehouseNotFoundException(ERROR_WAREHOUSE_NOT_FOUND + id);
                });
        return modelMapper.map(warehouse, WarehouseDTO.class);
    }

    @Override
    @Cacheable(value = "warehouses", key = "#warehouseCode")
    public WarehouseDTO getWarehouseByCode(String warehouseCode) {
        LOG.info("Getting warehouse by code: {}", warehouseCode);
        final Warehouse warehouse = warehouseRepository.findByWarehouseCode(warehouseCode)
                .orElseThrow(() -> {
                    LOG.error("Warehouse not found with code: {}", warehouseCode);
                    return new WarehouseNotFoundException(ERROR_WAREHOUSE_NOT_FOUND_CODE + warehouseCode);
                });
        return modelMapper.map(warehouse, WarehouseDTO.class);
    }

    @Override
    @Cacheable(value = "warehouses", key = "'city-' + #city")
    public List<WarehouseDTO> getWarehousesByCity(String city) {
        LOG.info("Getting warehouses by city: {}", city);
        final List<Warehouse> warehouses = warehouseRepository.findByCity(city);
        LOG.info("Found {} warehouses in city: {}", warehouses.size(), city);
        return warehouses.stream()
                .map(warehouse -> modelMapper.map(warehouse, WarehouseDTO.class))
                .toList();
    }

    @Override
    @Cacheable(value = "warehouses", key = "'province-' + #province")
    public List<WarehouseDTO> getWarehousesByProvince(String province) {
        LOG.info("Getting warehouses by province: {}", province);
        final List<Warehouse> warehouses = warehouseRepository.findByProvince(province);
        LOG.info("Found {} warehouses in province: {}", warehouses.size(), province);
        return warehouses.stream()
                .map(warehouse -> modelMapper.map(warehouse, WarehouseDTO.class))
                .toList();
    }

    @Override
    @Cacheable(value = "warehouses", key = "'country-' + #country")
    public List<WarehouseDTO> getWarehousesByCountry(String country) {
        LOG.info("Getting warehouses by country: {}", country);
        final List<Warehouse> warehouses = warehouseRepository.findByCountry(country);
        LOG.info("Found {} warehouses in country: {}", warehouses.size(), country);
        return warehouses.stream()
                .map(warehouse -> modelMapper.map(warehouse, WarehouseDTO.class))
                .toList();
    }

    @Override
    @Cacheable(value = "warehouses", key = "'active'")
    public List<WarehouseDTO> getActiveWarehouses() {
        LOG.info("Getting active warehouses");
        final List<Warehouse> warehouses = warehouseRepository.findByIsActiveTrue();
        LOG.info("Found {} active warehouses", warehouses.size());
        return warehouses.stream()
                .map(warehouse -> modelMapper.map(warehouse, WarehouseDTO.class))
                .toList();
    }

    @Override
    @Cacheable(value = "warehouses", key = "'primary'")
    public List<WarehouseDTO> getPrimaryWarehouses() {
        LOG.info("Getting primary warehouses");
        final List<Warehouse> warehouses = warehouseRepository.findByIsPrimaryTrue();
        LOG.info("Found {} primary warehouses", warehouses.size());
        return warehouses.stream()
                .map(warehouse -> modelMapper.map(warehouse, WarehouseDTO.class))
                .toList();
    }

    @Override
    @CacheEvict(value = "warehouses", allEntries = true)
    @Transactional
    public WarehouseDTO createWarehouse(WarehouseDTO warehouseDTO) {
        LOG.info("Creating warehouse with code: {}", warehouseDTO.getWarehouseCode());

        if (warehouseRepository.existsByWarehouseCode(warehouseDTO.getWarehouseCode())) {
            LOG.error("Warehouse with code {} already exists", warehouseDTO.getWarehouseCode());
            throw new IllegalStateException(
                    ERROR_WAREHOUSE_CODE_EXISTS + warehouseDTO.getWarehouseCode() + " already exists");
        }

        final Warehouse warehouse = modelMapper.map(warehouseDTO, Warehouse.class);
        final Warehouse savedWarehouse = warehouseRepository.save(warehouse);

        LOG.info("Warehouse created successfully with id: {}", savedWarehouse.getId());
        return modelMapper.map(savedWarehouse, WarehouseDTO.class);
    }

    @Override
    @CacheEvict(value = "warehouses", allEntries = true)
    @Transactional
    public WarehouseDTO updateWarehouse(Long id, WarehouseDTO warehouseDTO) {
        LOG.info("Updating warehouse with id: {}", id);

        final Warehouse existingWarehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> {
                    LOG.error(LOG_WAREHOUSE_NOT_FOUND_ID, id);
                    return new WarehouseNotFoundException(ERROR_WAREHOUSE_NOT_FOUND + id);
                });

        if (!existingWarehouse.getWarehouseCode().equals(warehouseDTO.getWarehouseCode())
                && warehouseRepository.existsByWarehouseCode(warehouseDTO.getWarehouseCode())) {
                LOG.error("Warehouse with code {} already exists", warehouseDTO.getWarehouseCode());
                throw new IllegalStateException(
                        ERROR_WAREHOUSE_CODE_EXISTS + warehouseDTO.getWarehouseCode() + " already exists");
            }

        modelMapper.map(warehouseDTO, existingWarehouse);
        final Warehouse updatedWarehouse = warehouseRepository.save(existingWarehouse);

        LOG.info("Warehouse updated successfully with id: {}", updatedWarehouse.getId());
        return modelMapper.map(updatedWarehouse, WarehouseDTO.class);
    }

    @Override
    @CacheEvict(value = "warehouses", allEntries = true)
    @Transactional
    public void deleteWarehouse(Long id) {
        LOG.info("Deleting warehouse with id: {}", id);

        if (!warehouseRepository.existsById(id)) {
            LOG.error(LOG_WAREHOUSE_NOT_FOUND_ID, id);
            throw new WarehouseNotFoundException(ERROR_WAREHOUSE_NOT_FOUND + id);
        }

        warehouseRepository.deleteById(id);
        LOG.info("Warehouse deleted successfully with id: {}", id);
    }

    @Override
    @CacheEvict(value = "warehouses", allEntries = true)
    @Transactional
    public void activateWarehouse(Long id) {
        LOG.info("Activating warehouse with id: {}", id);

        final Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> {
                    LOG.error(LOG_WAREHOUSE_NOT_FOUND_ID, id);
                    return new WarehouseNotFoundException(ERROR_WAREHOUSE_NOT_FOUND + id);
                });

        warehouse.setActive(true);
        final Warehouse updatedWarehouse = warehouseRepository.save(warehouse);

        LOG.info("Warehouse activated successfully with id: {}", updatedWarehouse.getId());
    }

    @Override
    @CacheEvict(value = "warehouses", allEntries = true)
    @Transactional
    public void deactivateWarehouse(Long id) {
        LOG.info("Deactivating warehouse with id: {}", id);

        final Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> {
                    LOG.error(LOG_WAREHOUSE_NOT_FOUND_ID, id);
                    return new WarehouseNotFoundException(ERROR_WAREHOUSE_NOT_FOUND + id);
                });

        warehouse.setActive(false);
        final Warehouse updatedWarehouse = warehouseRepository.save(warehouse);

        LOG.info("Warehouse deactivated successfully with id: {}", updatedWarehouse.getId());
    }
}
