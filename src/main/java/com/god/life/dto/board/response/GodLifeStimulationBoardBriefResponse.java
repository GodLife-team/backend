package com.god.life.dto.board.response;


import com.god.life.domain.Board;
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

    @Schema(description = "해당 게시판의 갓생 점수")
    private Integer godLifeScore = 0;

    @Schema(description = "해당 게시판의 조회 수")
    private Integer view = 0;

    public static GodLifeStimulationBoardBriefResponse of(Board board) {
        return new GodLifeStimulationBoardBriefResponse(board.getTitle(), board.getId(), board.getThumbnailUrl(),
                board.getIntroduction(), board.getMember().getNickname(), board.getTotalScore(), board.getView());
    }
}
