package com.god.life.repository;

import com.god.life.domain.Report;
import jakarta.validation.constraints.Min;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {

    // 아직 미삭제된 해당 타입(게시판/댓글) 신고 내역 가져오기
    List<Report> findByReportTypeAndComplete(String type, boolean complete);

    // 삭제 처리
    @Modifying
    @Query("update Report b set b.complete = true where b.reportType = :type and b.reportId = :id")
    void updateComplete(@Param("type") String type, @Param("id") Long id);
}
