package com.god.life.dto.alarm.response;


import com.god.life.domain.Alarm;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AlarmResponse {

    @Schema(description = "알람 번호")
    private Long alarmId;

    @Schema(description = "알람이 발생한 게시물 번호")
    private Long boardId;

    @Schema(description = "알람 제목")
    private String title;

    @Schema(description = "알람 내용")
    private String content;

    @Schema(description = "읽음 유무")
    private boolean isRead;

    public static AlarmResponse of(Alarm alarm) {
        return new AlarmResponse(alarm.getAlarmId(), alarm.getBoardId(),
                alarm.getTitle(), alarm.getContent(), alarm.isRead());
    }


}
