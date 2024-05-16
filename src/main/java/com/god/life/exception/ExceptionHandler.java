package com.god.life.exception;

import com.god.life.dto.CommonResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class ExceptionHandler {


    // Valid Exception
    @org.springframework.web.bind.annotation.ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CommonResponse<Map<String, String>>> methodNotValidException(MethodArgumentNotValidException ex) {
        log.info("캐스팅 실패..");
        Map<String, String> errors = parsingError(ex);

        return ResponseEntity.badRequest().body(
                new CommonResponse<>(HttpStatus.BAD_REQUEST, errors, "잘못된 값이 있습니다."));
    }

    private Map<String, String> parsingError(MethodArgumentNotValidException ex) {
        Map<String, String> result = new HashMap<>();
        final BindingResult bindingResult = ex.getBindingResult();
        bindingResult.getAllErrors().forEach(
                error -> result.put(((FieldError) error).getField(), error.getDefaultMessage())
        );

        return result;
    }

    // JWT 에러.
    @org.springframework.web.bind.annotation.ExceptionHandler(JwtInvalidException.class)
    public ResponseEntity<CommonResponse<String>> jwtInvalidException(JwtInvalidException exception) {
        return ResponseEntity.badRequest()
                .body(new CommonResponse<>(HttpStatus.BAD_REQUEST, "", exception.getMessage()));
    }
}
