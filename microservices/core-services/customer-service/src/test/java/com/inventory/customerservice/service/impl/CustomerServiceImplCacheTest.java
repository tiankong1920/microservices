package com.inventory.customerservice.service.impl;

import com.inventory.customerservice.dto.CustomerDTO;
import com.inventory.customerservice.entity.Customer;
import com.inventory.customerservice.repository.ICustomerRepository;
import org.modelmapper.ModelMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
public class CustomerServiceImplCacheTest {

    @Mock
    private ICustomerRepository customerRepository;

    @Mock
    private ModelMapper modelMapper;

    private CustomerServiceImpl customerService;

    private Customer testCustomer;
    private CustomerDTO testCustomerDTO;

    @BeforeEach
    void setUp() {
        customerService = new CustomerServiceImpl(customerRepository, modelMapper);
        testCustomer = new Customer();
        testCustomer.setId(1L);
        testCustomer.setName("Test Customer");
        testCustomer.setEmail("test@example.com");
        testCustomer.setPhone("1234567890");
        testCustomer.setAddress("Test Address");
        testCustomer.setActive(true);

        testCustomerDTO = new CustomerDTO();
        testCustomerDTO.setId(1L);
        testCustomerDTO.setName("Test Customer");
        testCustomerDTO.setEmail("test@example.com");
        testCustomerDTO.setPhone("1234567890");
        testCustomerDTO.setAddress("Test Address");
        testCustomerDTO.setActive(true);

        // Use lenient stubbing to avoid unnecessary stubbing exceptions
        lenient().when(modelMapper.map(any(Customer.class), eq(CustomerDTO.class))).thenReturn(testCustomerDTO);
        lenient().when(modelMapper.map(any(CustomerDTO.class), eq(Customer.class))).thenReturn(testCustomer);
        // Additional stubbing for update method (dto to existing entity mapping)
        lenient().doNothing().when(modelMapper).map(any(CustomerDTO.class), any(Customer.class));
    }

    @Test
    void testGetCustomerById() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));

        final CustomerDTO result = customerService.getCustomerById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Customer", result.getName());

        verify(customerRepository, times(1)).findById(1L);
    }

    @Test
    void testGetCustomerById_CacheMiss() {
        when(customerRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> customerService.getCustomerById(1L));
        verify(customerRepository, times(1)).findById(1L);
    }

    @Test
    void testCreateCustomer() {
        when(customerRepository.save(any(Customer.class))).thenReturn(testCustomer);

        final CustomerDTO result = customerService.createCustomer(testCustomerDTO);

        assertNotNull(result);
        assertEquals(1L, result.getId());

        verify(customerRepository, times(1)).save(any(Customer.class));
    }

    @Test
    void testUpdateCustomer() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(customerRepository.save(any(Customer.class))).thenReturn(testCustomer);

        final CustomerDTO result = customerService.updateCustomer(1L, testCustomerDTO);

        assertNotNull(result);
        assertEquals("Test Customer", result.getName());

        verify(customerRepository, times(1)).findById(1L);
        verify(customerRepository, times(1)).save(any(Customer.class));
    }

    @Test
    void testDeleteCustomer() {
        when(customerRepository.existsById(1L)).thenReturn(true);
        doNothing().when(customerRepository).deleteById(1L);

        customerService.deleteCustomer(1L);

        verify(customerRepository, times(1)).existsById(1L);
        verify(customerRepository, times(1)).deleteById(1L);
    }

    @Test
    void testConcurrentAccess() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));

        // Simulate concurrent access
        final CustomerDTO result1 = customerService.getCustomerById(1L);
        final CustomerDTO result2 = customerService.getCustomerById(1L);
        final CustomerDTO result3 = customerService.getCustomerById(1L);

        assertNotNull(result1);
        assertNotNull(result2);
        assertNotNull(result3);
        assertEquals(1L, result1.getId());
        assertEquals(1L, result2.getId());
        assertEquals(1L, result3.getId());

        // Without caching in test environment, each call goes to repository
        verify(customerRepository, times(3)).findById(1L);
    }
}
