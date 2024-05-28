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


    @Positive(message = "{BoardSearchRequest.page}")
    Integer page;

    String keyword;

    String tags;

}
