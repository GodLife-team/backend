package com.god.life.controller;


import com.god.life.dto.BoardSearchResponse;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "인기 관련 API", description = "명예의 전당 회원 조회/인기 게시물 조회 API 입니다.")
@Slf4j
public class PopularController {

    private final MemberService memberService;
    private final BoardService boardService;

    @Operation(summary = "주간 명예의 전당", description = "한 주간 가장 많이 갓생 인정을 받은 회원을 반환합니다.")
    @GetMapping("/popular/members/weekly")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "한 주간 가장 많이 인정을 받은 회원",
                            useReturnTypeSchema = true)
            }
    )
    public ResponseEntity<CommonResponse<List<PopularMemberResponse>>> getWeeklyPopularMember(){
        List<PopularMemberResponse> popularMemberResponses = memberService.searchWeeklyPopularMember();
        return ResponseEntity.ok(new CommonResponse<>(HttpStatus.OK, popularMemberResponses));
    }




    @Operation(summary = "한 주간 인기 있는 게시물 조회", description = "한 주간 갓생인정을 가장 많이 받은 인기 게시물 10개를 반환")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "한 주간 인기 게시물 조회",
                            //content = @Content(schema = @Schema(implementation = List.class)),
                            useReturnTypeSchema = true)
            }
    )
    @GetMapping("/popular/boards/weekly")
    public ResponseEntity<CommonResponse<List<BoardSearchResponse>>> getWeeklyPopularBoard(){
        List<BoardSearchResponse> result = boardService.searchPopularBoardList();

        return ResponseEntity.ok(new CommonResponse<>(HttpStatus.OK, result));
    }

    @Operation(summary = "전체 인기있는 게시물 조회", description = "전체 기간동안 갓생인정을 가장 많이 받은 인기 게시물 10개를 반환")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "전체 인기있는 게시물 조회",
                            //content = @Content(schema = @Schema(implementation = List.class)),
                            useReturnTypeSchema = true)
            }
    )
    @GetMapping("/popular/boards/all-time")
    public ResponseEntity<CommonResponse<List<BoardSearchResponse>>> getTotalPopularBoards(){
        List<BoardSearchResponse> result = boardService.searchTopPopularBoardList();

        return ResponseEntity.ok(new CommonResponse<>(HttpStatus.OK, result));
    }


}
