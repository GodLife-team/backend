package com.god.life.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class GodLifeStimulationBoardBriefResponse {

    @Schema(description = "제목")
    private String title;

    @Schema(description = "게시판 번호")
    private Long boardId;

    @Schema(description = "썸네일 URL")
    private String thumbnailUrl;

    @Schema(description = "갓생 자극 게시물 소개글")
    private String introduction;

    @Schema(description = "작성자 nickname")
    private String nickname;

}
