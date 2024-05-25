package com.god.life.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
@Tag(name = "이미지 업로드 관련 API", description = "이미지 업로드시 사용되는 API 입니다.")
public class ImageController {

//    private final ImageService imageService;
//
//    @Operation(summary = "이미지 업로드", description = "요청된 타입에 따른 사진을 저장합니다. (프로필:profile, 배경:background)")
//    @PostMapping("/image-upload")
//    @Parameter(name="Authorization", description = "Bearer {Access Token}형태", required = true)
//    @ApiResponses(
//            value = {
//                    @ApiResponse(responseCode = "200", description = "Body에 이미지 저장 주소반환",
//                            useReturnTypeSchema = true),
//            }
//    )
//    public ResponseEntity<CommonResponse<ImageSaveResponse>> uploadTest(ImageUploadRequest file
//            , @LoginUser Member loginMember) {
//        ImageSaveResponse save = imageService.saveImage(file.getImage(), loginMember, null);
//
//        return ResponseEntity.ok((new CommonResponse<>(HttpStatus.OK, save)));
//    }



}
