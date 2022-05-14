package com.backend.pointsystem.dto.request;

import com.backend.pointsystem.entity.ItemStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateItemRequest {

    @NotNull
    private String itemName;

    @NotNull
    private int price;

    @NotNull
    private int stockQuantity;

    @NotNull
    private int pointRatio;

    @NotNull
    private String owner;

    @NotNull
    private ItemStatus itemStatus;
}
