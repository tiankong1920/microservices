package com.inventory.inventoryservice.service;

import com.inventory.inventoryservice.dto.BatchDTO;
import com.inventory.inventoryservice.exception.BatchNotFoundException;

import java.time.LocalDate;
import java.util.List;

/**
 * Service interface for batch management operations.
 * Provides methods for managing product batches, including creation, retrieval, update, and deletion.
 */
public interface IBatchService {

    /**
 * Retrieves all batch records from the database.
     *
 * @return a list of all batch records as BatchDTO objects
     */
    List<BatchDTO> getAllBatches();

    /**
 * Retrieves a specific batch record by its unique identifier.
     *
 * @param id the unique identifier of the batch record to retrieve
 * @return the batch record with the specified id as a BatchDTO object
 * @throws BatchNotFoundException if no batch record with the specified id exists
     */
    BatchDTO getBatchById(Long id);

    /**
 * Retrieves a batch record by its unique batch code.
     *
 * @param batchCode the unique batch code of the batch record to retrieve
 * @return the batch record with the specified batch code as a BatchDTO object
 * @throws BatchNotFoundException if no batch record with the specified batch code exists
     */
    BatchDTO getBatchByCode(String batchCode);

    /**
 * Retrieves all batch records for a specific product.
     *
 * @param productId the unique identifier of the product
 * @return a list of batch records for the specified product as BatchDTO objects
     */
    List<BatchDTO> getBatchesByProductId(Long productId);

    /**
 * Retrieves all batch records for a specific warehouse.
     *
 * @param warehouseId the unique identifier of the warehouse
 * @return a list of batch records for the specified warehouse as BatchDTO objects
     */
    List<BatchDTO> getBatchesByWarehouseId(Long warehouseId);

    /**
 * Retrieves all batch records from a specific supplier.
     *
 * @param supplierId the unique identifier of the supplier
 * @return a list of batch records from the specified supplier as BatchDTO objects
     */
    List<BatchDTO> getBatchesBySupplierId(Long supplierId);

    /**
 * Retrieves all batch records with a specific status.
     *
 * @param status the status of the batch records to retrieve
 * @return a list of batch records with the specified status as BatchDTO objects
     */
    List<BatchDTO> getBatchesByStatus(String status);

    /**
 * Retrieves all batch records that expire before a specified date.
     *
 * @param date the date to check for expiration
 * @return a list of batch records expiring before the specified date as BatchDTO objects
     */
    List<BatchDTO> getBatchesExpiringBefore(LocalDate date);

    /**
 * Retrieves all batch records for a specific product in a specific warehouse.
     *
 * @param productId the unique identifier of the product
 * @param warehouseId the unique identifier of the warehouse
 * @return a list of batch records for the specified product and warehouse as BatchDTO objects
     */
    List<BatchDTO> getBatchesByProductAndWarehouse(Long productId, Long warehouseId);

    /**
 * Creates a new batch record in the database.
     *
 * @param batchDTO the batch data to create, as a BatchDTO object
 * @return the created batch record with generated id, as a BatchDTO object
     */
    BatchDTO createBatch(BatchDTO batchDTO);

    /**
 * Updates an existing batch record in the database.
     *
 * @param id the unique identifier of the batch record to update
 * @param batchDTO the updated batch data, as a BatchDTO object
 * @return the updated batch record, as a BatchDTO object
 * @throws BatchNotFoundException if no batch record with the specified id exists
     */
    BatchDTO updateBatch(Long id, BatchDTO batchDTO);

    /**
 * Deletes a batch record from the database by its unique identifier.
     *
 * @param id the unique identifier of the batch record to delete
 * @throws BatchNotFoundException if no batch record with the specified id exists
     */
    void deleteBatch(Long id);

    /**
 * Activates a batch record.
     *
 * @param id the unique identifier of the batch record to activate
 * @throws BatchNotFoundException if no batch record with the specified id exists
     */
    void activateBatch(Long id);

    /**
 * Deactivates a batch record.
     *
 * @param id the unique identifier of the batch record to deactivate
 * @throws BatchNotFoundException if no batch record with the specified id exists
     */
    void deactivateBatch(Long id);

    /**
 * Retrieves the available quantity for a specific batch.
     *
 * @param batchId the unique identifier of the batch
 * @return the available quantity for the specified batch
 * @throws BatchNotFoundException if no batch record with the specified id exists
     */
    Integer getAvailableQuantity(Long batchId);
}
