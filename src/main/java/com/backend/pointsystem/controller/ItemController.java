package com.backend.pointsystem.controller;

import com.backend.pointsystem.dto.request.CreateItemRequest;
import com.backend.pointsystem.dto.request.UpdateItemRequest;
import com.backend.pointsystem.dto.response.CreateItemResponse;
import com.backend.pointsystem.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/items")
public class ItemController {

    private final ItemService itemService;

    @PostMapping
    public ResponseEntity<CreateItemResponse> createItem(@Validated @RequestBody CreateItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(new CreateItemResponse(itemService.createItem(request)));
    }

    @PatchMapping
    public ResponseEntity<Void> updateItem(@Validated @RequestBody UpdateItemRequest request) {
        itemService.updateItem(request);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
