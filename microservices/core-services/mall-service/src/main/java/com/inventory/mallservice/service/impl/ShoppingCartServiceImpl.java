package com.inventory.mallservice.service.impl;

import com.inventory.common.core.exception.EntityNotFoundException;
import com.inventory.mallservice.entity.ShoppingCart;
import com.inventory.mallservice.repository.IShoppingCartRepository;
import com.inventory.mallservice.service.IShoppingCartService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 购物车服务实现类.
 *
 * @author Inventory Team
 * @version 1.0
 * @since 3.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("null")
public class ShoppingCartServiceImpl implements IShoppingCartService {

    private final IShoppingCartRepository cartRepository;

    @Override
    @Transactional
    public ShoppingCart addToCart(final Long userId, final Long skuId, final Integer quantity) {
        log.info("Adding sku {} to cart for user {}, quantity: {}", skuId, userId, quantity);
        List<ShoppingCart> existing = cartRepository.findByUserIdOrderByCreatedAtDesc(userId);
        ShoppingCart cartItem = existing.stream()
                .filter(c -> c.getSkuId().equals(skuId))
                .findFirst()
                .orElse(null);

        if (cartItem != null) {
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
            return cartRepository.save(cartItem);
        }

        ShoppingCart newCart = new ShoppingCart();
        newCart.setUserId(userId);
        newCart.setSkuId(skuId);
        newCart.setQuantity(quantity);
        newCart.setChecked(true);
        return cartRepository.save(newCart);
    }

    @Override
    @Transactional
    public ShoppingCart updateCartItemQuantity(final Long cartItemId, final Integer quantity) {
        log.info("Updating cart item {} quantity to {}", cartItemId, quantity);
        ShoppingCart cart = cartRepository.findById(cartItemId)
                .orElseThrow(() -> EntityNotFoundException.forEntity("ShoppingCart", cartItemId));
        cart.setQuantity(quantity);
        return cartRepository.save(cart);
    }

    @Override
    @Transactional
    public void removeFromCart(final Long cartItemId) {
        log.info("Removing cart item: {}", cartItemId);
        cartRepository.deleteById(cartItemId);
    }

    @Override
    @Transactional
    public void clearCart(final Long userId) {
        log.info("Clearing cart for user: {}", userId);
        cartRepository.deleteByUserId(userId);
    }

    @Override
    public List<ShoppingCart> getCartByUserId(final Long userId) {
        return cartRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    public List<ShoppingCart> getCheckedCartItems(final Long userId) {
        return cartRepository.findByUserIdAndCheckedTrue(userId);
    }

    @Override
    @Transactional
    public void checkCartItem(final Long cartItemId, final Boolean checked) {
        ShoppingCart cart = cartRepository.findById(cartItemId)
                .orElseThrow(() -> EntityNotFoundException.forEntity("ShoppingCart", cartItemId));
        cart.setChecked(checked);
        cartRepository.save(cart);
    }

    @Override
    @Transactional
    public void checkAllCartItems(final Long userId, final Boolean checked) {
        List<ShoppingCart> items = cartRepository.findByUserIdOrderByCreatedAtDesc(userId);
        items.forEach(item -> item.setChecked(checked));
        cartRepository.saveAll(items);
    }

    @Override
    public Integer getCartItemCount(final Long userId) {
        return cartRepository.countByUserId(userId);
    }
}
