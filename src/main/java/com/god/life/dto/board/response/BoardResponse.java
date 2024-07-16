package com.god.life.dto.board.response;

import com.god.life.domain.Board;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
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

    @Schema(description = "닉네임")
    private String nickname;

    @Schema(description = "자기소개")
    private String whoAmI;

    @Schema(description = "프로필이미지")
    private String profileURL;

    @Schema(description = "작성자 티어")
    private String tier;

    @Schema(description = "이미 해당 게시물에 좋아요를 눌렀는지")
    private boolean memberLikedBoard;

    @Schema(description = "해당 게시물 작성자 회원 ID")
    private Long writerId;

    public static BoardResponse of(Board board, Boolean isOwner, boolean likedBoard) {
        BoardResponse response = BoardResponse.builder()
                .board_id(board.getId())
                .body(board.getContent())
                .isBoardOwner(isOwner)
                .views(board.getView())
                .title(board.getTitle())
                .tags(board.toListTag())
                .nickname(board.getMember().getNickname())
                .whoAmI(board.getMember().getWhoAmI())
                .writtenAt(board.getCreateDate().toLocalDate())
                .tier("브론즈")
                .godScore(board.getTotalScore())
                .writerId(board.getMember().getId())
                .profileURL(board.getMember().getProfileName() == null ? "" : board.getMember().getProfileName())
                .memberLikedBoard(likedBoard).build();

        return response;
    }

    public static BoardResponse of(Board board, boolean isOwner, boolean likedBoard, int godLifeScore) {
        BoardResponse response = BoardResponse.builder()
                .board_id(board.getId())
                .body(board.getContent())
                .isBoardOwner(isOwner)
                .views(board.getView())
                .title(board.getTitle())
                .tags(board.toListTag())
                .nickname(board.getMember().getNickname())
                .whoAmI(board.getMember().getWhoAmI())
                .writtenAt(board.getCreateDate().toLocalDate())
                .tier("브론즈")
                .godScore(godLifeScore)
                .writerId(board.getMember().getId())
                .profileURL(board.getMember().getProfileName() == null ? "" : board.getMember().getProfileName())
                .memberLikedBoard(likedBoard).build();

        return response;
    }
}
