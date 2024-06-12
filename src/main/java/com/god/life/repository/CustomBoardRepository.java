package com.god.life.repository;

import com.god.life.domain.Board;
import com.god.life.dto.BoardResponse;
import com.god.life.dto.BoardSearchRequest;
import com.god.life.dto.BoardSearchResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CustomBoardRepository {


    Page<Board> findBoardWithSearchRequest(BoardSearchRequest boardSearchRequest, Pageable pageable);

    List<BoardSearchResponse> findWeeklyPopularBoard();

    List<BoardSearchResponse> findTotalPopularBoard();

}
