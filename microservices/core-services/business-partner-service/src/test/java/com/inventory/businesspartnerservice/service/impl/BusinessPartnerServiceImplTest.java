package com.inventory.businesspartnerservice.service.impl;

import com.inventory.businesspartnerservice.dto.BusinessPartnerDTO;
import com.inventory.businesspartnerservice.dto.BusinessPartnerStatisticsDTO;
import com.inventory.businesspartnerservice.entity.BusinessPartner;
import com.inventory.businesspartnerservice.entity.BusinessPartner.PartnerType;
import com.inventory.businesspartnerservice.exception.BusinessPartnerNotFoundException;
import com.inventory.businesspartnerservice.repository.BusinessPartnerRepository;
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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class BusinessPartnerServiceImplTest {

    @Mock
    private BusinessPartnerRepository repository;

    @InjectMocks
    private BusinessPartnerServiceImpl service;

    private BusinessPartner partner;

    @BeforeEach
    void setUp() {
        partner = new BusinessPartner();
        partner.setId(1L);
        partner.setPartnerCode("BP-001");
        partner.setPartnerName("Acme Corp");
        partner.setPartnerType(PartnerType.CUSTOMER);
        partner.setActive(true);
    }

    @Test
    void testCreateBusinessPartner() {
        when(repository.save(any(BusinessPartner.class))).thenReturn(partner);

        BusinessPartnerDTO input = new BusinessPartnerDTO();
        input.setPartnerName("Acme Corp");
        input.setPartnerCode("BP-001");
        input.setPartnerType(PartnerType.CUSTOMER);

        BusinessPartnerDTO result = service.createBusinessPartner(input);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(repository).save(any(BusinessPartner.class));
    }

    @Test
    void testGetBusinessPartnerById_NotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(BusinessPartnerNotFoundException.class,
                () -> service.getBusinessPartnerById(99L));
    }

    @Test
    void testGetBusinessPartnerById_Found() {
        when(repository.findById(1L)).thenReturn(Optional.of(partner));

        BusinessPartnerDTO result = service.getBusinessPartnerById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("BP-001", result.getPartnerCode());
    }

    @Test
    void testDeleteBusinessPartner() {
        when(repository.existsById(1L)).thenReturn(true);

        service.deleteBusinessPartner(1L);

        verify(repository).deleteById(1L);
    }

    @Test
    void testDeleteBusinessPartner_NotFound() {
        when(repository.existsById(99L)).thenReturn(false);

        assertThrows(BusinessPartnerNotFoundException.class,
                () -> service.deleteBusinessPartner(99L));
        verify(repository, never()).deleteById(anyLong());
    }

    @Test
    void testGetStatistics() {
        when(repository.count()).thenReturn(100L);
        when(repository.countByActiveTrue()).thenReturn(80L);
        when(repository.countByPartnerType(PartnerType.CUSTOMER)).thenReturn(60L);
        when(repository.countByPartnerType(PartnerType.SUPPLIER)).thenReturn(40L);

        BusinessPartnerStatisticsDTO stats = service.getStatistics();

        assertNotNull(stats);
        assertEquals(100L, stats.getTotalPartners());
        assertEquals(80L, stats.getActivePartners());
    }
}
