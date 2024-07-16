package com.god.life.dto.alarm.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AlarmCreateRequest {

    @Schema(description = "년")
    private int y;
    @Schema(description = "월")
    private int m;
    @Schema(description = "일")
    private int d;
    @Schema(description = "시간")
    private int hour;
    @Schema(description = "분")
    private int minute;

    public LocalDateTime toTime(){
        return LocalDateTime.of(y, m, d, hour, minute);
    }
}
