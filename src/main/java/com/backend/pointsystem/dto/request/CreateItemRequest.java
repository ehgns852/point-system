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
    private Integer price;

    @NotNull
    private Integer stockQuantity;

    @NotNull
    private Integer pointRatio;

    @NotNull
    private String owner;

    @NotNull
    private ItemStatus itemStatus;
}
