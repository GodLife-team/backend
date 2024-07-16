package com.god.life.dto.image;


import com.god.life.domain.Board;
import com.god.life.domain.Image;
import com.god.life.domain.Member;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ImageSaveResponse {

    @Schema(description = "원본 이미지 이름")
    private String originalName; // 원본 이름

    @Schema(description = "서버에 저장한 이미지 이름")
    private String serverName;  // 서버 저장 이름


    public static ImageSaveResponse from(Image image){
        return new ImageSaveResponse(image.getOriginalName(), image.getServerName());
    }

    public Image toEntity(Member member) {
        return Image.builder().member(member)
                .originalName(this.getOriginalName())
                .serverName(this.getServerName())
                .board(null)
                .build();
    }

    public Image toEntity(Member member, Board board) {
        return Image.builder().member(member)
                .originalName(this.getOriginalName())
                .serverName(this.getServerName())
                .board(board)
                .build();
    }
}
