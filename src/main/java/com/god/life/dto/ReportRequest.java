package com.god.life.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.god.life.domain.Report;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 신고시 전달되는 DTO
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ReportRequest {

    private String reporterNickname;

    private Long reporterId;

    private String receivedNickname;

    private Long receivedId;

    private String reason;

    private String reportContent;

    private Long reportId;

    private String reportType;


    public Report toReport(){
        return Report.builder()
                .reporterNickname(reporterNickname)
                .reporterId(reporterId)
                .receivedId(receivedId)
                .receivedNickname(receivedNickname)
                .reason(reason)
                .reportContent(reportContent)
                .reportId(reportId)
                .reportType(reportType).build();
    }

    public static ReportRequest of(Report report) {
        return new ReportRequest(
                report.getReporterNickname(),
                report.getReporterId(),
                report.getReceivedNickname(),
                report.getReceivedId(),
                report.getReason(),
                report.getReportContent(),
                report.getReportId(),
                report.getReportTime(),
                report.getReportType()
        );
    }
}
