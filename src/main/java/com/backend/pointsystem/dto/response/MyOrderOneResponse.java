package com.backend.pointsystem.dto.response;

import com.backend.pointsystem.entity.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MyOrderOneResponse {

    private Long orderId;

    private Long itemId;

    private String itemName;

    private PaymentMethod paymentMethod;

    private Integer count;

    private Integer totalPrice;

}
