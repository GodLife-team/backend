package com.god.life.controller;


import com.god.life.dto.BoardSearchResponse;
import com.god.life.dto.GodLifeStimulationBoardBriefResponse;
import com.god.life.dto.PopularMemberResponse;
import com.god.life.dto.common.CommonResponse;
import com.god.life.service.BoardService;
import com.god.life.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "인기 관련 API", description = "명예의 전당 회원 조회/인기 게시물 조회 API 입니다.")
@Slf4j
public class PopularController {

    private final MemberService memberService;
    private final BoardService boardService;

    @Operation(summary = "주간 명예의 전당",
            description = "한 주간 가장 많이 굿생 인정을 받은 회원 최대 10명을 반환합니다.")
    @GetMapping("/popular/members/weekly")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "한 주간 가장 많이 인정을 받은 회원",
                            useReturnTypeSchema = true)
            }
    )
    public ResponseEntity<CommonResponse<List<PopularMemberResponse>>> getWeeklyPopularMember() {
        List<PopularMemberResponse> responses = memberService.searchWeeklyPopularMember();
        return ResponseEntity.ok(new CommonResponse<>(HttpStatus.OK, responses));
    }

    @Operation(summary = "전체 명예의 전당",
            description = "전체 기간 동안 가장 많이 굿생인정을 받은 최대 회원 10명을 반환합니다.")
    @GetMapping("/popular/members/all-time")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "전체 기간 동안 가장 많이 인정을 받은 회원",
                            useReturnTypeSchema = true)
            }
    )
    public ResponseEntity<CommonResponse<List<PopularMemberResponse>>> getGoatPopularMember() {
        List<PopularMemberResponse> responses = memberService.searchAllTimePopularMember();
        return ResponseEntity.ok(new CommonResponse<>(HttpStatus.OK, responses));
    }

    @Operation(summary = "한 주간 인기있는 갓생 인증 게시물 조회",
            description = "한 주간 갓생인정을 가장 많이 받은 인기 게시물 10개를 반환")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "한 주간 갓생 인정 인기 게시물 조회",
                            useReturnTypeSchema = true)
            }
    )
    @GetMapping("/popular/boards/weekly")
    public ResponseEntity<CommonResponse<List<BoardSearchResponse>>> getWeeklyPopularBoard() {
        List<BoardSearchResponse> responses = boardService.searchWeeklyPopularBoardList();

        return ResponseEntity.ok(new CommonResponse<>(HttpStatus.OK, responses));
    }

    @Operation(summary = "전체기간 인기있는 갓생 인증 게시물 조회",
            description = "전체 기간동안 갓생인정을 가장 많이 받은 인기 게시물 10개를 반환")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "전체 인기있는 갓생 자극 게시물 조회",
                            useReturnTypeSchema = true)
            }
    )
    @GetMapping("/popular/boards/all-time")
    public ResponseEntity<CommonResponse<List<BoardSearchResponse>>> getTotalPopularBoards() {
        List<BoardSearchResponse> responses = boardService.searchTopPopularBoardList();
        return ResponseEntity.ok(new CommonResponse<>(HttpStatus.OK, responses));
    }

    @Operation(summary = "전체기간 인기있는 갓생 자극 게시물 조회",
            description = "전체 기간동안 갓생인정을 가장 많이 받은 인기 갓생 자극 게시물 10개 조회, 없다면 빈리스트 반환")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "전체 인기있는 갓생 인증 게시물 조회",
                            useReturnTypeSchema = true)
            }
    )
    @GetMapping("/popular/stimulus/boards/all-time")
    public ResponseEntity<CommonResponse<List<GodLifeStimulationBoardBriefResponse>>> getAllTimePopularSiBoard(){
        List<GodLifeStimulationBoardBriefResponse> responses = boardService.getAllTimePopularStimulusBoardList();
        return ResponseEntity.ok(new CommonResponse<>(HttpStatus.OK, responses));
    }


    @Operation(summary = "전체기간 조회수가 가장 많은 갓생 자극 게시물 조회",
            description = "전체 기간동안 조회수가 가장 많은 갓생 자극 게시물 10개 조회, 없다면 빈리스트 반환")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "조회수가 가장 많은 갓생 인증 게시물 조회",
                            useReturnTypeSchema = true)
            }
    )
    @GetMapping("/popular/stimulus/boards/view")
    public ResponseEntity<CommonResponse<List<GodLifeStimulationBoardBriefResponse>>> getAllTimeViewedSiBoard(){
        List<GodLifeStimulationBoardBriefResponse> responses = boardService.getMostViewedStimulusBoardList();
        return ResponseEntity.ok(new CommonResponse<>(HttpStatus.OK, responses));
    }
}
