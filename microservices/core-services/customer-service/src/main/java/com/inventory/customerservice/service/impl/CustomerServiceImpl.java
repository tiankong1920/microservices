package com.inventory.customerservice.service.impl;

import com.inventory.customerservice.dto.CustomerDTO;
import com.inventory.customerservice.entity.Customer;
import com.inventory.customerservice.exception.CustomerNotFoundException;
import com.inventory.customerservice.repository.ICustomerRepository;
import com.inventory.customerservice.service.ICustomerService;
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
public class CustomerServiceImpl implements ICustomerService {

    private final ICustomerRepository customerRepository;
    private final ModelMapper modelMapper;

    @Override
    public List<CustomerDTO> getAllCustomers() {
        log.debug("Getting all customers");
        return customerRepository.findAll().stream()
                .map(customer -> modelMapper.map(customer, CustomerDTO.class))
                .toList();
    }

    @Override
    public Page<CustomerDTO> getAllCustomers(Pageable pageable) {
        log.debug("Getting all customers with pagination: {}", pageable);
        return customerRepository.findAll(pageable)
                .map(customer -> modelMapper.map(customer, CustomerDTO.class));
    }

    @Override
    @Cacheable(value = "customers", key = "#id")
    public CustomerDTO getCustomerById(final Long id) {
        log.debug("Getting customer by id: {}", id);
        final Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));
        return modelMapper.map(customer, CustomerDTO.class);
    }

    @Override
    @Cacheable(value = "customers", key = "#email")
    public CustomerDTO getCustomerByEmail(final String email) {
        log.debug("Getting customer by email: {}", email);
        final Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with email: " + email));
        return modelMapper.map(customer, CustomerDTO.class);
    }

    @Override
    public List<CustomerDTO> getActiveCustomers() {
        log.debug("Getting active customers");
        return customerRepository.findByIsActive(true, Pageable.unpaged()).stream()
                .map(customer -> modelMapper.map(customer, CustomerDTO.class))
                .toList();
    }

    @Override
    public Page<CustomerDTO> getActiveCustomers(Pageable pageable) {
        log.debug("Getting active customers with pagination: {}", pageable);
        return customerRepository.findByIsActive(true, pageable)
                .map(customer -> modelMapper.map(customer, CustomerDTO.class));
    }

    @Override
    public Page<CustomerDTO> searchCustomers(String keyword, Pageable pageable) {
        log.debug("Searching customers with keyword: {}", keyword);
        return customerRepository.searchByKeyword(keyword, pageable)
                .map(customer -> modelMapper.map(customer, CustomerDTO.class));
    }

    @Override
    public Page<CustomerDTO> searchCustomersByName(String name, Pageable pageable) {
        log.debug("Searching customers by name: {}", name);
        return customerRepository.findByNameContainingIgnoreCase(name, pageable)
                .map(customer -> modelMapper.map(customer, CustomerDTO.class));
    }

    @Override
    @CacheEvict(value = "customers", allEntries = true)
    @Transactional
    public CustomerDTO createCustomer(final CustomerDTO customerDTO) {
        log.debug("Creating customer: {}", customerDTO.getName());

        if (customerRepository.findByEmail(customerDTO.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Customer with email " + customerDTO.getEmail() + " already exists");
        }

        final Customer customer = modelMapper.map(customerDTO, Customer.class);
        customer.setActive(true);
        final Customer savedCustomer = customerRepository.save(customer);
        log.info("Customer created successfully with id: {}", savedCustomer.getId());
        return modelMapper.map(savedCustomer, CustomerDTO.class);
    }

    @Override
    @CacheEvict(value = "customers", allEntries = true)
    @Transactional
    public CustomerDTO updateCustomer(final Long id, final CustomerDTO customerDTO) {
        log.debug("Updating customer with id: {}", id);

        final Customer existingCustomer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));

        if (customerDTO.getEmail() != null &&
            !customerDTO.getEmail().equals(existingCustomer.getEmail()) &&
            customerRepository.findByEmail(customerDTO.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Customer with email " + customerDTO.getEmail() + " already exists");
        }

        modelMapper.map(customerDTO, existingCustomer);
        final Customer updatedCustomer = customerRepository.save(existingCustomer);
        log.info("Customer updated successfully with id: {}", updatedCustomer.getId());
        return modelMapper.map(updatedCustomer, CustomerDTO.class);
    }

    @Override
    @CacheEvict(value = "customers", allEntries = true)
    @Transactional
    public void deleteCustomer(final Long id) {
        log.debug("Deleting customer with id: {}", id);
        if (!customerRepository.existsById(id)) {
            throw new CustomerNotFoundException(id);
        }
        customerRepository.deleteById(id);
        log.info("Customer deleted successfully with id: {}", id);
    }

    @Override
    @CacheEvict(value = "customers", allEntries = true)
    @Transactional
    public CustomerDTO activateCustomer(final Long id) {
        log.debug("Activating customer with id: {}", id);
        final Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));
        customer.setActive(true);
        final Customer updatedCustomer = customerRepository.save(customer);
        log.info("Customer activated with id: {}", id);
        return modelMapper.map(updatedCustomer, CustomerDTO.class);
    }

    @Override
    @CacheEvict(value = "customers", allEntries = true)
    @Transactional
    public CustomerDTO deactivateCustomer(final Long id) {
        log.debug("Deactivating customer with id: {}", id);
        final Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));
        customer.setActive(false);
        final Customer updatedCustomer = customerRepository.save(customer);
        log.info("Customer deactivated with id: {}", id);
        return modelMapper.map(updatedCustomer, CustomerDTO.class);
    }

    @Override
    public boolean existsByEmail(final String email) {
        return customerRepository.findByEmail(email).isPresent();
    }

    @Override
    public boolean existsByEmailAndNotId(final String email, final Long id) {
        return customerRepository.findByEmail(email)
                .map(customer -> !customer.getId().equals(id))
                .orElse(false);
    }
}
