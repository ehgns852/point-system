package com.backend.pointsystem.controller;

import com.backend.pointsystem.dto.request.AddItemToCartRequest;
import com.backend.pointsystem.dto.request.BuyAllRequest;
import com.backend.pointsystem.dto.request.CreateOrderRequest;
import com.backend.pointsystem.dto.response.AddItemToCartResponse;
import com.backend.pointsystem.dto.response.CreateOrderResponse;
import com.backend.pointsystem.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/carts")
public class CartController {

    private final CartService cartService;

    @PostMapping
    public ResponseEntity<AddItemToCartResponse> addItemToCart(@Validated @RequestBody AddItemToCartRequest request) {
        return ResponseEntity.status(CREATED).body(new AddItemToCartResponse(cartService.addItemToCart(request)));
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<Void> deleteItemToCart(@PathVariable Long itemId) {
        cartService.deleteItemToCart(itemId);
        return ResponseEntity.status(NO_CONTENT).build();
    }

    @PostMapping("/buy-all")
    public ResponseEntity myCartBuyAll(@Validated @RequestBody BuyAllRequest request) {
        return ResponseEntity.status(CREATED).body(new CreateOrderResponse(cartService.myCartBuyAll(request)));
    }

}
