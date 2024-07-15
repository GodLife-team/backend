package com.god.life.repository;

import com.god.life.domain.Board;
import com.god.life.domain.Member;
import com.god.life.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CustomBoardRepository {


    // 검색 조건에 맞는 게시물 조회 및 페이징 처리
    Page<Board> findBoardWithSearchRequest(BoardSearchRequest boardSearchRequest, Pageable pageable);

    // 한 주간 갓생 인증 페이지 조회
    List<BoardSearchResponse> findWeeklyPopularBoard();

    // 전체 기간 갓생 인증 페이지 조회
    List<BoardSearchResponse> findTotalPopularBoard();

    //갓셍 자극 페이지 상세 조회
    GodLifeStimulationBoardResponse findStimulusBoardEqualsBoardId(Long boardId, Member member);

    //페이징 번호에 따른 갓생 자극 페이지 조회
    Page<GodLifeStimulationBoardBriefResponse> findStimulusBoardPaging(Pageable pageable);

    //검색 조건에 포함되는 갓생 자극 페이지 조회
    List<GodLifeStimulationBoardBriefResponse> findStimulusBoardSearchCondition(StimulationBoardSearchCondition request);

    //전체 기간 인기있는 갓생 자극 페이지 10개 조회
    List<GodLifeStimulationBoardBriefResponse> findAllTimePopularStimulusBoardList();

    //전체 기간 조회수가 가장 많은 갓생 자극 페이지 10개 조회
    List<GodLifeStimulationBoardBriefResponse> findMostViewedBoardList();


    //List ids안에 있는 게시글 조회
    List<Board> getBoardInIds(List<Long> boardIds);

}
