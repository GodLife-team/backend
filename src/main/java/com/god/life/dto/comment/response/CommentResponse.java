package com.god.life.dto.comment.response;


import com.god.life.domain.Comment;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import static com.god.life.util.DateUtil.formattingTimeDifference;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
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

    @Schema(description = "언제 작성된 댓글인지")
    private String writtenAt;

    @Schema(description = "프로필 URL")
    private String profileURL;

    @Schema(description = "수정/삭제 유무")
    private boolean isCommentOwner;


    public static CommentResponse of(Comment comment, Long loginMemberId) {
        return CommentResponse
                .builder()
                .comment_id(comment.getId())
                .writer_id(comment.getMember().getId())
                .nickname(comment.getMember().getNickname())
                .comment(comment.getReplyContent())
                .aboutMe(comment.getMember().getWhoAmI())
                .writtenAt(formattingTimeDifference(comment.getCreateDate()))
                .isCommentOwner(comment.getMember().getId().equals(loginMemberId))
                .profileURL(comment.getMember().getProfileName() == null ? "" : comment.getMember().getProfileName())
                .build();
    }

}
