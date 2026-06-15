package com.inventory.inventoryservice.repository;

import com.inventory.inventoryservice.entity.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IWarehouseRepository extends JpaRepository<Warehouse, Long> {

    Optional<Warehouse> findByWarehouseCode(String warehouseCode);

    boolean existsByWarehouseCode(String warehouseCode);

    List<Warehouse> findByCity(String city);

    List<Warehouse> findByProvince(String province);

    List<Warehouse> findByCountry(String country);

    List<Warehouse> findByPrimaryTrue();

    List<Warehouse> findByActiveTrue();

    List<Warehouse> findByIsActiveTrue();

    List<Warehouse> findByIsPrimaryTrue();
}
