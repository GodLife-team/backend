package com.god.life.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id")
    private Long id;

    @Column(name = "original_name")
    private String originalName;

    @Column(name = "server_name")
    private String serverName;

    @JoinColumn(name = "member_Id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Member member; // 멤버는 여러 사진을 가질 수 있으므로 멤버 : 사진 = 1 : N 관계

    @JoinColumn(name = "board_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Board board; // 하나의 게시판에는 여러 사진을 가질 수 있으므로 게시판 : 사진 = 1 : N 관계


    public void updateUploader(Member member) {
        this.member = member;
    }
}
