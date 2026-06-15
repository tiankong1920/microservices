package com.inventory.mallservice.controller;

import com.inventory.mallservice.entity.FlashSaleActivity;
import com.inventory.mallservice.service.IFlashSaleService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class FlashSaleControllerTest {

    @Mock
    private IFlashSaleService flashSaleService;

    @InjectMocks
    private FlashSaleController flashSaleController;

    @Test
    void testCreateActivity() {
        FlashSaleActivity activity = new FlashSaleActivity();
        activity.setId(1L);
        when(flashSaleService.createActivity(any(FlashSaleActivity.class))).thenReturn(activity);

        ResponseEntity<FlashSaleActivity> response = flashSaleController.createActivity(activity);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(1L, response.getBody().getId());
    }

    @Test
    void testParticipateFlashSale() {
        when(flashSaleService.participate(anyLong(), anyLong())).thenReturn(true);

        Map<String, Object> req = Map.of("activityId", 1, "userId", 100);
        ResponseEntity<Map<String, Object>> response = flashSaleController.participate(req);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }
}
