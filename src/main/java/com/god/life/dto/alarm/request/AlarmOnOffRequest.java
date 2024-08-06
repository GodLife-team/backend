package com.god.life.dto.alarm.request;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AlarmOnOffRequest {

    @Schema(description = "on/off 유무, true면 On , false면 off")
    private Boolean onOff;

}
