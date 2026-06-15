package com.inventory.customerservice.service;

import com.inventory.customerservice.dto.CustomerDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ICustomerService {

    List<CustomerDTO> getAllCustomers();

    Page<CustomerDTO> getAllCustomers(Pageable pageable);

    CustomerDTO getCustomerById(Long id);

    CustomerDTO getCustomerByEmail(String email);

    List<CustomerDTO> getActiveCustomers();

    Page<CustomerDTO> getActiveCustomers(Pageable pageable);

    Page<CustomerDTO> searchCustomers(String keyword, Pageable pageable);

    Page<CustomerDTO> searchCustomersByName(String name, Pageable pageable);

    CustomerDTO createCustomer(CustomerDTO customerDTO);

    CustomerDTO updateCustomer(Long id, CustomerDTO customerDTO);

    void deleteCustomer(Long id);

    CustomerDTO activateCustomer(Long id);

    CustomerDTO deactivateCustomer(Long id);

    boolean existsByEmail(String email);

    boolean existsByEmailAndNotId(String email, Long id);
}
