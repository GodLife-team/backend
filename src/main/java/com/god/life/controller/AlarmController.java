package com.god.life.controller;


import com.god.life.annotation.LoginMember;
import com.god.life.domain.Member;
import com.god.life.dto.alarm.response.AlarmResponse;
import com.god.life.dto.common.CommonResponse;
import com.god.life.service.alarm.AlarmServiceFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
@Slf4j
@RequiredArgsConstructor
@Tag(name = "알람 API", description = "알람과 관련된 API 입니다.")
public class AlarmController {

    private final AlarmServiceFacade alarmServiceFacade;

    @GetMapping("/alarm")
    @Operation(summary = "현재 읽지 않은 알람 조회, 없는 경우 빈 리스트로 반환")
    @ApiResponse(responseCode = "200", description = "알람", useReturnTypeSchema = true)
    public ResponseEntity<CommonResponse<List<AlarmResponse>>> getAlarm(
            @Parameter(hidden = true) @LoginMember Member member
    ) {
        return ResponseEntity.ok(new CommonResponse<>(HttpStatus.OK, alarmServiceFacade.getUnreadAlarm(member.getId())));
    }


}
