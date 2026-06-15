package com.inventory.mallservice.controller;

import com.inventory.mallservice.entity.FullDiscountActivity;
import com.inventory.mallservice.service.IFullDiscountService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class FullDiscountControllerTest {

    @Mock
    private IFullDiscountService fullDiscountService;

    @InjectMocks
    private FullDiscountController fullDiscountController;

    @Test
    void testCreateActivity() {
        FullDiscountActivity activity = new FullDiscountActivity();
        activity.setId(1L);
        when(fullDiscountService.createActivity(any(FullDiscountActivity.class))).thenReturn(activity);

        ResponseEntity<FullDiscountActivity> response = fullDiscountController.createActivity(activity);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(1L, response.getBody().getId());
    }

    @Test
    void testGetActiveActivities() {
        FullDiscountActivity activity = new FullDiscountActivity();
        activity.setId(1L);
        when(fullDiscountService.getActiveActivities()).thenReturn(List.of(activity));

        ResponseEntity<List<FullDiscountActivity>> response = fullDiscountController.getActiveActivities();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }
}
