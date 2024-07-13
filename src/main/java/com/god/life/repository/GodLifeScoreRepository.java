package com.god.life.repository;

import com.god.life.domain.Board;
import com.god.life.domain.GodLifeScore;
import com.god.life.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GodLifeScoreRepository extends JpaRepository<GodLifeScore, Long> {

    boolean existsByBoardAndMember(Board board, Member member);

    void deleteByBoardAndMember(Board board, Member member);

    // boardId 에 대응되는 게시판의 갓생 점수 합
    @Query("select sum(g.score) from GodLifeScore g where g.board.id = :boardId")
    Integer calculateGodLifeScoreWithBoardId(@Param("boardId") Long boardId); //만약 좋아요가 하나도 안달려있으면 NULL,

    // memberId 가 받은 갓생 점수 합
    @Query("select sum(g.score) from GodLifeScore g join g.board where g.board.member.id = :memberId or g.likedMember.id = :memberId")
    Integer calculateGodLifeScoreWithMember(@Param("memberId") Long memberId);

    @Modifying
    void deleteByMember(Member deleteMember);


    @Modifying
    @Query("delete GodLifeScore g where g.board.id = :boardId")
    void deleteByBoardId(@Param("boardId") Long boardId);
}
