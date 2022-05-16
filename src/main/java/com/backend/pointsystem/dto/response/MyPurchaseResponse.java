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
public class MyPurchaseResponse {

    private Long orderId;

    private Long itemId;

    private String itemName;

    private Integer totalPrice;

    private Integer count;

    private PaymentMethod paymentMethod;

}
