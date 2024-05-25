package com.god.life.repository;

import com.god.life.domain.Board;
import com.god.life.domain.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {


    List<Image> findByBoardId(Long board);


    @Modifying
    @Query("delete from Image i where i.board.id = :boardId")
    void deleteByBoardId(@Param("boardId") Long boardId);
}
