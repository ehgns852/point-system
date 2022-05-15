package com.backend.pointsystem.service;

import com.backend.pointsystem.common.UserUtil;
import com.backend.pointsystem.dto.request.CreateOrderRequest;
import com.backend.pointsystem.dto.request.OrderRequest;
import com.backend.pointsystem.entity.*;
import com.backend.pointsystem.exception.ItemNotFoundException;
import com.backend.pointsystem.repository.CartItemRepository;
import com.backend.pointsystem.repository.ItemRepository;
import com.backend.pointsystem.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final ItemRepository itemRepository;
    private final UserUtil userUtil;
    private final CartItemRepository cartItemRepository;

    @Transactional
    public Long createOrder(CreateOrderRequest request) {
        User user = userUtil.findCurrentUser();

        List<OrderItem> orderItems = createOrderItems(request, user, request.getOrderRequests());

        return orderRepository.save(Order.createOrder(user, orderItems)).getId();
    }

    private List<OrderItem> createOrderItems(CreateOrderRequest request, User user, List<OrderRequest> orderRequests) {
        List<OrderItem> orderItems = new ArrayList<>();

        for (OrderRequest order : orderRequests) {
            Item item = itemRepository.findById(order.getItemId())
                    .orElseThrow(() -> new ItemNotFoundException("해당 상품을 찾을 수 없습니다."));

            int totalPrice = getTotalPrice(order.getItemCount(), item.getPrice());
            int earnPoint = getEarnPoint(item.getPointRatio(), totalPrice);

            OrderItem orderItem = OrderItem.createOrderItem(item, request.getPaymentMethod(), totalPrice, order.getItemCount());

            confirmPaymentMethod(request.getPaymentMethod(), user, totalPrice, earnPoint);

            orderItems.add(orderItem);
        }
        return orderItems;
    }

    @Transactional
    public Long createOrder(List<CartItem> cartItems, PaymentMethod paymentMethod, User user) {

        List<OrderItem> orderItems = createOrderByCart(cartItems, paymentMethod, user);

        return orderRepository.save(Order.createOrder(user, orderItems)).getId();
    }

    private List<OrderItem> createOrderByCart(List<CartItem> cartItems, PaymentMethod paymentMethod, User user) {
        List<OrderItem> orderItems = new ArrayList<>();

        for (CartItem cartItem : cartItems) {
            Item item = itemRepository.findById(cartItem.getItem().getId())
                    .orElseThrow(() -> new ItemNotFoundException("해당 상품을 찾을 수 없습니다."));

            int earnPoint = getEarnPoint(item.getPointRatio(), cartItem.getTotalPrice());

            OrderItem orderItem = OrderItem.createOrderItem(item, paymentMethod, cartItem.getTotalPrice(), cartItem.getCount());

            confirmPaymentMethod(paymentMethod, user, cartItem.getTotalPrice(), earnPoint);

            orderItems.add(orderItem);

            cartItemRepository.delete(cartItem);
        }
        return orderItems;
    }


    private void confirmPaymentMethod(PaymentMethod paymentMethod, User user, int totalPrice, int earnPoint) {
        if (matchPaymentMethod(paymentMethod)) {
            user.deductMoney(totalPrice, earnPoint);
        } else {
            user.deductPoint(totalPrice);
        }
    }

    private int getEarnPoint(int pointRatio, int totalPrice) {
        return (int) (totalPrice * (pointRatio * 0.01));
    }

    private int getTotalPrice(int count, int price) {
        return price * count;
    }

    private boolean matchPaymentMethod(PaymentMethod paymentMethod) {
        return paymentMethod == PaymentMethod.MONEY;
    }

}
