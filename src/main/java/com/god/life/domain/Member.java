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

    @Column(nullable = false, unique = true)
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

    @OneToMany(mappedBy = "member", fetch = FetchType.LAZY)
    private List<Image> images = new ArrayList<>();

    @Column(name = "fcm_token")
    private String fcmToken;

    @Column(name = "profile_name")
    private String profileName;

    @Column(name = "background_name")
    private String backgroundName;

    @Column(name = "checkAlarm")
    private boolean checkAlarm; //개인 알람 on/off 유무

    @Override
    public String toString() {
        return "Member{" +
                "id=" + id +
                ", nickname='" + nickname + '\'' +
                ", email='" + email + '\'' +
                '}';
    }

    public void updateWhoAmI(String whoAmI){
        this.whoAmI = whoAmI;
    }

    public void updateProfileImageName(String profileName) {
        this.profileName = profileName;
    }

    public void updateBackgroundImageName(String backgroundName) {
        this.backgroundName = backgroundName;
    }
}
