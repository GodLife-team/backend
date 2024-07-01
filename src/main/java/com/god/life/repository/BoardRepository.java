package com.god.life.repository;

import com.god.life.domain.Board;
import com.god.life.domain.BoardStatus;
import com.god.life.domain.CategoryType;
import com.god.life.domain.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BoardRepository extends JpaRepository<Board, Long>, CustomBoardRepository {


    @Query("select b from Board b join fetch b.member where b.id = :boardId and b.category.categoryType = :categoryType")
    Optional<Board> findByIdWithMember(@Param("boardId") Long boardId, @Param("categoryType") CategoryType categoryType);

    @Query("select b from Board b where b.id = :boardId")
    Optional<Board> findByIdAnyBoardType(@Param("boardId") Long boardId);

//    @Query(value = "select b from Board b join fetch b.member where b.category = Category.categoryType", countQuery = "select count(b) from Board b join b.member")
//    Page<Board> findByBoardfetchjoin(Pageable pageable);

    @Modifying
    void deleteByMember(Member deleteMember);

    @Query(value = "select b from Board b join b.category where b.category.categoryType = :categoryType")
    List<Board> getBoardsByCategory(@Param("categoryType") CategoryType categoryType);

    @Query("select b from Board b join fetch b.member where b.id = :boardId and b.status = :status")
    Optional<Board> findTemporaryBoardByIdAndBoardStatus(@Param("boardId") Long boardId,
                                                          @Param("status") BoardStatus status);

    // 하루 전까지 미완성된 게시글 ID를 가져옴
    @Query("select b.id from Board b join b.category where b.createDate < :date and b.status = :status and b.category.categoryType = :categoryType")
    //@Query("select b.id from Board b where b.createDate < :date")
    List<Long> findIncompleteBoardsBeforeDate(@Param("date") LocalDateTime date,
                                              @Param("status") BoardStatus status,
                                              @Param("categoryType") CategoryType categoryType);


//    @Query(
//            "select b from Board b left join (select g from god_life_score g where" +
//                    " between DATE_SUB(CURDATE(), INTERVAL WEEKDAY(CURDATE()) DAY) AND NOW()) as g" +
//                    " on b.board_id = g.board_id group by b.board_id"
//    )
//    @Query("select ")
//    void ASDF();
}
