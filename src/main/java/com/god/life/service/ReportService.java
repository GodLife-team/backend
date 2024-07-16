package com.god.life.service;

import com.god.life.domain.Report;
import com.god.life.dto.report.request.ReportRequest;
import com.god.life.repository.CommentRepository;
import com.god.life.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final BoardService boardService;
    private final CommentRepository commentRepository;

    /**
     * 신고 기록을 저장합니다.
     * @param request : 신고 기록
     * @return
     */
    @Transactional
    public boolean createReport(ReportRequest request) {
        Report report = request.toReport();
        reportRepository.save(report);
        return true;
    }

    /**
     * 아직 처리되지 않은 신고 기록을 가져옵니다.
     * @param category 신고 카테고리 종류
     * @return 카테고리에 맞는 신고 요청 DTO 반환
     */
    @Transactional(readOnly = true)
    public List<ReportRequest> getReports(String category) {
        List<Report> byReportType = reportRepository.findByReportTypeAndComplete(category, false);
        return byReportType.stream().map(ReportRequest::of).toList();
    }

    /**
     * 신고할 게시글 삭제 처리
     * @param boardId 삭제할 댓글 정보
     */
    @Transactional
    public void deleteBoard(Long boardId) {
        boardService.deleteBoard(boardId);
        reportRepository.updateComplete("board", boardId);
    }

    /**
     * 신고된 댓글 삭제 처리
     * @param commentId 삭제할 댓글 번호
     */
    @Transactional
    public void deleteComment(Long commentId) {
        commentRepository.deleteById(commentId);
        reportRepository.updateComplete("comment", commentId);
    }

}
