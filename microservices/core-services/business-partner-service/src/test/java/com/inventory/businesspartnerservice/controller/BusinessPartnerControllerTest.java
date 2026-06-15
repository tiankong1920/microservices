package com.inventory.businesspartnerservice.controller;

import com.inventory.businesspartnerservice.dto.BusinessPartnerDTO;
import com.inventory.businesspartnerservice.dto.BusinessPartnerStatisticsDTO;
import com.inventory.businesspartnerservice.entity.BusinessPartner.PartnerType;
import com.inventory.businesspartnerservice.service.IBusinessPartnerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class BusinessPartnerControllerTest {

    @Mock
    private IBusinessPartnerService businessPartnerService;

    @InjectMocks
    private BusinessPartnerController businessPartnerController;

    private BusinessPartnerDTO testPartner;

    @BeforeEach
    void setUp() {
        testPartner = new BusinessPartnerDTO();
        testPartner.setId(1L);
        testPartner.setPartnerCode("BP-001");
        testPartner.setPartnerName("Acme Corp");
        testPartner.setPartnerType(PartnerType.CUSTOMER);
        testPartner.setActive(true);
    }

    @Test
    void testCreateBusinessPartner() {
        BusinessPartnerDTO input = new BusinessPartnerDTO();
        input.setPartnerName("New Partner");

        BusinessPartnerDTO created = new BusinessPartnerDTO();
        created.setId(2L);
        created.setPartnerName("New Partner");
        created.setPartnerType(PartnerType.SUPPLIER);

        when(businessPartnerService.createBusinessPartner(any(BusinessPartnerDTO.class))).thenReturn(created);

        ResponseEntity<BusinessPartnerDTO> response = businessPartnerController.createBusinessPartner(input);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2L, response.getBody().getId());
    }

    @Test
    void testGetBusinessPartnerById() {
        when(businessPartnerService.getBusinessPartnerById(1L)).thenReturn(testPartner);

        ResponseEntity<BusinessPartnerDTO> response = businessPartnerController.getBusinessPartnerById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("BP-001", response.getBody().getPartnerCode());
    }

    @Test
    void testGetBusinessPartnerByCode() {
        when(businessPartnerService.getBusinessPartnerByCode("BP-001")).thenReturn(testPartner);

        ResponseEntity<BusinessPartnerDTO> response = businessPartnerController.getBusinessPartnerByCode("BP-001");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Acme Corp", response.getBody().getPartnerName());
    }

    @Test
    void testGetAllBusinessPartners() {
        when(businessPartnerService.getAllBusinessPartners()).thenReturn(List.of(testPartner));

        ResponseEntity<List<BusinessPartnerDTO>> response = businessPartnerController.getAllBusinessPartners();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testGetBusinessPartnersPaged() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<BusinessPartnerDTO> page = new PageImpl<>(List.of(testPartner), pageable, 1);
        when(businessPartnerService.getAllBusinessPartnersPaged(any(Pageable.class))).thenReturn(page);

        ResponseEntity<Page<BusinessPartnerDTO>> response = businessPartnerController.getBusinessPartnersPaged(0, 10, "id", "asc");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getTotalElements());
    }

    @Test
    void testGetBusinessPartnersPagedDesc() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<BusinessPartnerDTO> page = new PageImpl<>(List.of(testPartner), pageable, 1);
        when(businessPartnerService.getAllBusinessPartnersPaged(any(Pageable.class))).thenReturn(page);

        ResponseEntity<Page<BusinessPartnerDTO>> response = businessPartnerController.getBusinessPartnersPaged(0, 10, "id", "desc");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testUpdateBusinessPartner() {
        BusinessPartnerDTO input = new BusinessPartnerDTO();
        input.setPartnerName("Updated Partner");

        BusinessPartnerDTO updated = new BusinessPartnerDTO();
        updated.setId(1L);
        updated.setPartnerName("Updated Partner");

        when(businessPartnerService.updateBusinessPartner(eq(1L), any(BusinessPartnerDTO.class))).thenReturn(updated);

        ResponseEntity<BusinessPartnerDTO> response = businessPartnerController.updateBusinessPartner(1L, input);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Updated Partner", response.getBody().getPartnerName());
    }

    @Test
    void testDeleteBusinessPartner() {
        doNothing().when(businessPartnerService).deleteBusinessPartner(1L);

        ResponseEntity<Void> response = businessPartnerController.deleteBusinessPartner(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(businessPartnerService).deleteBusinessPartner(1L);
    }

    @Test
    void testGetBusinessPartnersByType() {
        when(businessPartnerService.getBusinessPartnersByType(PartnerType.CUSTOMER))
                .thenReturn(List.of(testPartner));

        ResponseEntity<List<BusinessPartnerDTO>> response =
                businessPartnerController.getBusinessPartnersByType(PartnerType.CUSTOMER);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testGetBusinessPartnersByTypePaged() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<BusinessPartnerDTO> page = new PageImpl<>(List.of(testPartner), pageable, 1);
        when(businessPartnerService.getBusinessPartnersByTypePaged(eq(PartnerType.CUSTOMER), any(Pageable.class)))
                .thenReturn(page);

        ResponseEntity<Page<BusinessPartnerDTO>> response =
                businessPartnerController.getBusinessPartnersByTypePaged(PartnerType.CUSTOMER, 0, 10);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getTotalElements());
    }

    @Test
    void testGetActiveBusinessPartners() {
        when(businessPartnerService.getActiveBusinessPartners()).thenReturn(List.of(testPartner));

        ResponseEntity<List<BusinessPartnerDTO>> response = businessPartnerController.getActiveBusinessPartners();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testGetActiveBusinessPartnersPaged() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<BusinessPartnerDTO> page = new PageImpl<>(List.of(testPartner), pageable, 1);
        when(businessPartnerService.getActiveBusinessPartnersPaged(any(Pageable.class))).thenReturn(page);

        ResponseEntity<Page<BusinessPartnerDTO>> response = businessPartnerController.getActiveBusinessPartnersPaged(0, 10);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testSearchBusinessPartners() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<BusinessPartnerDTO> page = new PageImpl<>(List.of(testPartner), pageable, 1);
        when(businessPartnerService.searchBusinessPartners(eq("Acme"), any(Pageable.class))).thenReturn(page);

        ResponseEntity<Page<BusinessPartnerDTO>> response = businessPartnerController.searchBusinessPartners("Acme", 0, 10);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getTotalElements());
    }

    @Test
    void testGetBusinessPartnersByCity() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<BusinessPartnerDTO> page = new PageImpl<>(List.of(testPartner), pageable, 1);
        when(businessPartnerService.getBusinessPartnersByCity(eq("Beijing"), any(Pageable.class))).thenReturn(page);

        ResponseEntity<Page<BusinessPartnerDTO>> response = businessPartnerController.getBusinessPartnersByCity("Beijing", 0, 10);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testGetBusinessPartnersByCountry() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<BusinessPartnerDTO> page = new PageImpl<>(List.of(testPartner), pageable, 1);
        when(businessPartnerService.getBusinessPartnersByCountry(eq("CN"), any(Pageable.class))).thenReturn(page);

        ResponseEntity<Page<BusinessPartnerDTO>> response = businessPartnerController.getBusinessPartnersByCountry("CN", 0, 10);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testToggleBusinessPartnerStatus() {
        BusinessPartnerDTO toggled = new BusinessPartnerDTO();
        toggled.setId(1L);
        toggled.setActive(false);
        when(businessPartnerService.toggleBusinessPartnerStatus(1L)).thenReturn(toggled);

        ResponseEntity<BusinessPartnerDTO> response = businessPartnerController.toggleBusinessPartnerStatus(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(false, response.getBody().isActive());
    }

    @Test
    void testActivateBusinessPartner() {
        when(businessPartnerService.activateBusinessPartner(1L)).thenReturn(testPartner);

        ResponseEntity<BusinessPartnerDTO> response = businessPartnerController.activateBusinessPartner(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(true, response.getBody().isActive());
    }

    @Test
    void testDeactivateBusinessPartner() {
        BusinessPartnerDTO deactivated = new BusinessPartnerDTO();
        deactivated.setId(1L);
        deactivated.setActive(false);
        when(businessPartnerService.deactivateBusinessPartner(1L)).thenReturn(deactivated);

        ResponseEntity<BusinessPartnerDTO> response = businessPartnerController.deactivateBusinessPartner(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(false, response.getBody().isActive());
    }

    @Test
    void testGetStatistics() {
        BusinessPartnerStatisticsDTO stats = new BusinessPartnerStatisticsDTO();
        stats.setTotalPartners(100L);
        stats.setActivePartners(80L);
        when(businessPartnerService.getStatistics()).thenReturn(stats);

        ResponseEntity<BusinessPartnerStatisticsDTO> response = businessPartnerController.getStatistics();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(100L, response.getBody().getTotalPartners());
    }

    @Test
    void testGetAllCities() {
        when(businessPartnerService.getAllCities()).thenReturn(List.of("Beijing", "Shanghai"));

        ResponseEntity<List<String>> response = businessPartnerController.getAllCities();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
    }

    @Test
    void testGetAllCountries() {
        when(businessPartnerService.getAllCountries()).thenReturn(List.of("CN", "US"));

        ResponseEntity<List<String>> response = businessPartnerController.getAllCountries();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
    }

    @Test
    void testBatchActivateBusinessPartners() {
        when(businessPartnerService.batchActivateBusinessPartners(List.of(1L, 2L, 3L))).thenReturn(3);

        ResponseEntity<Integer> response = businessPartnerController.batchActivateBusinessPartners(List.of(1L, 2L, 3L));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(3, response.getBody());
    }

    @Test
    void testBatchDeactivateBusinessPartners() {
        when(businessPartnerService.batchDeactivateBusinessPartners(List.of(4L, 5L))).thenReturn(2);

        ResponseEntity<Integer> response = businessPartnerController.batchDeactivateBusinessPartners(List.of(4L, 5L));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody());
    }
}
