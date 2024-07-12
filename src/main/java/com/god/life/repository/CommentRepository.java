package com.god.life.repository;


import com.god.life.domain.Comment;
import com.god.life.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {


    // 해당 게시글에 작성된 댓글들을 조회합니다.
    @Query("select c from Comment c join fetch c.member where c.board.id = :boardId order by c.createDate asc")
    List<Comment> findByBoardIdWithMember(@Param("boardId") Long id);


    // commentId에 대한 댓글을 조회합니다.
    @Query("select c from Comment c join fetch c.member where c.id = :commentId")
    Optional<Comment> findByIdWithMember(@Param("commentId") Long commentId);

    // 해당 회원이 작성한 댓글을 삭제합니다.
    @Modifying
    void deleteByMember(Member deleteMember);

    void deleteByBoardId(Long boardId);
}

