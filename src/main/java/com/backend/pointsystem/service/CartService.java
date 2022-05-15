package com.backend.pointsystem.service;

import com.backend.pointsystem.common.UserUtil;
import com.backend.pointsystem.dto.request.AddItemToCartRequest;
import com.backend.pointsystem.dto.request.CartRequest;
import com.backend.pointsystem.entity.Cart;
import com.backend.pointsystem.entity.CartItem;
import com.backend.pointsystem.entity.Item;
import com.backend.pointsystem.entity.User;
import com.backend.pointsystem.exception.ItemNotFoundException;
import com.backend.pointsystem.repository.CartItemRepository;
import com.backend.pointsystem.repository.CartRepository;
import com.backend.pointsystem.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartService {

    private final CartRepository cartRepository;
    private final UserUtil userUtil;
    private final ItemRepository itemRepository;
    private final CartItemRepository cartItemRepository;


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
            Item item = itemRepository.findById(cartItem.getItemId())
                    .orElseThrow(() -> new ItemNotFoundException("해당 상품을 찾을 수 없습니다."));

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
}
