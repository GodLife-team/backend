package com.god.life.service;

import com.god.life.domain.Board;
import com.god.life.dto.board.response.GodLifeStimulationBoardBriefResponse;
import com.god.life.repository.BoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendService {

    private final RedisService redisService;
    private final BoardRepository boardRepository;

    public List<GodLifeStimulationBoardBriefResponse> findRecommendBoards() {
        List<Long> boardIds = redisService.getList("board").stream().map(Long::valueOf).toList();
        if(boardIds.isEmpty()){
            return new ArrayList<>();
        }
        List<Board> boardInIds = boardRepository.getBoardInIds(boardIds);
        return boardInIds.stream().map(GodLifeStimulationBoardBriefResponse::of).toList();
    }


    public List<GodLifeStimulationBoardBriefResponse> findBoardWrittenAuthor() {
        String author = redisService.getValues("author");
        if (author.equals(RedisService.NO_VALUE)) {
            return new ArrayList<>();
        }
        List<Board> boardWrittenAuthor = boardRepository.findBoardWrittenAuthor(author);
        return boardWrittenAuthor.stream().map(GodLifeStimulationBoardBriefResponse::of).toList();
    }
}
