package com.god.life.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class GodLifeStimulationBoardResponse {

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

    @Schema(description = "갓생 자극 인정 수")
    private Integer godLifeScore;

    @Schema(description = "본인 수정 가능 유무")
    private boolean owner;

    @Schema(description = "게시판 html 본문")
    private String content;

    @Schema(description = "갓생 자극 작성자 ID")
    private Long writerId;

    public GodLifeStimulationBoardResponse(String title, String thumbnailUrl, String introduction, String nickname, Integer godLifeScore, boolean owner) {
        this.title = title;
        this.thumbnailUrl = thumbnailUrl;
        this.introduction = introduction;
        this.nickname = nickname;
        this.godLifeScore = godLifeScore;
        this.owner = owner;
    }



}
