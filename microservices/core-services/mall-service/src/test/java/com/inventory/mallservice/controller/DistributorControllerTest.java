package com.inventory.mallservice.controller;

import com.inventory.mallservice.entity.Distributor;
import com.inventory.mallservice.service.IDistributorService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class DistributorControllerTest {

    @Mock
    private IDistributorService distributorService;

    @InjectMocks
    private DistributorController distributorController;

    @Test
    void testRegister() {
        Distributor distributor = new Distributor();
        distributor.setId(1L);
        when(distributorService.registerDistributor(anyLong(), any())).thenReturn(distributor);

        Map<String, Object> req = Map.of("userId", 100, "parentId", 50);
        ResponseEntity<Distributor> response = distributorController.register(req);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testUpdateCommissionRate() {
        Distributor distributor = new Distributor();
        distributor.setId(1L);
        distributor.setCommissionRate(new BigDecimal("0.15"));
        when(distributorService.updateCommissionRate(anyLong(), any(BigDecimal.class))).thenReturn(distributor);

        Map<String, Object> req = Map.of("distributorId", 1, "rate", "0.15");
        ResponseEntity<Distributor> response = distributorController.updateCommissionRate(req);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }
}
