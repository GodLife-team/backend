package com.god.life.controller;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
public class ImageUploadRequest {

    @Schema(description = "이미지 타입")
    String imageType;

    @Schema(description = "이미지 실제 파일")
    MultipartFile image;

}
