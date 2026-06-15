package com.inventory.mallservice.controller;

import com.inventory.mallservice.entity.ShoppingCart;
import com.inventory.mallservice.service.IShoppingCartService;
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
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class ShoppingCartControllerTest {

    @Mock
    private IShoppingCartService cartService;

    @InjectMocks
    private ShoppingCartController shoppingCartController;

    @Test
    void testAddToCart() {
        ShoppingCart cart = new ShoppingCart();
        cart.setId(1L);
        when(cartService.addToCart(anyLong(), anyLong(), anyInt())).thenReturn(cart);

        Map<String, Object> req = Map.of("userId", 1L, "skuId", 100L, "quantity", 2);
        ResponseEntity<ShoppingCart> response = shoppingCartController.addToCart(req);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(1L, response.getBody().getId());
    }

    @Test
    void testUpdateQuantity() {
        ShoppingCart cart = new ShoppingCart();
        cart.setId(1L);
        cart.setQuantity(5);
        when(cartService.updateCartItemQuantity(anyLong(), anyInt())).thenReturn(cart);

        Map<String, Integer> req = Map.of("quantity", 5);
        ResponseEntity<ShoppingCart> response = shoppingCartController.updateQuantity(1L, req);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(5, response.getBody().getQuantity());
    }

    @Test
    void testRemoveFromCart() {
        ResponseEntity<Void> response = shoppingCartController.removeFromCart(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    void testClearCart() {
        ResponseEntity<Void> response = shoppingCartController.clearCart(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    void testGetCart() {
        ShoppingCart cart = new ShoppingCart();
        cart.setId(1L);
        when(cartService.getCartByUserId(anyLong())).thenReturn(List.of(cart));

        ResponseEntity<List<ShoppingCart>> response = shoppingCartController.getCart(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testCheckCartItem() {
        Map<String, Boolean> req = Map.of("checked", true);
        ResponseEntity<Void> response = shoppingCartController.checkCartItem(1L, req);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testCheckAllCartItems() {
        Map<String, Boolean> req = Map.of("checked", false);
        ResponseEntity<Void> response = shoppingCartController.checkAllCartItems(1L, req);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testGetCartItemCount() {
        when(cartService.getCartItemCount(anyLong())).thenReturn(3);

        ResponseEntity<Integer> response = shoppingCartController.getCartItemCount(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(3, response.getBody());
    }
}
