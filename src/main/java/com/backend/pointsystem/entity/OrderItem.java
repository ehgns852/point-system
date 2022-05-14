package com.backend.pointsystem.entity;

import com.backend.pointsystem.common.BaseEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

import static javax.persistence.FetchType.*;
import static javax.persistence.GenerationType.*;
import static lombok.AccessLevel.*;

@Entity
@NoArgsConstructor(access = PROTECTED)
@Getter
public class OrderItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "order_item_id")
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    private int totalPrice;

    private int count;


    public void setOrder(Order order) {
        this.order = order;
    }

    @Builder
    public OrderItem(Order order, Item item, PaymentMethod paymentMethod, int totalPrice, int count) {
        this.order = order;
        this.item = item;
        this.paymentMethod = paymentMethod;
        this.totalPrice = totalPrice;
        this.count = count;
    }

    public static OrderItem createOrderItem(Item item, PaymentMethod paymentMethod, int totalPrice, int count) {
        OrderItem orderItem = OrderItem.builder()
                .item(item)
                .paymentMethod(paymentMethod)
                .totalPrice(totalPrice)
                .count(count)
                .build();

        item.removeStock(count);

        return orderItem;
    }

}
