package com.backend.pointsystem.service;

import com.backend.pointsystem.common.UserUtil;
import com.backend.pointsystem.dto.request.CreateOrderRequest;
import com.backend.pointsystem.dto.request.OrderRequest;
import com.backend.pointsystem.dummy.ItemDummy;
import com.backend.pointsystem.dummy.UserDummy;
import com.backend.pointsystem.entity.*;
import com.backend.pointsystem.exception.LockOfMoneyException;
import com.backend.pointsystem.exception.NotEnoughStockException;
import com.backend.pointsystem.repository.CartItemRepository;
import com.backend.pointsystem.repository.ItemRepository;
import com.backend.pointsystem.repository.OrderRepository;
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



}