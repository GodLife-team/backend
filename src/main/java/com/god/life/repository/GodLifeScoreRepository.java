package com.god.life.repository;

import com.god.life.domain.Board;
import com.god.life.domain.GodLifeScore;
import com.god.life.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GodLifeScoreRepository extends JpaRepository<GodLifeScore, Long> {

    boolean existsByBoardAndMember(Board board, Member member);

    //@Query("delete from GodLifeScore g where g.member = :member and g.board = :board")
    void deleteByBoardAndMember(Board board, Member member);

    @Query("select sum(g.score) from GodLifeScore g where g.board.id = :boardId")
    Integer calculateGodLifeScoreWithBoardId(@Param("boardId") Long boardId); //만약 좋아요가 하나도 안달려있으면 NULL,
}
