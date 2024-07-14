package com.god.life.admin;

import com.god.life.dto.GodLifeStimulationBoardBriefResponse;
import com.god.life.dto.ReportRequest;
import com.god.life.dto.StimulationBoardSearchCondition;
import com.god.life.service.BoardService;
import com.god.life.service.MemberService;
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
    private final BoardService boardService;
    private final MemberService memberService;
    private final StimulationBoardSearchCondition emptyCondition = new StimulationBoardSearchCondition();

    @GetMapping("/main")
    public String mainPageView(
            @RequestParam(name = "category", defaultValue = "게시판", required = false) String category,
            Model model) {
        List<ReportRequest> list = reportService.getReports(category);
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

        if (category.equals("게시판")) {
            reportService.deleteBoard(id);
        } else if (category.equals("댓글")) {
            reportService.deleteComment(id);
        }

        redirectAttributes.addAttribute("category", category);
        return "redirect:/admin/main";
    }

    //추천 갓생 자극 게시물 선정
    @GetMapping("/recommend/board")
    public String selectBoardView(@RequestParam(value = "order", required = false) String orderType, Model model) {
        List<GodLifeStimulationBoardBriefResponse> dto =
                boardService.getListStimulusBoardUsingSearchCondition(emptyCondition);

        model.addAttribute("boards", dto);
        return "board";
    }

    //추천 작가 선정
    @GetMapping("/recommend/author")
    public String selectAuthor(Model model){
        List<GodLifeStimulationBoardBriefResponse> dto
                = boardService.getListStimulusBoardUsingSearchCondition(emptyCondition);

        Map<String, List<GodLifeStimulationBoardBriefResponse>> m =
                dto.stream().collect(Collectors.groupingBy(GodLifeStimulationBoardBriefResponse::getNickname));

        model.addAttribute("map", m);

        return "author";
    }

}
