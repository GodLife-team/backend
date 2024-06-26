package com.god.life.dto;

import com.god.life.domain.Board;
import com.god.life.domain.Image;
import com.god.life.util.DateUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString(exclude = {"imagesURL", "views", "isBoardOwner", "tier"})
public class BoardSearchResponse {

    @Schema(description = "게시판 번호")
    private Long board_id; //게시판 번호

    @Schema(description = "게시판 이미지 URL, 업로드 한 순서대로 반환함.")
    private List<String> imagesURL = new ArrayList<>(); //이미지 URL

    @Schema(description = "게시물 작성 시간")
    private String writtenAt;

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

    @Schema(description = "댓글 개수")
    private int commentCount; //댓글 개수

    @Schema(description = "닉네임")
    private String nickname;

    @Schema(description = "프로필URL")
    private String profileURL;

    @Schema(description = "티어")
    private String tier;

    public static BoardSearchResponse of(Board board, boolean isOwner) {
        BoardSearchResponse response = BoardSearchResponse.builder()
                .board_id(board.getId())
                .body(board.getContent())
                .isBoardOwner(isOwner)
                .views(board.getView())
                .title(board.getTitle())
                .tags(board.toListTag())
                .writtenAt(DateUtil.formattingTimeDifference(board.getCreateDate()))
                .godScore(board.getTotalScore())
                .imagesURL(board.getImages().stream().map(Image::getServerName).toList())
                .commentCount(board.getComments().size())
                .nickname(board.getMember().getNickname())
                .tier("브론즈")
                .build();

        String profileURL = "";
        List<Image> memberImages = board.getMember().getImages();
        for (Image image : memberImages) {
            if (image.getServerName().startsWith("profile")) {
                profileURL = image.getServerName().substring("profile".length());
            }
        }
        
        response.setProfileURL(profileURL);
        return response;
    }
}
