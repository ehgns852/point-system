package com.backend.pointsystem.dto.request;

import com.backend.pointsystem.entity.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {

    @NotNull
    private List<OrderRequest> orderRequests = new ArrayList<>();

    @NotNull
    private PaymentMethod paymentMethod;

}
