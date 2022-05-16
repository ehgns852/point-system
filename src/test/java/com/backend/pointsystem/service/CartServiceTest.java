package com.backend.pointsystem.service;

import com.backend.pointsystem.common.UserUtil;
import com.backend.pointsystem.dto.request.AddItemToCartRequest;
import com.backend.pointsystem.dto.request.CartRequest;
import com.backend.pointsystem.dummy.CartDummy;
import com.backend.pointsystem.dummy.ItemDummy;
import com.backend.pointsystem.dummy.UserDummy;
import com.backend.pointsystem.entity.Cart;
import com.backend.pointsystem.entity.CartItem;
import com.backend.pointsystem.entity.Item;
import com.backend.pointsystem.entity.User;
import com.backend.pointsystem.repository.CartItemRepository;
import com.backend.pointsystem.repository.CartRepository;
import com.backend.pointsystem.repository.ItemRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private UserUtil userUtil;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private OrderService orderService;

    @InjectMocks
    private CartService cartService;

    @Test
    @DisplayName("장바구니 항목 추가 - 성공")
    void addItemToCart() {
        //given
        User user = UserDummy.dummyUser();
        Cart cart = CartDummy.dummyCart(user);

        Item item1 = ItemDummy.itemDummy();
        Item item2 = ItemDummy.itemDummy();

        CartItem cartItem1 = new CartItem(3, 10000, cart, item1);
        CartItem cartItem2 = new CartItem(3, 10000, cart, item1);

        List<CartRequest> request = List.of(new CartRequest(1L, 3),
                new CartRequest(2L, 4));

        given(userUtil.findCurrentUser()).willReturn(user);
        given(cartRepository.findByUser(any(User.class))).willReturn(Optional.of(cart));
        given(itemRepository.findById(anyLong())).willReturn(Optional.of(item1), Optional.of(item2));
        given(cartItemRepository.findByCartAndItem(any(Cart.class), any(Item.class))).willReturn(Optional.empty());
        given(cartItemRepository.save(any(CartItem.class))).willReturn(cartItem1, cartItem2);

        //when
        cartService.addItemToCart(new AddItemToCartRequest(request));

        //then
        verify(userUtil, times(1)).findCurrentUser();
        verify(itemRepository, times(2)).findById(anyLong());
        verify(cartRepository, times(1)).findByUser(any(User.class));
        verify(cartItemRepository, times(2)).findByCartAndItem(any(Cart.class), any(Item.class));
        verify(cartItemRepository, times(2)).save(any(CartItem.class));
    }

}