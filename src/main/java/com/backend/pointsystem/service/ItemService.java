package com.backend.pointsystem.service;

import com.backend.pointsystem.dto.request.CreateItemRequest;
import com.backend.pointsystem.entity.Item;
import com.backend.pointsystem.exception.PointRatioSettingException;
import com.backend.pointsystem.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemService {

    private final ItemRepository itemRepository;

    @Transactional
    public Long createItem(CreateItemRequest request) {
        if (request.getPointRatio() > 100 || request.getPointRatio() < 0) {
            throw new PointRatioSettingException("포인트 비율 설정이 올바르지 않습니다.");
        }
        return itemRepository.save(addItem(request)).getId();
    }

    private Item addItem(CreateItemRequest request) {
        return Item.createItem(request.getItemName(), request.getPrice(), request.getStockQuantity(),
                request.getPointRatio(), request.getOwner(), request.getItemStatus());
    }
}
