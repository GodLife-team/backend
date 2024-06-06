package com.god.life.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.*;

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

    @Column(name = "refresh_token", length = 1024)
    private String refreshToken;

    @Column(nullable = false)
    private String nickname;

    @Column(name = "provider_id", nullable = false)
    private String providerId;

    @Column(nullable = false)
    private String email;

    @Enumerated(value = EnumType.STRING)
    private Sex sex;

    private Integer age;

    @Column(name = "god_life_point")
    private long godLifePoint;

    @Column(name = "introduction")
    private String whoAmI;

    // 조회 용도, mappedBy : 조회만 할 것이고, member 조회시 member_id에 대응되는 이미지 다 가져오게함.
    @OneToMany(mappedBy = "member", fetch = FetchType.LAZY)
    private List<Image> images = new ArrayList<>();


    @Override
    public String toString() {
        return "Member{" +
                "id=" + id +
                ", nickname='" + nickname + '\'' +
                ", email='" + email + '\'' +
                '}';
    }

    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public void updateWhoAmI(String whoAmI){
        this.whoAmI = whoAmI;
    }
}
