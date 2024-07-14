package com.god.life.service;

import com.god.life.domain.Report;
import com.god.life.dto.ReportRequest;
import com.god.life.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final BoardService boardService;
    private final CommentService commentService;

    @Transactional
    public boolean createReport(ReportRequest request) {
        Report report = request.toReport();
        reportRepository.save(report);
        return true;
    }

    @Transactional(readOnly = true)
    public List<ReportRequest> getReports(String category) {
        String type = category.equals("게시판") ? "board" : "comment";
        List<Report> byReportType = reportRepository.findByReportTypeAndComplete(type, false);
        return byReportType.stream().map(ReportRequest::of).toList();
    }

    @Transactional
    public void deleteBoard(Long id) {
        boardService.deleteBoard(id);
        reportRepository.updateComplete("board", id);
    }

    @Transactional
    public void deleteComment(Long id) {
        commentService.deleteComment(id, null);
        reportRepository.updateComplete("comment", id);
    }

    public List<ReportRequest> sampleReport(String category) {
        String c = category.equals("게시판") ? "게시판" : "댓글";
        List<ReportRequest> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            list.add(new ReportRequest("A" + i, (long) i, "B" + i,
                    (long) (i + 10), "신고사유!!", c + "신고 내용" + i, (long) i
                    , LocalDateTime.now(), "b"));
        }
        list.add(new ReportRequest("A" + 0, (long) 0, "B" + 0,
                (long) (10), "신고사유!1234!", c + "신고 내용" + 10, (long) 1
                , LocalDateTime.now(), "b"));
        return list;
    }
}
