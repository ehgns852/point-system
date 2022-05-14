package com.backend.pointsystem.entity;

import com.backend.pointsystem.common.BaseEntity;
import com.backend.pointsystem.utils.PasswordUtil;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

import static javax.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

@Entity
@NoArgsConstructor(access = PROTECTED)
@Getter
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    private int asset;

    private int point;

    @OneToMany(mappedBy = "user", orphanRemoval = true)
    private List<Order> orders = new ArrayList<>();

    @OneToOne(orphanRemoval = true)
    @JoinColumn(name = "cart_id")
    private Cart cart;


    @Builder
    public User(String name, String username, String password, int asset) {
        this.name = name;
        this.username = username;
        this.password = PasswordUtil.encode(password);
        this.asset = asset;
    }

    /**
     * 생성 메서드
     */
    public static User createUser(String name, String username, String password, int asset) {
        return User.builder()
                .name(name)
                .username(username)
                .password(password)
                .asset(asset)
                .build();
    }

}
