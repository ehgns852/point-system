package com.backend.pointsystem.entity;

import com.backend.pointsystem.common.BaseEntity;
import com.backend.pointsystem.exception.LockOfMoneyException;
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

    @Builder
    public User(Long id, String name, String username, String password, int asset) {
        this.id = id;
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

    public void deductMoney(int totalPrice, int earnPoint) {
        int remainingAsset = this.asset - totalPrice;
        if (remainingAsset < 0) {
            throw new LockOfMoneyException("자산이 부족합니다.");
        }
        this.asset = remainingAsset;
        this.point += earnPoint;
    }

    public void deductPoint(int totalPrice) {
        int remainingPoint = this.point - totalPrice;
        if (remainingPoint < 0) {
            throw new LockOfMoneyException("포인트가 부족합니다.");
        }
        this.point = remainingPoint;
    }
}
