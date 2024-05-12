package com.god.life.controller;

import com.god.life.dto.CommonResponse;
import com.god.life.dto.SignupRequest;
import com.god.life.dto.SignupResponse;
import com.god.life.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "로그인 및 회원가입 API", description = "로그인과 회원가입시 이용되는 API 입니다.")
public class MemberController {

    private final MemberService memberService;

    @Operation(summary = "회원가입", description = "회원가입을 진행합니다.")
    @Parameter(name = "request", required = true)
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "body에 accessToken과 refreshToken 발급",
                        content = @Content(schema = @Schema(implementation = SignupResponse.class))),
                    @ApiResponse(responseCode = "400", description = "특정한 값에 유효하지 않는 값이 설정됨. body 확인")
            }
    )
    @PostMapping("/signup")
    public ResponseEntity<CommonResponse<SignupResponse>> signup(@Valid @RequestBody SignupRequest request) {
        return ResponseEntity.ok(new CommonResponse<>(HttpStatus.OK, memberService.signUp(request)));
    }





}
