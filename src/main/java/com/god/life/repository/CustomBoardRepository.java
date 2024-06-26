package com.god.life.repository;

import com.god.life.domain.Board;
import com.god.life.domain.Member;
import com.god.life.dto.BoardResponse;
import com.god.life.dto.BoardSearchRequest;
import com.god.life.dto.BoardSearchResponse;
import com.god.life.dto.GodLifeStimulationBoardResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface CustomBoardRepository {


    Page<Board> findBoardWithSearchRequest(BoardSearchRequest boardSearchRequest, Pageable pageable);

    List<BoardSearchResponse> findWeeklyPopularBoard();

    List<BoardSearchResponse> findTotalPopularBoard();

    GodLifeStimulationBoardResponse findStimulusBoardEqualsBoardId(Long boardId, Member member);

    Page<GodLifeStimulationBoardResponse> findStimulusBoardPaging(Pageable pageable);
}
