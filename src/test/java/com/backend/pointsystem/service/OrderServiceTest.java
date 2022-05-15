package com.backend.pointsystem.service;

import com.backend.pointsystem.common.UserUtil;
import com.backend.pointsystem.dto.request.CreateOrderRequest;
import com.backend.pointsystem.dto.request.OrderRequest;
import com.backend.pointsystem.dummy.CartDummy;
import com.backend.pointsystem.dummy.ItemDummy;
import com.backend.pointsystem.dummy.UserDummy;
import com.backend.pointsystem.entity.*;
import com.backend.pointsystem.exception.LockOfMoneyException;
import com.backend.pointsystem.exception.NotEnoughStockException;
import com.backend.pointsystem.repository.CartItemRepository;
import com.backend.pointsystem.repository.ItemRepository;
import com.backend.pointsystem.repository.OrderRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserUtil userUtil;

    @Mock
    private CartItemRepository cartItemRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    @DisplayName("주문 생성 - 성공")
    void createOrder() {
        //given
        User user = UserDummy.dummyUser();

        Item item1 = ItemDummy.itemDummy();
        Item item2 = ItemDummy.itemDummy2();

        List<OrderItem> orderItem = List.of(OrderItem.createOrderItem(item1, PaymentMethod.MONEY, 100000, 10),
                OrderItem.createOrderItem(item2, PaymentMethod.POINT, 200000, 5));

        Order order = Order.createOrder(user, orderItem);

        List<OrderRequest> request = List.of(new OrderRequest(1L, 3),
                new OrderRequest(2L, 4));

        given(userUtil.findCurrentUser()).willReturn(user);
        given(itemRepository.findById(anyLong())).willReturn(Optional.of(item1), Optional.of(item2));
        given(orderRepository.save(any(Order.class))).willReturn(order);

        //when
        Long orderId = orderService.createOrder(new CreateOrderRequest(request, PaymentMethod.MONEY));

        //then
        verify(userUtil, times(1)).findCurrentUser();
        verify(itemRepository, times(2)).findById(anyLong());
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    @DisplayName("주문 생성 - 상품 재고 수량이 부족한 경우 실패")
    void createOrderFail() {
        //given
        User user = UserDummy.dummyUser();

        Item item1 = ItemDummy.itemDummy();
        Item item2 = ItemDummy.itemDummy2();

        List<OrderRequest> request = List.of(new OrderRequest(1L, 99),
                new OrderRequest(2L, 50));

        given(userUtil.findCurrentUser()).willReturn(user);
        given(itemRepository.findById(anyLong())).willReturn(Optional.of(item1), Optional.of(item2));

        //when
        assertThatThrownBy(() -> orderService.createOrder(new CreateOrderRequest(request, PaymentMethod.MONEY)))
                .isInstanceOf(NotEnoughStockException.class);

        //then
        verify(userUtil, times(1)).findCurrentUser();
        verify(itemRepository, times(2)).findById(anyLong());
    }

    @Test
    @DisplayName("주문 생성 - 재산 OR 포인트가 부족한 경우 실패")
    void createOrderFail2() {
        //given
        User user = UserDummy.dummyUser();

        Item item1 = ItemDummy.itemDummy();
        Item item2 = ItemDummy.itemDummy2();

        List<OrderRequest> request = List.of(new OrderRequest(1L, 99),
                new OrderRequest(2L, 50));

        given(userUtil.findCurrentUser()).willReturn(user);
        given(itemRepository.findById(anyLong())).willReturn(Optional.of(item1), Optional.of(item2));

        //when
        assertThatThrownBy(() -> orderService.createOrder(new CreateOrderRequest(request, PaymentMethod.POINT)))
                .isInstanceOf(LockOfMoneyException.class);

        //then
        verify(userUtil, times(1)).findCurrentUser();
        verify(itemRepository, times(1)).findById(anyLong());
    }

    @Test
    @DisplayName("내 장바구니 목록 전체 구매 - 성공")
    void createOrderByMyCart() {
        //given
        User user = UserDummy.dummyUser();
        Cart cart = CartDummy.dummyCart(user);

        Item item1 = ItemDummy.itemDummy();
        Item item2 = ItemDummy.itemDummy2();

        List<OrderItem> orderItem = List.of(OrderItem.createOrderItem(item1, PaymentMethod.MONEY, 100000, 30),
                OrderItem.createOrderItem(item2, PaymentMethod.MONEY, 50000, 4));

        Order order = Order.createOrder(user, orderItem);

        List<CartItem> cartItems = List.of(CartItem.builder().totalPrice(100000).count(3).cart(cart).item(item1).build(),
                CartItem.builder().totalPrice(100000).count(3).cart(cart).item(item2).build());

        given(itemRepository.findById(anyLong())).willReturn(Optional.of(item1), Optional.of(item2));
        given(orderRepository.save(any(Order.class))).willReturn(order);

        //when
        Long orderId = orderService.createOrderByMyCart(cartItems, PaymentMethod.MONEY, user);

        //then
        verify(itemRepository, times(2)).findById(anyLong());
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    @DisplayName("내 장바구니 목록 전체 구매 - 상품 재고가 부족한 경우 실패")
    void createOrderByMyCartFail() {
        //given
        User user = UserDummy.dummyUser();
        Cart cart = CartDummy.dummyCart(user);

        Item item1 = ItemDummy.itemDummy();
        Item item2 = ItemDummy.itemDummy2();

        List<CartItem> cartItems = List.of(CartItem.builder().totalPrice(100000).count(3).cart(cart).item(item1).build(),
                CartItem.builder().totalPrice(100000).count(41).cart(cart).item(item2).build());

        given(itemRepository.findById(anyLong())).willReturn(Optional.of(item1), Optional.of(item2));

        //when
        assertThatThrownBy(() -> orderService.createOrderByMyCart(cartItems, PaymentMethod.MONEY, user))
                .isInstanceOf(NotEnoughStockException.class);

        //then
        verify(itemRepository, times(2)).findById(anyLong());
    }

    @Test
    @DisplayName("내 장바구니 목록 전체 구매 - 재산 Or 포인트가 부족한 경우 실패")
    void createOrderByMyCartFail2() {
        //given
        User user = UserDummy.dummyUser();
        Cart cart = CartDummy.dummyCart(user);

        Item item1 = ItemDummy.itemDummy();
        Item item2 = ItemDummy.itemDummy2();

        List<CartItem> cartItems = List.of(CartItem.builder().totalPrice(100000).count(3).cart(cart).item(item1).build(),
                CartItem.builder().totalPrice(100000).count(3).cart(cart).item(item2).build());

        given(itemRepository.findById(anyLong())).willReturn(Optional.of(item1), Optional.of(item2));

        //when
        assertThatThrownBy(() -> orderService.createOrderByMyCart(cartItems, PaymentMethod.POINT, user))
                .isInstanceOf(LockOfMoneyException.class);

        //then
        verify(itemRepository, times(1)).findById(anyLong());
    }

}