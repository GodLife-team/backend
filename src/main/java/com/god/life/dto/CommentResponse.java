package com.god.life.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentResponse {

    @Schema(description = "댓글 번호")
    private Long comment_id;

    @Schema(description = "댓글 작성자 회원 번호")
    private Long writer_id;

    @Schema(description = "댓글 작성자 닉네임")
    private String nickname;

    @Schema(description = "댓글 본문")
    private String comment;

    @Schema(description = "프로필 소개글")
    private String aboutMe;

    @Schema(description = "댓글 작성 일자")
    private String writtenAt;

    @Schema(description = "프로필 URL")
    private String profileURL;


}
