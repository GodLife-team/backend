package com.god.life.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ImageSaveResponse {

    private String originalName; // 원본 이름
    private String serverName;  // 서버 저장 이름


}
