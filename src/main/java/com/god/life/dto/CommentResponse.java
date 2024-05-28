package com.god.life.dto;


import com.god.life.domain.Comment;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.cglib.core.Local;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.temporal.ChronoUnit;

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
                .build();
    }

    private static String formattingTimeDifference(LocalDateTime createDate) {
        LocalDateTime now = LocalDateTime.now();

        long yearDifference = ChronoUnit.YEARS.between(createDate, now);
        if (yearDifference > 0) {
            return yearDifference + "년 전";
        }

        long monthDifference = ChronoUnit.MONTHS.between(createDate, now);
        if (monthDifference > 0) {
            return monthDifference + "개월 전";
        }

        long dayDifference = ChronoUnit.DAYS.between(createDate, now);
        if (dayDifference > 0) {
            return dayDifference + "일 전";
        }

        long hourDifference = ChronoUnit.HOURS.between(createDate, now);
        if (hourDifference > 0) {
            return hourDifference + "시간 전";
        }

        long minutesDifference = ChronoUnit.MINUTES.between(createDate, now);
        if (minutesDifference > 0) {
            return minutesDifference + "분 전";
        }

        long secondDifference = ChronoUnit.SECONDS.between(createDate, now);
        if (secondDifference > 0) {
            return secondDifference + "초 전";
        }

        return "방금 전";
    }

}
