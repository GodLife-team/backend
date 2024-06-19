package com.god.life.controller;


import com.god.life.annotation.LoginMember;
import com.god.life.domain.Member;
import com.god.life.dto.AlarmCreateRequest;
import com.god.life.dto.common.CommonResponse;
import com.god.life.dto.fcm.FcmSendDto;
import com.god.life.service.FcmAlarmService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/fcm")
@Tag(name = "FCM 알림", description = "FCM Notification 알림 메세지 입니다.")
@RequiredArgsConstructor
public class FcmController {

    private final FcmAlarmService fcmAlarmService;

    @PostMapping("/send")
    @Operation(summary = "메세지 보내기 테스트")
    public ResponseEntity<CommonResponse<String>> sendNotificationByToken(@RequestBody FcmSendDto fcmSendDto) {
        return ResponseEntity.ok(new CommonResponse<>(HttpStatus.OK, fcmAlarmService.sendNotification(fcmSendDto)));
    }

    @PostMapping("/alarm")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "알람 생성 성공", useReturnTypeSchema = true),
                    @ApiResponse(responseCode = "400", description = "현재 시간보다 이전 시간에 알람 등록")
            }
    )
    @Operation(summary = "금일 알람 시간 생성")
    public ResponseEntity<CommonResponse<Boolean>> createAlarm(@RequestBody AlarmCreateRequest alarmCreateRequest,
                                                               @LoginMember Member member) {
        fcmAlarmService.createTodayAlarm(alarmCreateRequest, member);
        return ResponseEntity.ok(new CommonResponse<>(HttpStatus.OK, Boolean.TRUE));
    }

    @DeleteMapping("/alarm")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "알람 삭제 성공", useReturnTypeSchema = true),
            }
    )
    @Operation(summary = "금일 알람 시간 삭제")
    public ResponseEntity<CommonResponse<Boolean>> deleteAlarm(@LoginMember Member member) {
        fcmAlarmService.deleteTodayAlarm(member);
        return ResponseEntity.ok(new CommonResponse<>(HttpStatus.OK, Boolean.TRUE));
    }

    @PutMapping("/alarm")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "알람 수정 성공", useReturnTypeSchema = true),
                    @ApiResponse(responseCode = "400", description = "현재 시간보다 이전 시간에 알람 등록"),
            }
    )
    @Operation(summary = "금일 알람 시간 수정")
    public ResponseEntity<CommonResponse<Boolean>> updateAlarm(@RequestBody AlarmCreateRequest alarmCreateRequest,
                                                               @LoginMember Member member) {

        fcmAlarmService.updateTodayAlarm(alarmCreateRequest, member);
        return ResponseEntity.ok(new CommonResponse<>(HttpStatus.OK, Boolean.TRUE));
    }



}
