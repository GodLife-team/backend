package com.god.life.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    @Column(name = "provider_name", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private ProviderType providerName;

    @Column(name = "refresh_token")
    private String refreshToken;

    @Column(nullable = false)
    private String nickname;

    @Column(name = "provider_token", nullable = false)
    private String providerToken;

    @Column(nullable = false)
    private String email;

    @Enumerated(value = EnumType.STRING)
    private Sex sex;

    private Integer age;


    @Column(name = "god_life_point")
    private long godLifePoint;


    @Column(name = "introduction")
    private String whoAmI;


    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
