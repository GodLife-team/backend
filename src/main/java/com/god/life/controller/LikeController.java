package com.god.life.controller;


import com.god.life.annotation.LoginMember;
import com.god.life.domain.Member;
import com.god.life.dto.board.BoardAlarmInfo;
import com.god.life.dto.common.CommonResponse;
import com.god.life.service.GodLifeScoreService;
import com.god.life.service.alarm.AlarmServiceFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "게시판 갓생 인정 API", description = "게시판 갓생 인정 API")
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/like")
public class LikeController {

    private final GodLifeScoreService godLifeScoreService;
    private final AlarmServiceFacade alarmServiceFacade;

    @Operation(description = "게시판ID 에 대한 갓생 인정 클릭")
    @PostMapping("/board/{boardId}")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "게시판 좋아요 누르기 성공",
                            useReturnTypeSchema = true),
                    @ApiResponse(responseCode = "400", description = "게시판 좋아요 누르기 실패",
                            useReturnTypeSchema = true)
            }
    )
    public ResponseEntity<CommonResponse<String>> agreeGodLife(
            @PathVariable(name = "boardId") Long boardId,
            @LoginMember Member member) {

        BoardAlarmInfo boardTypeDTO = godLifeScoreService.likeBoard(member, boardId);
        alarmServiceFacade.processAlarm(member.getId(), boardTypeDTO);

        return ResponseEntity.ok(new CommonResponse<>(HttpStatus.OK, "true", ""));
    }


    @Operation(description = "게시판ID 에 대한 갓생 인정 취소")
    @DeleteMapping("/board/{boardId}")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "게시판 좋아요 취소 성공",
                            useReturnTypeSchema = true),
            }
    )
    public ResponseEntity<CommonResponse<String>> cancelAgreedGodLife(
            @PathVariable(name = "boardId") Long boardId,
            @LoginMember Member member) {

        godLifeScoreService.cancelLike(boardId, member);

        return ResponseEntity.status(HttpStatus.OK)
                .body(new CommonResponse<>(HttpStatus.OK, "취소되었습니다.", ""));
    }

}
