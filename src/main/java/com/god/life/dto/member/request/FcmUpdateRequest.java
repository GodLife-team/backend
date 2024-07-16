package com.god.life.dto.member.request;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FcmUpdateRequest {

    @NotBlank
    @Schema(description = "FCM Token 값")
    String fcmToken;

}
