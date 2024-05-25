package com.god.life.domain;


import com.god.life.dto.BoardCreateRequest;
import jakarta.persistence.*;
import lombok.*;

import java.util.stream.Collectors;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class Board extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "board_id")
    private Long id;

    private String title;

    private String content;

    private String tag;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member; //작성자

    @Column(name = "total_score")
    private int totalScore;

    private int view;

    public void updateBoard(BoardCreateRequest request) {
        this.title = request.getTitle();
        this.content = request.getContent();
        this.tag = String.join(",", request.getTags());
    }
}
