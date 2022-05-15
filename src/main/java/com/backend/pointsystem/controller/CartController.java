package com.backend.pointsystem.controller;

import com.backend.pointsystem.dto.request.AddItemToCartRequest;
import com.backend.pointsystem.dto.response.AddItemToCartResponse;
import com.backend.pointsystem.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/carts")
public class CartController {

    private final CartService cartService;

    @PostMapping
    public ResponseEntity<AddItemToCartResponse> addItemToCart(@Validated @RequestBody AddItemToCartRequest request) {
        return ResponseEntity.status(CREATED).body(new AddItemToCartResponse(cartService.addItemToCart(request)));
    }


}
