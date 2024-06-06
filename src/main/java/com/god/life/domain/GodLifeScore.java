package com.god.life.domain;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
@Table(uniqueConstraints = {
        @UniqueConstraint(
                name = "MEMBER_BOARD_UNIQUE",
                columnNames = {"member", "board"}
        ),
        @UniqueConstraint(
                name = "MEMBER_MEMBER_UNIQUE",
                columnNames = {"member", "liked_member"}
        )
})
public class GodLifeScore {

    private static final int BOARD_LIKE = 2;
    private static final int GOD_LIFE_AGREE = 2;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "god_life_score_id")
    private Long godLiefScoreId;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", foreignKey = @ForeignKey(name = "fk_member"))
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", foreignKey = @ForeignKey(name = "fk_board"))
    private Board board;

    private int score;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "liked_member", foreignKey = @ForeignKey(name = "fk_liked_member"))
    private Member likedMember;


    public static GodLifeScore likeMemberToBoard(Member member, Board board) {
        return GodLifeScore
                .builder()
                .board(board)
                .member(member)
                .score(BOARD_LIKE)
                .build();
    }


}
