package com.backend.pointsystem.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CartRequest {

    @NotNull
    private Long itemId;

    @NotNull
    private Integer itemCount;

}
