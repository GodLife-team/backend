package com.god.life.controller;

import com.god.life.dto.board.response.GodLifeStimulationBoardBriefResponse;
import com.god.life.dto.common.CommonResponse;
import com.god.life.dto.recommend.response.RecommendAuthorResponse;
import com.god.life.service.RecommendService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "추천 작가/게시물 조회", description = "관리자가 선정한 추천 작가, 게시물을 조회하는 API")
public class RecommendController {

    private final RecommendService recommendService;

    /**
     * 추천 갓생 자극 게시물 미리보기 조회
     */
    @Operation(summary = "관리자가 선정한 추천 게시물 조회")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "갓생 자극 게시물 미리보기 값 전달 \n" +
                            "아직 선정되지 않았다면 Empty List로 반환",
                            useReturnTypeSchema = true)
            }
    )
    @GetMapping("/recommend/board")
    public ResponseEntity<CommonResponse<List<GodLifeStimulationBoardBriefResponse>>> getRecommendBoard() {
        return ResponseEntity.ok(new CommonResponse<>(HttpStatus.OK, recommendService.findRecommendBoards()));
    }

    /**
     * 추천 작가의 갓생 자극 게시물 리스트 조회
     */
    @Operation(summary = "관리자가 선정한 작가의 갓생 자극 게시물 조회")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "갓생 자극 게시물 미리보기 값 전달 \n" +
                            "아직 선정되지 않았다면 Empty List로 반환",
                            useReturnTypeSchema = true)
            }
    )
    @GetMapping("/recommend/author")
    public ResponseEntity<CommonResponse<RecommendAuthorResponse>> getRecommendWithAuthor() {
        return ResponseEntity.ok(new CommonResponse<>(HttpStatus.OK, recommendService.findBoardWrittenAuthor()));
    }

}
