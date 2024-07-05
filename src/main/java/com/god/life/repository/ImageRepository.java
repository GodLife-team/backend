package com.god.life.repository;

import com.god.life.domain.Board;
import com.god.life.domain.Image;
import com.god.life.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {


    List<Image> findByBoardId(Long board);

    // 게시판이 있는 이미지는 게시판 삭제시 삭제 진행
    @Modifying
    void deleteByMember(Member member);

    @Modifying
    @Query("delete from Image i where i.board.id = :boardId")
    void deleteByBoardId(@Param("boardId") Long boardId);

    @Modifying
    @Query("delete from Image i where i.serverName like :imageType and i.member.id = :memberId")
    void deleteImageType(@Param("imageType") String imageType, @Param("memberId") Long memberId);

    @Modifying
    @Query("delete from Image i where i.board.id in :boardIds")
    void deleteByBoardIds(@Param("boardIds") List<Long> boardIds);

    @Modifying
    @Query("delete from Image i where i.board.id = :boardId and i.serverName not in :imageNames")
    void deleteUnusedImageOnBoard(@Param("imageNames") List<String> usedImageNames, @Param("boardId") Long boardId);

    Optional<Image> findByMemberAndServerName(Member member, String serverName);
}
