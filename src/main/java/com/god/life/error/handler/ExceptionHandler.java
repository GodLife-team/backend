package com.god.life.error.handler;

import com.god.life.dto.common.CommonResponse;
import com.god.life.error.ForbiddenException;
import com.god.life.error.JwtInvalidException;
import com.god.life.error.NotFoundResource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

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
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new CommonResponse<>(HttpStatus.FORBIDDEN, "", ex.getMessage()));
    }

    //파일 업로드 크기 에러
    @org.springframework.web.bind.annotation.ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<CommonResponse<String>> multipartException(MaxUploadSizeExceededException ex) {
        log.error("MaxUploadSizeExceededException", ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new CommonResponse<>(HttpStatus.BAD_REQUEST, "", "크기가 3MB이하 이미지만 업로드할 수 있습니다."));
    }

    // 그 외의 에러
    @org.springframework.web.bind.annotation.ExceptionHandler(Exception.class)
    public ResponseEntity<CommonResponse<String>> defaultException(Exception exception) {
        log.error("exception Full Trace : ", exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new CommonResponse<>(HttpStatus.INTERNAL_SERVER_ERROR, "", "서버 내부 오류입니다."));
    }


}
