package com.backend.pointsystem.dummy;

import com.backend.pointsystem.entity.User;

public class UserDummy {

    public static User dummyUser() {
        return User.builder()
                .asset(10000000)
                .id(1L)
                .name("김도훈")
                .username("ehgns852")
                .password("123123")
                .build();
    }

}
