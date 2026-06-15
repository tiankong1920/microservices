package com.inventory.mallservice.controller;

import com.inventory.mallservice.entity.BargainActivity;
import com.inventory.mallservice.entity.BargainDetail;
import com.inventory.mallservice.entity.BargainRecord;
import com.inventory.mallservice.service.IBargainService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class BargainControllerTest {

    @Mock
    private IBargainService bargainService;

    @InjectMocks
    private BargainController bargainController;

    @Test
    void testCreateBargainActivity() {
        BargainActivity activity = new BargainActivity();
        activity.setId(1L);
        activity.setName("双11砍价");
        when(bargainService.createBargainActivity(any(BargainActivity.class))).thenReturn(activity);

        ResponseEntity<BargainActivity> response = bargainController.createBargainActivity(activity);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(1L, response.getBody().getId());
    }

    @Test
    void testGetBargainActivities() {
        BargainActivity activity = new BargainActivity();
        activity.setId(1L);
        Page<BargainActivity> page = new PageImpl<>(List.of(activity), PageRequest.of(0, 20), 1);
        when(bargainService.getBargainActivities(any())).thenReturn(page);

        ResponseEntity<Page<BargainActivity>> response = bargainController.getBargainActivities(0, 20);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getTotalElements());
    }

    @Test
    void testGetActiveBargainActivities() {
        BargainActivity activity = new BargainActivity();
        activity.setId(1L);
        when(bargainService.getActiveBargainActivities()).thenReturn(List.of(activity));

        ResponseEntity<List<BargainActivity>> response = bargainController.getActiveBargainActivities();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testInitiateBargain() {
        BargainDetail detail = new BargainDetail();
        detail.setId(1L);
        when(bargainService.initiateBargain(anyLong(), anyLong())).thenReturn(detail);

        Map<String, Object> req = Map.of("activityId", 1, "userId", 100);
        ResponseEntity<BargainDetail> response = bargainController.initiateBargain(req);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
    }
}
