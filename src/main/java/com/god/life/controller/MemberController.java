package com.god.life.controller;

import com.god.life.annotation.LoginMember;
import com.god.life.domain.Member;
import com.god.life.dto.*;
import com.god.life.dto.common.CommonResponse;
import com.god.life.error.JwtInvalidException;
import com.god.life.error.NotFoundResource;
import com.god.life.service.GodLifeScoreService;
import com.god.life.service.ImageService;
import com.god.life.service.ImageUploadService;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "로그인 및 회원가입 API", description = "로그인과 회원가입시 이용되는 API 입니다.")
@Slf4j
public class MemberController {

    private final MemberService memberService;
    private final JwtUtil jwtUtil;
    private final ImageService imageService;
    private final ImageUploadService imageUploadService;
    private final GodLifeScoreService godLifeScoreService;

    @Operation(summary = "닉네임 중복체크")
    @Parameter(name = "nickname", required = true, description = "중복을 확인할 닉네임")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "body에 중복이면 false, 중복이 아니면 true",
                            useReturnTypeSchema = true)
            }
    )
    @GetMapping("/check/nickname")
    public ResponseEntity<CommonResponse<Boolean>> checkNickname(@RequestParam(value = "nickname") String nickname) {
        if (nickname == null) {
            return ResponseEntity.ok().body(new CommonResponse<>(HttpStatus.BAD_REQUEST, false));
        }
        if (!StringUtils.hasText(nickname) || nickname.length() > 10) {
            return ResponseEntity.ok().body(new CommonResponse<>(HttpStatus.BAD_REQUEST, false));
        }

        //중복된 경우
        boolean check = memberService.checkDuplicateNickname(nickname);
        if (check) {
            return ResponseEntity.ok()
                    .body(new CommonResponse<>(HttpStatus.BAD_REQUEST, false));
        }

        return ResponseEntity.ok().body(new CommonResponse<>(HttpStatus.OK, true));
    }

    @Operation(summary = "가입 여부 확인")
    @Parameter(name = "memberId", required = true, description = "카카오 ID")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "이미 가입한 경우 : alreadySignup = true \t\n 가입하지 않은 경우 : alreadySignup : false",
                            useReturnTypeSchema = true)
            }
    )
    @GetMapping("/check/id")
    public ResponseEntity<CommonResponse<AlreadySignUpResponse>> checkAlreadySignup(@RequestParam(value = "memberId") String memberId) {
        AlreadySignUpResponse response = new AlreadySignUpResponse("", "", "false");

        if (!StringUtils.hasText(memberId)) {
            return ResponseEntity.ok().body(new CommonResponse<>(HttpStatus.BAD_REQUEST, response));
        }

        boolean alreadySignup = memberService.checkAlreadySignup(memberId);
        if (alreadySignup) {
            TokenResponse token = memberService.reissueToken(memberId);
            response.updateResponse(token, "true");
            return ResponseEntity.ok().body(new CommonResponse<>(HttpStatus.OK, response));
        }

        return ResponseEntity.ok()
                .body(new CommonResponse<>(HttpStatus.OK, response));
    }

    @Operation(summary = "이메일 중복체크")
    @Parameter(name = "email", required = true, description = "중복을 확인할 이메일")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "body에 중복이면 false, 중복이 아니면 true",
                            useReturnTypeSchema = true)
            }
    )
    @GetMapping("/check/email")
    public ResponseEntity<CommonResponse<Boolean>> checkEmail(@RequestParam(name = "email") String email) {
        if (email == null) {
            return ResponseEntity.ok().body(new CommonResponse<>(HttpStatus.BAD_REQUEST, false));
        }
        if (!StringUtils.hasText(email)) {
            return ResponseEntity.ok().body(new CommonResponse<>(HttpStatus.BAD_REQUEST, false));
        }

        //중복된 경우
        boolean check = memberService.checkDuplicateEmail(email);
        if (check) {
            return ResponseEntity.ok()
                    .body(new CommonResponse<>(HttpStatus.BAD_REQUEST, false));
        }

        return ResponseEntity.ok().body(new CommonResponse<>(HttpStatus.OK, true));
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

    @Operation(summary = "reissue access Token", description = "refreshToken과 accessToken을 재발급합니다.")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "body에 access Token과 refreshToken 발급",
                            useReturnTypeSchema = true),
                    @ApiResponse(responseCode = "400", description = "Refresh Token이 유효하지 않음, 재로그인 필요")
            }
    )
    @Parameter(name="Authorization", description = "Bearer {Refresh Token}형태", required = true)
    @PostMapping("/reissue")
    public ResponseEntity<CommonResponse<TokenResponse>> reissueToken(HttpServletRequest request) {
        String jwtHeader = request.getHeader(JwtUtil.AUTHORIZE_HEADER);
        if (jwtHeader == null) {
            throw new JwtInvalidException("refresh 토큰이 존재하지 않습니다.");
        }

        String jwt = JwtUtil.parseJwt(jwtHeader);
        jwtUtil.validateRefreshJwt(jwt);

        // 새로운 Refresh Token과 accessToken 재발급
        TokenResponse response = memberService.updateRefreshToken(jwt);

        return ResponseEntity
                .ok(new CommonResponse<>(HttpStatus.OK, response));
    }

    @Operation(summary = "현재 로그인 중인 유저 정보를 조회합니다.")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Body에 유저 정보가 담인 DTO 반환",
                    useReturnTypeSchema = true),
            }
    )
    @Parameter(name="Authorization", description = "Bearer {Access Token}형태", required = true)
    @GetMapping("/member")
    public ResponseEntity<CommonResponse<LoginInfoResponse>> loginUserInfo(@LoginMember Member loginMember) {
        log.info("login member = {}", loginMember);

        LoginInfoResponse userInfo = memberService.getUserInfo(loginMember.getId());
        // 로그인 한 유저 갓생점수 가져오기
        Integer memberScore = godLifeScoreService.calculateGodLifeScoreMember(loginMember);
        userInfo.setGodLifeScore(memberScore);

        return ResponseEntity.ok((new CommonResponse<>(HttpStatus.OK, userInfo)));
    }


    @Operation(summary = "이미지 업로드", description = "요청된 타입에 따른 사진을 저장합니다. (프로필:profile, 배경:background)")
    @PostMapping(value = "/image-upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Parameter(name="Authorization", description = "Bearer {Access Token}형태", required = true)
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Body에 이미지 저장 주소반환",
                            useReturnTypeSchema = true),
            }
    )
    public ResponseEntity<CommonResponse<ImageSaveResponse>> profileUpload(
            @ModelAttribute ImageUploadRequest uploadRequest,
            @LoginMember Member loginMember) throws IOException {

        //ImageSaveResponse save = imageService.uploadImage(file.getImage(), loginMember);
        ImageSaveResponse response = imageUploadService.upload(uploadRequest.getImage());
        response.setServerName(uploadRequest.getImageType() + response.getServerName());
        imageService.deleteTypeImage(uploadRequest.getImageType() + "%", loginMember.getId());
        imageService.saveImage(response, loginMember, null);
        response.setServerName(response.getServerName().substring(uploadRequest.imageType.length()));

        return ResponseEntity.ok((new CommonResponse<>(HttpStatus.OK, response)));
    }

    @Operation(summary = "자기소개 변경")
    @PatchMapping("/member")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "변경 성공시 body에 true, 실패시 false",
                            useReturnTypeSchema = true),
            }
    )
    public ResponseEntity<CommonResponse<Boolean>> modifyWhoAmI(@LoginMember Member member,
                                       @RequestBody ModifyWhoAmIRequest modifyWhoAmIRequest) {

        return ResponseEntity.ok(new CommonResponse<>(HttpStatus.OK, memberService.updateWhoAmI(member.getId(), modifyWhoAmIRequest)));
    }

    @Operation(summary = "로그아웃")
    @PostMapping("/member")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "현재 로그인 중 사용자 로그아웃 처리 --> 리프레시 토큰 삭제",
                            useReturnTypeSchema = true),
            }
    )
    public ResponseEntity<CommonResponse<String>> logout(@LoginMember Member member) {
        memberService.removeRefreshToken(member.getId());
        return ResponseEntity.ok(new CommonResponse<>(HttpStatus.OK, "로그아웃 되었습니다."));
    }

    @Operation(summary = "회원탈퇴")
    @DeleteMapping("/member")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "현재 로그인 중 사용자 탈퇴처리, 작성한 내용 모두 삭제",
                            useReturnTypeSchema = true),
            }
    )
    public ResponseEntity<CommonResponse<String>> withdrawMember(@LoginMember Member member) {
        memberService.withdrawalMember(member.getId());
        return ResponseEntity.ok(new CommonResponse<>(HttpStatus.OK, "회원탈퇴 처리가 완료되었습니다."));
    }

    @Operation(summary = "회원 정보 조회", description = "조회하는 회원의 각종 정보를 조회합니다.")
    @GetMapping("/member/{memberId}")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "memberId에 대응되는 회원 정보 조회",
                            useReturnTypeSchema = true),
                    @ApiResponse(responseCode = "400", description = "잘못된 memberId에 대응되는 회원 정보 조회")
            }
    )
    @Parameter(name = "memberId", description = "조회하려는 회원의 id")
    public ResponseEntity<CommonResponse<MemberInfoResponse>> getMemberInfo(@LoginMember Member member,
                                                @PathVariable(name = "memberId") String pathVariableMemberId) {
        Long infoMemberId = checkId(pathVariableMemberId);
        MemberInfoResponse response = memberService.memberInfoResponse(member, infoMemberId);
        return ResponseEntity.ok(new CommonResponse<>(HttpStatus.OK, response));
    }

    private Long checkId(String id) {
        Long memberId = null;
        try {
            memberId = Long.parseLong(id);
        } catch (NumberFormatException ex) {
            throw new NotFoundResource("존재하지 않는 회원입니다.");
        }

        return memberId;
    }
}
