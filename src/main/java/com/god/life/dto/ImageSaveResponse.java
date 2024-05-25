package com.god.life.dto;


import com.god.life.domain.Image;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ImageSaveResponse {

    private String originalName; // 원본 이름
    private String serverName;  // 서버 저장 이름


    public static ImageSaveResponse from(Image image){
        return new ImageSaveResponse(image.getOriginalName(), image.getServerName());
    }

}
