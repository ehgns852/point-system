package com.backend.pointsystem.service;

import com.backend.pointsystem.common.UserUtil;
import com.backend.pointsystem.dto.request.AddItemToCartRequest;
import com.backend.pointsystem.dto.request.BuyAllRequest;
import com.backend.pointsystem.dto.request.CartRequest;
import com.backend.pointsystem.entity.Cart;
import com.backend.pointsystem.entity.CartItem;
import com.backend.pointsystem.entity.Item;
import com.backend.pointsystem.entity.User;
import com.backend.pointsystem.exception.CartItemNotFountException;
import com.backend.pointsystem.exception.CartNotFoundException;
import com.backend.pointsystem.exception.ItemNotFoundException;
import com.backend.pointsystem.repository.CartItemRepository;
import com.backend.pointsystem.repository.CartRepository;
import com.backend.pointsystem.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartService {

    private final CartRepository cartRepository;
    private final UserUtil userUtil;
    private final ItemRepository itemRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderService orderService;

    @Transactional
    public Long addItemToCart(AddItemToCartRequest request) {
        User user = userUtil.findCurrentUser();

        Cart cart = cartRepository.findByUser(user)
                .orElseGet(() -> cartRepository.save(new Cart(user)));

        addCartList(cart, request.getCartRequests());

        return cart.getId();
    }

    private void addCartList(Cart cart, List<CartRequest> cartRequests) {
        for (CartRequest cartItem : cartRequests) {
            Item item = getItem(cartItem.getItemId());

            int totalPrice = item.getPrice() * cartItem.getItemCount();

            Optional<CartItem> findCartItem = cartItemRepository.findByItem(item);

            //장바구니 상품 중복 시 총 가격, 수량만 증가
            if (findCartItem.isPresent()) {
                findCartItem.get().updateCartItem(cartItem.getItemCount(), totalPrice);
            } else {
                cartItemRepository.save(CartItem.addItemToCart(cart, item, cartItem.getItemCount(), totalPrice));
            }
        }
    }

    private Item getItem(Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException("해당 상품을 찾을 수 없습니다."));
        return item;
    }

    @Transactional
    public void deleteItemToCart(Long itemId) {
        User user = userUtil.findCurrentUser();

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new CartNotFoundException("회원의 장바구니가 존재하지 않습니다."));
        Item item = getItem(itemId);

        cartItemRepository.delete(cartItemRepository.findByCartAndItem(cart, item)
                .orElseThrow(() -> new CartItemNotFountException("회원의 장바구니 상품을 찾을 수 없습니다.")));
    }

    @Transactional
    public Long myCartBuyAll(BuyAllRequest request) {
        User user = userUtil.findCurrentUser();

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new CartNotFoundException("회원의 장바구니가 존재하지 않습니다."));

        List<CartItem> cartItems = cartItemRepository.findByCart(cart);

        return orderService.createOrder(cartItems, request.getPaymentMethod(), user);
    }
}
