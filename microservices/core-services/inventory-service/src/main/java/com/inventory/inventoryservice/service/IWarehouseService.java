package com.inventory.inventoryservice.service;

import com.inventory.inventoryservice.dto.WarehouseDTO;
import com.inventory.inventoryservice.exception.WarehouseNotFoundException;

import java.util.List;

/**
 * Service interface for warehouse management operations.
 * Provides methods for managing warehouses, including creation, retrieval, update, and deletion.
 */
public interface IWarehouseService {

    /**
 * Retrieves all warehouse records from the database.
     *
 * @return a list of all warehouse records as WarehouseDTO objects
     */
    List<WarehouseDTO> getAllWarehouses();

    /**
 * Retrieves a specific warehouse record by its unique identifier.
     *
 * @param id the unique identifier of the warehouse record to retrieve
 * @return the warehouse record with the specified id as a WarehouseDTO object
 * @throws WarehouseNotFoundException if no warehouse record with the specified id exists
     */
    WarehouseDTO getWarehouseById(Long id);

    /**
 * Retrieves a warehouse record by its unique warehouse code.
     *
 * @param warehouseCode the unique code of the warehouse to retrieve
 * @return the warehouse record with the specified code as a WarehouseDTO object
 * @throws WarehouseNotFoundException if no warehouse record with the specified code exists
     */
    WarehouseDTO getWarehouseByCode(String warehouseCode);

    /**
 * Retrieves all warehouse records located in a specific city.
     *
 * @param city the city where the warehouses are located
 * @return a list of warehouse records in the specified city as WarehouseDTO objects
     */
    List<WarehouseDTO> getWarehousesByCity(String city);

    /**
 * Retrieves all warehouse records located in a specific province.
     *
 * @param province the province where the warehouses are located
 * @return a list of warehouse records in the specified province as WarehouseDTO objects
     */
    List<WarehouseDTO> getWarehousesByProvince(String province);

    /**
 * Retrieves all warehouse records located in a specific country.
     *
 * @param country the country where the warehouses are located
 * @return a list of warehouse records in the specified country as WarehouseDTO objects
     */
    List<WarehouseDTO> getWarehousesByCountry(String country);

    /**
 * Retrieves all active warehouse records.
     *
 * @return a list of active warehouse records as WarehouseDTO objects
     */
    List<WarehouseDTO> getActiveWarehouses();

    /**
 * Retrieves all primary warehouse records.
     *
 * @return a list of primary warehouse records as WarehouseDTO objects
     */
    List<WarehouseDTO> getPrimaryWarehouses();

    /**
 * Creates a new warehouse record in the database.
     *
 * @param warehouseDTO the warehouse data to create, as a WarehouseDTO object
 * @return the created warehouse record with generated id, as a WarehouseDTO object
     */
    WarehouseDTO createWarehouse(WarehouseDTO warehouseDTO);

    /**
 * Updates an existing warehouse record in the database.
     *
 * @param id the unique identifier of the warehouse record to update
 * @param warehouseDTO the updated warehouse data, as a WarehouseDTO object
 * @return the updated warehouse record, as a WarehouseDTO object
 * @throws WarehouseNotFoundException if no warehouse record with the specified id exists
     */
    WarehouseDTO updateWarehouse(Long id, WarehouseDTO warehouseDTO);

    /**
 * Deletes a warehouse record from the database by its unique identifier.
     *
 * @param id the unique identifier of the warehouse record to delete
 * @throws WarehouseNotFoundException if no warehouse record with the specified id exists
     */
    void deleteWarehouse(Long id);

    /**
 * Activates a warehouse record.
     *
 * @param id the unique identifier of the warehouse record to activate
 * @throws WarehouseNotFoundException if no warehouse record with the specified id exists
     */
    void activateWarehouse(Long id);

    /**
 * Deactivates a warehouse record.
     *
 * @param id the unique identifier of the warehouse record to deactivate
 * @throws WarehouseNotFoundException if no warehouse record with the specified id exists
     */
    void deactivateWarehouse(Long id);
}
