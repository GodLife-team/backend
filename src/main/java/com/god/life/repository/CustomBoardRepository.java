package com.god.life.repository;

import com.god.life.domain.Board;
import com.god.life.domain.Member;
import com.god.life.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CustomBoardRepository {


    Page<Board> findBoardWithSearchRequest(BoardSearchRequest boardSearchRequest, Pageable pageable);

    List<BoardSearchResponse> findWeeklyPopularBoard();

    List<BoardSearchResponse> findTotalPopularBoard();

    GodLifeStimulationBoardResponse findStimulusBoardEqualsBoardId(Long boardId, Member member);

    Page<GodLifeStimulationBoardBriefResponse> findStimulusBoardPaging(Pageable pageable);

    List<GodLifeStimulationBoardBriefResponse> findStimulusBoardSearchCondition(StimulationBoardSearchCondition request);
}
