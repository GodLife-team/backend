package com.god.life.domain;


import com.god.life.domain.converter.ListToStringConverter;
import com.god.life.dto.BoardCreateRequest;
import com.querydsl.core.annotations.PropertyType;
import com.querydsl.core.annotations.QueryType;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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

    // 굳이 별도의 entity를 둘 필요가 없다고 판단 1: 어떤 태그가 들어올지 모름, 2. 단순히 조회용
    private String tag;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member; //작성자

    @Column(name = "total_score")
    private int totalScore;

    private int view;

    @OneToMany(mappedBy = "board", fetch = FetchType.LAZY)
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "board", fetch = FetchType.LAZY)
    private List<Image> images = new ArrayList<>();

    public void updateBoard(BoardCreateRequest request) {
        this.title = request.getTitle();
        this.content = request.getContent();
        this.tag = toDBTag(request.getTags());
    }

    public static String toDBTag(List<String> tags) {
        return String.join(",", tags);
    }

    public List<String> toListTag(){
        if(tag == null || tag.isEmpty()) return new ArrayList<>();
        return Arrays.stream(tag.split(",")).toList();
    }


}
