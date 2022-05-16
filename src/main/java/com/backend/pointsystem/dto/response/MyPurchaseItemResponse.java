package com.backend.pointsystem.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MyPurchaseItemResponse {

    private List<MyPurchaseResponse> myPurchaseResponses = new ArrayList<>();

}
