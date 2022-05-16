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
public class UpdateItemRequest {

    @NotNull
    private Long itemId;

    private String itemName;

    private Integer price;

    private Integer stockQuantity;

    private Integer pointRatio;

    private String owner;

    private ItemStatus itemStatus;
}
