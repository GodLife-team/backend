package com.god.life.dto.board.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class GodLifeStimulationBoardRequest {

    @Schema(description = "생성된 boardId")
    private Long boardId;

    @Schema(description = "제목")
    private String title;

    @Schema(description = "본문")
    private String content;

    @Schema(description = "썸네일URL")
    private String thumbnailUrl;

    @Schema(description = "소개글")
    private String introduction;


}
