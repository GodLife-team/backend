package com.god.life.controller;

import com.god.life.dto.CommonResponse;
import com.god.life.dto.SignupRequest;
import com.god.life.dto.SignupResponse;
import com.god.life.dto.TokenResponse;
import com.god.life.exception.JwtInvalidException;
import com.god.life.service.MemberService;
import com.god.life.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "로그인 및 회원가입 API", description = "로그인과 회원가입시 이용되는 API 입니다.")
public class MemberController {

    private final MemberService memberService;
    private final JwtUtil jwtUtil;

    @Operation(summary = "닉네임 중복체크")
    @GetMapping("/check/nickname")
    public ResponseEntity<Object> checkNickname(@RequestParam(value = "nickname") String nickname) {
        if(nickname == null){
            return ResponseEntity.badRequest().body(new CommonResponse<>(HttpStatus.BAD_REQUEST, false));
        }
        if (!StringUtils.hasText(nickname) || nickname.length() > 10) {
            return ResponseEntity.badRequest().body(new CommonResponse<>(HttpStatus.BAD_REQUEST, false));
        }

        //중복된 경우
        boolean check = memberService.checkDuplicateNickname(nickname);
        if (check) {
            return ResponseEntity.badRequest()
                    .body(new CommonResponse<>(HttpStatus.BAD_REQUEST, false));
        }

        return ResponseEntity.ok().body(new CommonResponse<>(HttpStatus.OK, true));
    }

    @Operation(summary = "이메일 중복체크")
    @GetMapping("/check/email")
    public ResponseEntity<Object> checkEmail(@RequestParam(name = "email") String email) {
        if(email == null){
            return ResponseEntity.badRequest().build();
        }
        if (!StringUtils.hasText(email)) {
            return ResponseEntity.badRequest().build();
        }

        //중복된 경우
        boolean check = memberService.checkDuplicateEmail(email);
        if (check) {
            return ResponseEntity.badRequest()
                    .body(new CommonResponse<>(HttpStatus.BAD_REQUEST, "", "중복된 이메일"));
        }

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "회원가입", description = "회원가입을 진행합니다.")
    @Parameter(name = "request", required = true)
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "body에 accessToken과 refreshToken 발급",
                            content = @Content(schema = @Schema(implementation = TokenResponse.class))),
                    @ApiResponse(responseCode = "400", description = "특정한 값에 유효하지 않는 값이 설정됨. body 확인")
            }
    )
    @PostMapping("/signup")
    public ResponseEntity<CommonResponse<TokenResponse>> signup(@Valid @RequestBody SignupRequest request) {
        return ResponseEntity.ok(new CommonResponse<>(HttpStatus.OK, memberService.signUp(request)));
    }



    @Operation(summary = "reissue access Token", description = "access Token을 재발급합니다.")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "body에 access Token과 refreshToken 발급",
                            content = @Content(schema = @Schema(implementation = TokenResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Refresh Token이 유효하지 않음, 재발급 필요")
            }
    )
    @PostMapping("/reissue")
    public ResponseEntity<CommonResponse<TokenResponse>> reissueToken(HttpServletRequest request) {
        String jwtHeader = request.getHeader(JwtUtil.AUTHORIZE_HEADER);
        if (jwtHeader == null) {
            throw new JwtInvalidException("refresh 토큰이 존재하지 않습니다.");
        }

        String jwt = JwtUtil.parseJwt(jwtHeader);
        validateJwt(jwt);

        // 새로운 Refresh Token과 accessToken 재발급
        TokenResponse response = memberService.updateRefreshToken(jwt);

        return ResponseEntity
                .ok(new CommonResponse<>(HttpStatus.OK, response));
    }

    private void validateJwt(String jwt) {
        if (jwt == null) {
            throw new JwtInvalidException("refresh 토큰이 존재하지 않습니다.");
        }

        // jwt 만료 확인
        if (jwtUtil.validateExpiredJwt(jwt)) {
            throw new JwtInvalidException("토큰이 만료되었습니다.");
        }

        // jwt 토큰 종류 확인
        if (!jwtUtil.getRole(jwt).equals(JwtUtil.REFRESH)) {
            throw new JwtInvalidException("잘못된 토큰입니다.");
        }
    }
}
