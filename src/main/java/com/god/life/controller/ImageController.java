package com.god.life.controller;

import com.god.life.annotation.LoginMember;
import com.god.life.domain.Member;
import com.god.life.dto.image.ImageSaveResponse;
import com.god.life.dto.common.CommonResponse;
import com.god.life.service.ImageService;
import com.god.life.service.ImageUploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@Slf4j
@RequiredArgsConstructor
@Tag(name = "이미지 업로드 관련 API", description = "이미지 업로드시 사용되는 API 입니다.")
public class ImageController {

    private final ImageService imageService;
    private final ImageUploadService imageUploadService;

//    @GetMapping("/html/test")
//    public void test(@RequestParam("body") String html){
//        imageService.deleteUnusedImageInHtml(html, 123123L);
//    }

    @Operation(summary = "회원 프로필 이미지 업데이트", description = "전송한 이미지를 해당 회원의 프로필 사진으로 저장 및 등록합니다.")
    @PostMapping(value = "/member/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Parameter(name="Authorization", description = "Bearer {Access Token}형태", required = true)
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "프로필 이미지 반환",
                            useReturnTypeSchema = true),
            }
    )
    public ResponseEntity<CommonResponse<ImageSaveResponse>> profileImageUpload(
            @RequestParam(name = "image") MultipartFile image,
            @LoginMember Member loginMember) {

        ImageSaveResponse response = imageUploadService.upload(image);
        imageService.updateMemberProfileImage(response, loginMember);

        return ResponseEntity.ok((new CommonResponse<>(HttpStatus.OK, response)));
    }

    @Operation(summary = "회원 배경 화면 업데이트", description = "전송한 이미지를 해당 회원의 배경 사진으로 저장 및 등록합니다.")
    @PostMapping(value = "/member/background", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Parameter(name="Authorization", description = "Bearer {Access Token}형태", required = true)
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "배경 이미지 주소 반환",
                            useReturnTypeSchema = true),
            }
    )
    public ResponseEntity<CommonResponse<ImageSaveResponse>> backgroundImageUpload(
            @RequestParam(name = "image") MultipartFile image,
            @LoginMember Member loginMember) {

        ImageSaveResponse response = imageUploadService.upload(image);
        imageService.updateMemberBackgroundImage(response, loginMember);

        return ResponseEntity.ok((new CommonResponse<>(HttpStatus.OK, response)));
    }

    @PostMapping("/board/image-upload")
    @Operation(summary = "게시판 작성시 이미지 업로드 API")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "이미지 경로 반환",
                            useReturnTypeSchema = true)
            }
    )
    public ResponseEntity<CommonResponse<String>> postImage(
            @Parameter(description = "업로드할 이미지") @RequestParam("image") MultipartFile image,
            @Parameter(description = "업로드할 이미지의 게시판 번호") @RequestParam("tmpBoardId") Long tmpBoardId,
            @LoginMember Member member) {

        ImageSaveResponse response = imageUploadService.upload(image);
        imageService.saveImage(response, member, tmpBoardId);

        return ResponseEntity.ok(new CommonResponse<>(HttpStatus.OK, response.getServerName()));
    }

}
