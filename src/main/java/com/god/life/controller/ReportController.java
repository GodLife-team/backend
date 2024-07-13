package com.god.life.controller;


import com.god.life.dto.ReportRequest;
import com.god.life.dto.common.CommonResponse;
import com.god.life.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ReportController {

    private ReportService reportService;

    @PostMapping("/report")
    public ResponseEntity<CommonResponse<Boolean>> postReport(@RequestBody ReportRequest request) {
        return ResponseEntity.ok(new CommonResponse<>(HttpStatus.OK, reportService.createReport(request)));
    }

}
