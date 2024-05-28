package com.god.life.repository;


import com.god.life.domain.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {


    @Query("select c from Comment c join fetch c.member where c.board.id = :boardId order by c.createDate asc")
    List<Comment> findByBoardIdWithMember(@Param("boardId") Long id);


    @Query("select c from Comment c join fetch c.member where c.id = :commentId")
    Optional<Comment> findByIdWithMember(@Param("commentId") Long commentId);
}

