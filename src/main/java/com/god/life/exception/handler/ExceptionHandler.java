package com.god.life.exception.handler;

import com.god.life.dto.common.CommonResponse;
import com.god.life.exception.ForbiddenException;
import com.god.life.exception.JwtInvalidException;
import com.god.life.exception.NotFoundResource;
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
    public ResponseEntity<CommonResponse<String>> jwtInvalidException(JwtInvalidException ex) {
        return ResponseEntity.badRequest()
                .body(new CommonResponse<>(HttpStatus.BAD_REQUEST, "", ex.getMessage()));
    }

    // 존재하지 않는 Resource(게시판 등) 조회
    @org.springframework.web.bind.annotation.ExceptionHandler(NotFoundResource.class)
    public ResponseEntity<CommonResponse<String>> notFoundException(NotFoundResource ex) {
        return ResponseEntity.badRequest()
                .body(new CommonResponse<>(HttpStatus.BAD_REQUEST, "", ex.getMessage()));
    }

    // 권한이 없는 리소스 삭제 시도
    @org.springframework.web.bind.annotation.ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<CommonResponse<String>> forbiddenException(ForbiddenException ex) {
        log.info("Forbidden Exception!!");
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new CommonResponse<>(HttpStatus.FORBIDDEN, "", ex.getMessage()));
    }

}
