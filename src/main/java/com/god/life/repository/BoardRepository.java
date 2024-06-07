package com.god.life.repository;

import com.god.life.domain.Board;
import com.god.life.domain.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BoardRepository extends JpaRepository<Board, Long>, CustomBoardRepository {



    @Query("select b from Board b join fetch b.member where b.id = :boardId")
    Optional<Board> findByIdWithMember(@Param("boardId") Long boardId);


    @Query(value = "select b from Board b join fetch b.member", countQuery = "select count(b) from Board b join b.member")
    Page<Board> findByBoardfetchjoin(Pageable pageable);

    @Modifying
    void deleteByMember(Member deleteMember);


//    @Query(
//            "select b from Board b left join (select g from god_life_score g where" +
//                    " between DATE_SUB(CURDATE(), INTERVAL WEEKDAY(CURDATE()) DAY) AND NOW()) as g" +
//                    " on b.board_id = g.board_id group by b.board_id"
//    )
//    @Query("select ")
//    void ASDF();
}
