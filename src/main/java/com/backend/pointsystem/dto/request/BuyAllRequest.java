package com.backend.pointsystem.dto.request;

import com.backend.pointsystem.entity.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BuyAllRequest {

    @NotNull
    private PaymentMethod paymentMethod;

}
