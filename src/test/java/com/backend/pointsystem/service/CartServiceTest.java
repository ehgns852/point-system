package com.backend.pointsystem.service;

import com.backend.pointsystem.common.UserUtil;
import com.backend.pointsystem.dto.request.AddItemToCartRequest;
import com.backend.pointsystem.dto.request.BuyAllRequest;
import com.backend.pointsystem.dto.request.CartRequest;
import com.backend.pointsystem.dummy.CartDummy;
import com.backend.pointsystem.dummy.ItemDummy;
import com.backend.pointsystem.dummy.UserDummy;
import com.backend.pointsystem.entity.*;
import com.backend.pointsystem.exception.CartItemNotFountException;
import com.backend.pointsystem.exception.ItemNotFoundException;
import com.backend.pointsystem.repository.CartItemRepository;
import com.backend.pointsystem.repository.CartRepository;
import com.backend.pointsystem.repository.ItemRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
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

    @Test
    @DisplayName("장바구니 항목 추가 - 해당 상품이 존재하지 않은 경우 실패")
    void addItemToCartFail() {
        //given
        User user = UserDummy.dummyUser();
        Cart cart = CartDummy.dummyCart(user);

        Item item1 = ItemDummy.itemDummy();

        List<CartRequest> request = List.of(new CartRequest(1L, 3),
                new CartRequest(2L, 4));

        given(userUtil.findCurrentUser()).willReturn(user);
        given(cartRepository.findByUser(any(User.class))).willReturn(Optional.of(cart));
        given(itemRepository.findById(anyLong())).willReturn(Optional.of(item1),Optional.empty());

        //when
        assertThatThrownBy(() -> cartService.addItemToCart(new AddItemToCartRequest(request)))
                .isInstanceOf(ItemNotFoundException.class);

        //then
        verify(userUtil, times(1)).findCurrentUser();
        verify(itemRepository, times(2)).findById(anyLong());
        verify(cartRepository, times(1)).findByUser(any(User.class));
    }

    @Test
    @DisplayName("회원 장바구니 상품 삭제 - 성공")
    void deleteItemToCart() {
        //given
        User user = UserDummy.dummyUser();
        Cart cart = CartDummy.dummyCart(user);
        Item item = ItemDummy.itemDummy();

        CartItem cartItem = new CartItem(3, 10000, cart, item);

        given(userUtil.findCurrentUser()).willReturn(user);
        given(cartRepository.findByUser(any(User.class))).willReturn(Optional.of(cart));
        given(itemRepository.findById(anyLong())).willReturn(Optional.of(item));
        given(cartItemRepository.findByCartAndItem(any(Cart.class), any(Item.class))).willReturn(Optional.of(cartItem));

        //when
        cartService.deleteItemToCart(1L);

        //then
        verify(userUtil, times(1)).findCurrentUser();
        verify(cartRepository, times(1)).findByUser(any(User.class));
        verify(itemRepository, times(1)).findById(anyLong());
        verify(cartItemRepository, times(1)).findByCartAndItem(any(Cart.class), any(Item.class));
    }

    @Test
    @DisplayName("회원 장바구니 상품 삭제 - 회원의 장바구니 상품이 존재하지 않은 경우 실패")
    void deleteItemToCartFail() {
        //given
        User user = UserDummy.dummyUser();
        Cart cart = CartDummy.dummyCart(user);
        Item item = ItemDummy.itemDummy();

        given(userUtil.findCurrentUser()).willReturn(user);
        given(cartRepository.findByUser(any(User.class))).willReturn(Optional.of(cart));
        given(itemRepository.findById(anyLong())).willReturn(Optional.of(item));

        //when
        assertThatThrownBy(() -> cartService.deleteItemToCart(1L))
                .isInstanceOf(CartItemNotFountException.class);

        //then
        verify(userUtil, times(1)).findCurrentUser();
        verify(cartRepository, times(1)).findByUser(any(User.class));
        verify(itemRepository, times(1)).findById(anyLong());
    }

    @Test
    @DisplayName("장바구니 목록 전체 구매 - 성공")
    void myCartBuyAll() {
        //given
        User user = UserDummy.dummyUser();
        Cart cart = CartDummy.dummyCart(user);

        Item item1 = ItemDummy.itemDummy();
        Item item2 = ItemDummy.itemDummy2();

        List<CartItem> cartItems = List.of(new CartItem(3, 10000, cart, item1),
                new CartItem(3, 10000, cart, item2));

        given(userUtil.findCurrentUser()).willReturn(user);
        given(cartRepository.findByUser(any(User.class))).willReturn(Optional.of(cart));
        given(cartItemRepository.findByCart(any(Cart.class))).willReturn(cartItems);
        given(orderService.createOrderByMyCart(any(List.class), any(PaymentMethod.class), any(User.class))).willReturn(1L);

        //when
        cartService.myCartBuyAll(new BuyAllRequest(PaymentMethod.MONEY));

        //then
        verify(userUtil, times(1)).findCurrentUser();
        verify(cartRepository, times(1)).findByUser(any(User.class));
        verify(cartItemRepository, times(1)).findByCart(any(Cart.class));
        verify(orderService, times(1)).createOrderByMyCart(any(List.class), any(PaymentMethod.class), any(User.class));
    }

}