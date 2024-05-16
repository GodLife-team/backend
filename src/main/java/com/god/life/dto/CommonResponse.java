package com.god.life.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;


@Data
@NoArgsConstructor
public class CommonResponse<T> {

    private HttpStatus status;
    private T body;
    private String message;

    public CommonResponse(HttpStatus status, T body, String message) {
        this.status = status;
        this.body = body;
        this.message = message;
    }

    public CommonResponse(HttpStatus status, T body) {
        this(status, body, "");
    }


}
