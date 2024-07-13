package com.god.life.admin;

import com.god.life.dto.ReportRequest;
import com.god.life.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final ReportService reportService;

    @GetMapping("/main")
    public String mainPage(
            @RequestParam(name = "category", defaultValue = "게시판", required = false) String category,
            Model model) {
        List<ReportRequest> list = reportService.sampleReport(category);
        Map<Long, List<ReportRequest>> m = list.stream().collect(Collectors.groupingBy(ReportRequest::getReportId));
        model.addAttribute("category", category);
        model.addAttribute("reports", list);
        model.addAttribute("map", m);

        return "main";
    }

    @PostMapping("/delete/{id}")
    public String postId(
            @RequestParam(name = "category") String category,
            RedirectAttributes redirectAttributes,
            @PathVariable("id") Long id
    ) {
        category = category.replaceAll("\"", "");
        log.info("삭제할 카테고리 {} 번호 = {}", category, id);
        redirectAttributes.addAttribute("category", category);
        return "redirect:/admin/main";
    }


}
