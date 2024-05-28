package com.god.life.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class Comment extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long id;

    @Column(name = "reply_content")
    private String replyContent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id")
    private Board board; // 작성한 댓글이 달린 게시판

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member; //작성자

    @ManyToOne(fetch = FetchType.LAZY) // 하나의 댓글에는 여러 개의 대댓글이 달릴 수 있다.
    // name : 외래키 이름, referencedColumnname 참조하는 entity의 참조할 외래키 키 값
    // default로 해당 entity의 pk 값이 설정됨.
    @JoinColumn(name = "parent_id", referencedColumnName = "comment_id")
    private Comment parent;

    @OneToMany(mappedBy = "parent")
    private List<Comment> child = new ArrayList<>();


    public void updateComment(String comment) {
        this.replyContent = comment;
    }
}
