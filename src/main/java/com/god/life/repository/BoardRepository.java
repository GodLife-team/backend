package com.god.life.repository;

import com.god.life.domain.Board;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BoardRepository extends JpaRepository<Board, Long> {



    @Query("select b from Board b join fetch b.member where b.id = :boardId")
    Optional<Board> findByIdWithMember(@Param("boardId") Long boardId);


    @Query(value = "select b from Board b", countQuery = "select count(b) from Board b")
    Page<Board> findByBoardfetchjoin(Pageable pageable);

}
