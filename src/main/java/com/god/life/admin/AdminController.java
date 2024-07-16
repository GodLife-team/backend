package com.god.life.admin;

import com.god.life.domain.Member;
import com.god.life.dto.board.response.BoardResponse;
import com.god.life.dto.board.response.GodLifeStimulationBoardBriefResponse;
import com.god.life.dto.report.request.ReportRequest;
import com.god.life.dto.board.request.StimulationBoardSearchCondition;
import com.god.life.service.BoardService;
import com.god.life.service.RedisService;
import com.god.life.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final ReportService reportService;
    private final BoardService boardService;
    private final StimulationBoardSearchCondition EMPTY_CONDITION = new StimulationBoardSearchCondition();

    private final RedisService redisService;
    private final String RECOMMEND_BOARD_KEY = "board";
    private final String RECOMMEND_AUTHOR_KEY = "author";

    private final Member adminMember = Member.builder().id(-1L).build();

    @GetMapping("/main")
    public String mainPageView(
            @RequestParam(name = "category", defaultValue = "게시판", required = false) String category,
            Model model) {
        String type = category.equals("게시판") ? "board" : "comment";
        List<ReportRequest> list = reportService.getReports(type);
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
        log.info("삭제 처리 함");
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
                boardService.getListStimulusBoardUsingSearchCondition(EMPTY_CONDITION);

        List<String> list = redisService.getList(RECOMMEND_BOARD_KEY);
        List<GodLifeStimulationBoardBriefResponse> recommendList = new ArrayList<>();
        for (int i = 0; i < dto.size(); i++) {
            for (int j = 0; j < list.size(); j++) {
                if (dto.get(i).getBoardId().equals(Long.valueOf(list.get(j)))) {
                    recommendList.add(dto.get(i));
                    dto.get(i).setView(-1);
                    break;
                }
            }
        }

        model.addAttribute("recommend", recommendList);
        model.addAttribute("boards", dto);
        return "boardList";
    }

    //추천 작가 선정
    @GetMapping("/recommend/author")
    public String selectAuthor(Model model) {
        List<GodLifeStimulationBoardBriefResponse> dto
                = boardService.getListStimulusBoardUsingSearchCondition(EMPTY_CONDITION);

        // Nickname에 따라 그루핑
        Map<String, List<GodLifeStimulationBoardBriefResponse>> m =
                dto.stream().collect(Collectors.groupingBy(GodLifeStimulationBoardBriefResponse::getNickname));

        String recommendAuthorNickname = redisService.getValues(RECOMMEND_AUTHOR_KEY);
        if (!recommendAuthorNickname.equals(RedisService.NO_VALUE)) {
            Set<String> keys = m.keySet();
            for (String key : keys) {
                if (key.equals(recommendAuthorNickname)) {
                    model.addAttribute("recommendAuthor", m.get(recommendAuthorNickname));
                    m = dto.stream().filter(d -> !d.getNickname().equals(recommendAuthorNickname))
                            .collect(Collectors.groupingBy(GodLifeStimulationBoardBriefResponse::getNickname));
                }
            }
        }

        model.addAttribute("map", m);
        return "author";
    }

    // boardId에 해당하는 게시물을 추천 갓생 자극 게시물로 선정
    @PostMapping("/recommend/board/{boardId}")
    public String selectRecommendBoard(@PathVariable("boardId") String id) {
        redisService.addValueToRightOnList(RECOMMEND_BOARD_KEY, id);

        return "redirect:/admin/recommend/board";
    }

    // memberId에 해당하는 회원을 추천 작가로 선정
    @PostMapping("/recommend/author/{nickname}")
    public String selectRecommendAuthor(@PathVariable("nickname") String nickname) {
        redisService.setValue(RECOMMEND_AUTHOR_KEY, nickname);
        return "redirect:/admin/recommend/author";
    }

    //추천 작가 취소
    @PostMapping("/recommend/author")
    public String deleteRecommendAuthor() {
        redisService.deleteValue(RECOMMEND_AUTHOR_KEY);
        return "redirect:/admin/recommend/author";
    }

    @PostMapping("/recommend/board")
    public String deleteRecommendBoard(@ModelAttribute(name = "id") String removeId){
        redisService.deleteValueInList(RECOMMEND_BOARD_KEY, removeId);
        return "redirect:/admin/recommend/board";
    }

    @GetMapping("/board/{boardId}")
    public String detailReportedBoardView(@PathVariable(name = "boardId") Long boardId,
                                          Model model) {

        BoardResponse boardResponse = boardService.detailBoard(boardId, adminMember);
        model.addAttribute("boardResponse", boardResponse);
        return "boardDetail";
    }


    @ExceptionHandler(value = Exception.class)
    public void ex(Exception exception) {
        log.info("", exception);
    }

}
