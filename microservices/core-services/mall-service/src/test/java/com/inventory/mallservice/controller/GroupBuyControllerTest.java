package com.inventory.mallservice.controller;

import com.inventory.mallservice.entity.GroupBuyActivity;
import com.inventory.mallservice.service.IGroupBuyService;
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

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class GroupBuyControllerTest {

    @Mock
    private IGroupBuyService groupBuyService;

    @InjectMocks
    private GroupBuyController groupBuyController;

    @Test
    void testCreateActivity() {
        GroupBuyActivity activity = new GroupBuyActivity();
        activity.setId(1L);
        when(groupBuyService.createActivity(any(GroupBuyActivity.class))).thenReturn(activity);

        ResponseEntity<GroupBuyActivity> response = groupBuyController.createActivity(activity);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(1L, response.getBody().getId());
    }

    @Test
    void testJoinGroup() {
        when(groupBuyService.joinGroup(anyLong(), anyLong())).thenReturn(true);

        Map<String, Object> req = Map.of("activityId", 1, "userId", 100);
        ResponseEntity<Map<String, Object>> response = groupBuyController.joinGroup(req);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testListActivities() {
        GroupBuyActivity activity = new GroupBuyActivity();
        activity.setId(1L);
        Page<GroupBuyActivity> page = new PageImpl<>(List.of(activity), PageRequest.of(0, 20), 1);
        when(groupBuyService.listActivities(any())).thenReturn(page);

        ResponseEntity<Page<GroupBuyActivity>> response = groupBuyController.listActivities(0, 20);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getTotalElements());
    }
}
