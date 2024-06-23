package com.god.life.domain;


import com.god.life.error.BadRequestException;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FcmAlarm extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fcm_alarm_id")
    private Long fcmTokenId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member; // 회원

    private LocalDateTime sendTime; // 알림 전송 시각 저장

    public FcmAlarm(Member member, LocalDateTime sendTime) {
        this.member = member;
        updateAlarm(sendTime);
    }

    // 알람 시간 변경
    public void updateAlarm(LocalDateTime updateSendTime) {
        if (updateSendTime.isBefore(LocalDateTime.now())) {
            throw new BadRequestException("현재 시간보다 이전의 알람을 등록할 수 없습니다");
        }
        this.sendTime = updateSendTime;
    }


}
