package com.backend.pointsystem.dummy;

import com.backend.pointsystem.entity.Cart;
import com.backend.pointsystem.entity.CartItem;
import com.backend.pointsystem.entity.User;

public class CartDummy {
    public static Cart dummyCart(User user) {
        return new Cart(user);
    }
}
