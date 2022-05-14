package com.backend.pointsystem.service;

import com.backend.pointsystem.common.UserUtil;
import com.backend.pointsystem.dto.request.CreateOrderRequest;
import com.backend.pointsystem.entity.*;
import com.backend.pointsystem.exception.ItemNotFoundException;
import com.backend.pointsystem.repository.ItemRepository;
import com.backend.pointsystem.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final ItemRepository itemRepository;
    private final UserUtil userUtil;

    @Transactional
    public Long createOrder(CreateOrderRequest request) {
        User user = userUtil.findCurrentUser();

        List<Item> items = request.getItemIds().stream()
                .map(id -> itemRepository.findById(id)
                        .orElseThrow(() -> new ItemNotFoundException("해당 상품을 찾을 수 없습니다.")))
                .collect(Collectors.toList());

        List<OrderItem> orderItems = createOrderItems(request, user, items);

        return orderRepository.save(Order.createOrder(user, orderItems)).getId();
    }

    private List<OrderItem> createOrderItems(CreateOrderRequest request, User user, List<Item> items) {
        List<OrderItem> orderItems = new ArrayList<>();

        for (Item item : items) {
            int totalPrice = item.getPrice() * request.getCount();
            int earnPoint = (int) (totalPrice * (item.getPointRatio() * 0.01));
            OrderItem orderItem = OrderItem.createOrderItem(item, request.getPaymentMethod(), totalPrice, request.getCount());
            if (request.getPaymentMethod() == PaymentMethod.MONEY) {
                user.deductMoney(totalPrice, earnPoint);
            }
            orderItems.add(orderItem);
        }
        return orderItems;
    }
}
