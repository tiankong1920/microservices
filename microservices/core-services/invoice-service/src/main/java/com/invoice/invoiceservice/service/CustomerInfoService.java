package com.invoice.invoiceservice.service;

import java.util.List;

import com.invoice.invoiceservice.dto.CustomerInfoDTO;

public interface CustomerInfoService {

    CustomerInfoDTO createCustomer(CustomerInfoDTO dto);

    CustomerInfoDTO updateCustomer(Long id, CustomerInfoDTO dto);

    CustomerInfoDTO getCustomerById(Long id);

    List<CustomerInfoDTO> getAllCustomers();

    List<CustomerInfoDTO> getActiveCustomers();

    void deleteCustomer(Long id);

    void enableCustomer(Long id);

    void disableCustomer(Long id);

    List<CustomerInfoDTO> searchCustomers(String keyword);

    void incrementUsageCount(Long id);

    CustomerInfoDTO autoFillCustomerInfo(Long id);
}
