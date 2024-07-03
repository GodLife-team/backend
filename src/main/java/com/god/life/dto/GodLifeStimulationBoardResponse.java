package com.god.life.dto;

import com.god.life.util.DateUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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

    @Schema(description = "조회수")
    private int view;

    @Schema(description = "작성 일자")
    private String createDate;

    public GodLifeStimulationBoardResponse(
            String title,
            String thumbnailUrl,
            String introduction,
            String content,
            Long boardId,
            String nickname,
            Long writerId,
            Integer view,
            LocalDateTime createDate,
            Integer godLifeScore
    ){
        this.title = title;
        this.thumbnailUrl =thumbnailUrl;
        this.introduction = introduction;
        this.content = content;
        this.boardId = boardId;
        this.nickname = nickname;
        this.writerId = writerId;
        this.view = view;
        this.godLifeScore = godLifeScore;
        this.createDate = DateUtil.formattingTimeDifference(createDate);
    }



}
