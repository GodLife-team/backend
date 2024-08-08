package com.god.life.domain;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

//알람 정보
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class Alarm extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long alarmId;

    private Long boardId; //알림이 생긴 게시물 번호

    private String title; //알람 제목

    private String content; //알람 내용

    private Long memberId; //알람을 받은 회원 번호

    private boolean isRead; //읽었는지 유무

    @Enumerated(EnumType.STRING)
    @Column(name = "category_type")
    private CategoryType categoryType; // 알람 게시판 종류


    public void checkRead(){
        this.isRead = true;
    }

}
