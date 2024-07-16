package com.god.life.domain;


import com.god.life.dto.board.request.BoardCreateRequest;
import com.god.life.dto.board.request.GodLifeStimulationBoardRequest;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
@ToString(exclude = {"member", "comments", "images"})
@Table(indexes = @Index(name = "create_date_index", columnList = "create_date"))
public class Board extends BaseEntity{

    public static final int WRITE_POINT = 2;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "board_id")
    private Long id;

    private String title;

    @Column(columnDefinition = "text")
    private String content;

    // 굳이 별도의 entity를 둘 필요가 없다고 판단 1: 어떤 태그가 들어올지 모름, 2. 단순히 조회용
    private String tag;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member; //작성자

    @Column(name = "total_score")
    private Integer totalScore;

    @Column
    private int view;

    @Column
    private String thumbnailUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private BoardStatus status;

    @Column(name = "introduction")
    private String introduction;

    @OneToMany(mappedBy = "board", fetch = FetchType.LAZY)
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "board", fetch = FetchType.LAZY)
    private List<Image> images = new ArrayList<>();

    @OneToMany(mappedBy = "board", fetch = FetchType.LAZY)
    private List<GodLifeScore> godLifeScores = new ArrayList<>();

    public void updateBoard(BoardCreateRequest request) {
        this.title = request.getTitle();
        this.content = request.getContent();
        this.tag = toDBTag(request.getTags());
    }

    public void updateBoard(GodLifeStimulationBoardRequest request) {
        this.title = request.getTitle();
        this.content = request.getContent();
        this.thumbnailUrl = request.getThumbnailUrl();
        this.introduction = request.getIntroduction();
        this.tag = "";
        this.status = BoardStatus.S;
        this.view = 0;
    }

    public static String toDBTag(List<String> tags) {
        return String.join(",", tags);
    }

    public List<String> toListTag(){
        if(tag == null || tag.isEmpty()) return new ArrayList<>();
        return Arrays.stream(tag.split(",")).toList();
    }


    public void increaseViewCount() {
        this.view += 1;
    }

}
