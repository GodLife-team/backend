package com.god.life.dto;


import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BoardSearchRequest {


    @Positive(message = "페이지 번호는 자연수여야 합니다.")
    Integer page = 0;

    String keyword;

    String tags;

    String nickname;

}
