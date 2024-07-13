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
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;

    @Transactional
    public boolean createReport(ReportRequest request) {
        Report report = request.toReport();
        reportRepository.save(report);
        return true;
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
