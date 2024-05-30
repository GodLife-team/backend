package com.god.life.dto;

import com.god.life.domain.Board;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BoardResponse {

    @Schema(description = "게시판 번호")
    private Long board_id; //게시판 번호

    @Schema(description = "게시판 이미지 URL, 업로드 한 순서대로 반환함.")
    private List<String> imagesURL = new ArrayList<>(); //이미지 URL

    @Schema(description = "게시물 작성 시간")
    private LocalDate writtenAt;

    @Schema(description = "조회수")
    private int views;

    @Schema(description = "갓생 점수")
    private int godScore;

    @Schema(description = "내용")
    private String body;

    @Schema(description = "조회한 게시물을 작성한 사람인지 유무")
    private boolean isBoardOwner;

    @Schema(description = "TAG")
    private List<String> tags;

    @Schema(description = "제목")
    private String title;


    public static BoardResponse of(Board board, Boolean isOwner) {
        return BoardResponse.builder()
                .board_id(board.getId())
                .body(board.getContent())
                .isBoardOwner(isOwner)
                .views(board.getView())
                .title(board.getTitle())
                .tags(board.toListTag())
                .writtenAt(board.getCreateDate().toLocalDate())
                .godScore(board.getTotalScore()).build();
    }
}
