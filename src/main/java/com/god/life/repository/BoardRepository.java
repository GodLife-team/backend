package com.god.life.repository;

import com.god.life.domain.Board;
import com.god.life.domain.BoardStatus;
import com.god.life.domain.CategoryType;
import com.god.life.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BoardRepository extends JpaRepository<Board, Long>, CustomBoardRepository {

    // boardId에 대한 글 조회합니다
    @Query("select b from Board b join fetch b.member where b.id = :boardId and b.category.categoryType = :categoryType")
    Optional<Board> findByIdWithMember(@Param("boardId") Long boardId, @Param("categoryType") CategoryType categoryType);

    @Query("select b from Board b join fetch b.member m where b.id = :boardId")
    Board findMemberByBoardId(@Param("boardId") Long boardId);

    // 해당 회원이 작성한 글 삭제합니다
    @Modifying
    @Query("delete Board b where b.member = :member")
    void deleteByMember(@Param("member") Member deleteMember);

    // Category에 맞는 글을 조회합니다
    @Query(value = "select b from Board b join b.category where b.category.categoryType = :categoryType")
    List<Board> getBoardsByCategory(@Param("categoryType") CategoryType categoryType);

    // 최종 저장을 위해 임시 저장된 게시판을 가져옵니다.
    @Query("select b from Board b join fetch b.member where b.id = :boardId and b.status = :status")
    Optional<Board> findTemporaryBoardByIdAndBoardStatus(@Param("boardId") Long boardId,
                                                          @Param("status") BoardStatus status);

    // 하루 전까지 미완성된 게시글 ID를 가져옵니다
    @Query("select b.id from Board b join b.category where b.createDate < :date and b.status = :status and b.category.categoryType = :categoryType")
    List<Long> findIncompleteBoardsBeforeDate(@Param("date") LocalDateTime date,
                                              @Param("status") BoardStatus status,
                                              @Param("categoryType") CategoryType categoryType);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update Board b set b.totalScore = b.totalScore + 2 where b.id = :boardId")
    void incrementGodLifeScore(@Param("boardId") Long boardId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update Board b set b.totalScore = b.totalScore - 2 where b.id = :boardId")
    void decrementGodLifeScore(@Param("boardId") Long boardId);

    @Query("select sum(b.totalScore) from Board b where b.member = :member")
    Integer totalScoreBoardByLoginMember(@Param("member") Member loginMember);

    @Query("select b from Board b join fetch b.member join b.category " +
            "where b.member.nickname = :author and b.status = 'S' and b.category.categoryType = 'GOD_LIFE_STIMULUS'")
    List<Board> findBoardWrittenAuthor(@Param("author") String author);

    @Query("select b from Board b join fetch b.category where b.id = :boardId")
    Optional<Board> findByIdWithCategory(@Param("boardId") Long boardId);


    @Query("select b.id from Board b where b.member = :member")
    List<Long> findAllBoardIdByMember(@Param("member") Member deteleMember);
}
