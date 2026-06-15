package com.inventory.mallservice.controller;

import com.inventory.mallservice.entity.Coupon;
import com.inventory.mallservice.entity.CouponTemplate;
import com.inventory.mallservice.service.ICouponService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class CouponControllerTest {

    @Mock
    private ICouponService couponService;

    @InjectMocks
    private CouponController couponController;

    @Test
    void testCreateTemplate() {
        CouponTemplate template = new CouponTemplate();
        template.setId(1L);
        template.setName("满100减10");
        when(couponService.createTemplate(any(CouponTemplate.class))).thenReturn(template);

        ResponseEntity<CouponTemplate> response = couponController.createTemplate(template);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
    }

    @Test
    void testBatchGenerate() {
        Coupon coupon = new Coupon();
        coupon.setId(1L);
        when(couponService.batchGenerateCoupons(anyLong(), anyInt()))
                .thenReturn(List.of(coupon, coupon, coupon));

        ResponseEntity<List<Coupon>> response = couponController.batchGenerate(1L, Map.of("count", 3));

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(3, response.getBody().size());
    }

    @Test
    void testIssueCoupon() {
        Coupon coupon = new Coupon();
        coupon.setId(1L);
        when(couponService.issueCoupon(anyLong(), anyLong())).thenReturn(coupon);

        ResponseEntity<Coupon> response = couponController.issueCoupon(Map.of("templateId", 1L, "userId", 100L));

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(1L, response.getBody().getId());
    }

    @Test
    void testUseCoupon() {
        Coupon coupon = new Coupon();
        coupon.setId(1L);
        coupon.setStatus(1);
        when(couponService.useCoupon(anyLong(), anyLong())).thenReturn(coupon);

        ResponseEntity<Coupon> response = couponController.useCoupon(Map.of("couponId", 1L, "orderId", 100L));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }
}
