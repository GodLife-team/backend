package com.god.life.controller;


import com.god.life.dto.ReportRequest;
import com.god.life.dto.common.CommonResponse;
import com.god.life.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "게시글/댓글 신고", description = "게시글/댓글 신고할 때 사용하는 API")
public class ReportController {

    private final ReportService reportService;

    /**
     * 부적절한 댓글/게시물을 신고하는 입니다.
     * @param request 신고 정보
     * @return 정상적으로 신고됐는지 확인
     */
    @Operation(summary = "신고 API")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "신고가 정상적으로 처리됐으면 true(boolean) 반환",
                            useReturnTypeSchema = true)
            }
    )
    @PostMapping("/report")
    public ResponseEntity<CommonResponse<Boolean>> postReport(@RequestBody ReportRequest request) {
        return ResponseEntity.ok(new CommonResponse<>(HttpStatus.OK, reportService.createReport(request)));
    }

}
