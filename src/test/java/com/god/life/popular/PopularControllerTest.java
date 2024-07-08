package com.god.life.popular;


import com.god.life.controller.PopularController;
import com.god.life.dto.GodLifeStimulationBoardBriefResponse;
import com.god.life.dto.PopularMemberResponse;
import com.god.life.mockuser.MockUserCustom;
import com.god.life.service.BoardService;
import com.god.life.service.MemberService;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;

@WebMvcTest(PopularController.class)
public class PopularControllerTest
{
    @MockBean
    private MemberService memberService;

    @MockBean
    private BoardService boardService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @MockUserCustom
    void 한주간_인기있는_멤버_10명_조회_테스트_() throws Exception {
        //given
        List<PopularMemberResponse> responses = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            responses.add(new PopularMemberResponse((i + 1L), "nickname" + i, (i + 1) * 2, "HI IM TESTER " + i, "profile" + i, "background" + i));
        }
        responses.sort(Comparator.comparing(PopularMemberResponse::getGodLifeScore).reversed());
        BDDMockito.given(memberService.searchWeeklyPopularMember()).willReturn(
                responses
        );

        //when
        ResultActions result = mockMvc.perform(get("/popular/members/weekly"));

        //then
        result.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.body[0].godLifeScore").value(20));

        for (int i = 0; i < 10; i++) {
            result.andExpect(MockMvcResultMatchers.jsonPath("$.body[" + i + "].godLifeScore").value(20 - (i * 2)));
        }
    }

    @Test
    @WithMockUser
    void 전체기간_인기있는_멤버_10명_테스트() throws Exception {
        //given
        List<PopularMemberResponse> responses = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            responses.add(new PopularMemberResponse((i + 1L), "nickname" + i, (i + 1) * 2, "HI IM TESTER " + i, "profile" + i, "background" + i));
        }
        responses.sort(Comparator.comparing(PopularMemberResponse::getGodLifeScore).reversed());
        BDDMockito.given(memberService.searchAllTimePopularMember()).willReturn(
                responses
        );

        //when
        ResultActions result = mockMvc.perform(get("/popular/members/all-time"));

        //then
        result.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.body[0].godLifeScore").value(20));

        for (int i = 0; i < 10; i++) {
            result.andExpect(MockMvcResultMatchers.jsonPath("$.body[" + i + "].godLifeScore").value(20 - (i * 2)));
            result.andExpect(MockMvcResultMatchers.jsonPath("$.body[" + i + "].backgroundUrl").value("background" + (9-i)));
            result.andExpect(MockMvcResultMatchers.jsonPath("$.body[" + i + "].profileURL").value("profile" + (9-i)));
        }
    }

    @Test
    @WithMockUser
    void 한주간_인기있는_멤버_0명_테스트() throws Exception {
        //given
        List<PopularMemberResponse> responses = new ArrayList<>();
        BDDMockito.given(memberService.searchWeeklyPopularMember()).willReturn(
                responses
        );

        //when
        ResultActions result = mockMvc.perform(get("/popular/members/weekly"));

        //then
        result.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.body").isArray())
                .andExpect(MockMvcResultMatchers.jsonPath("$.body").isEmpty());
    }

    @Test
    @WithMockUser
    void 전체기간_인기있는_멤버_0명_테스트() throws Exception {
        //given
        List<PopularMemberResponse> responses = new ArrayList<>();
        BDDMockito.given(memberService.searchAllTimePopularMember()).willReturn(
                responses
        );

        //when
        ResultActions result = mockMvc.perform(get("/popular/members/all-time"));

        //then
        result.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.body").isArray())
                .andExpect(MockMvcResultMatchers.jsonPath("$.body").isEmpty());
    }

    @Test
    @WithMockUser
    void 전체기간_갓생_자극_페이지_API_조회() throws Exception {
        //given
        List<GodLifeStimulationBoardBriefResponse> responses = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            responses.add(new GodLifeStimulationBoardBriefResponse("title" + i, Integer.toUnsignedLong(i), "thumb" + i, "intro" + i, "nick" + i,
                    i * 10, 0));
        }
        responses.sort(Comparator.comparing(GodLifeStimulationBoardBriefResponse::getGodLifeScore).reversed());
        BDDMockito.given(boardService.getAllTimePopularStimulusBoardList()).willReturn(responses);

        //when
        ResultActions result = mockMvc.perform(get("/popular/stimulus/boards/all-time"));


        //then
        result.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.body").isArray())
                .andExpect(MockMvcResultMatchers.jsonPath("$.body.length()").value(5));

        for (int i = 0; i < 5; i++) {
            result.andExpect(MockMvcResultMatchers.jsonPath("$.body[" + i + "].godLifeScore").value(50 - (i * 10)));
            result.andExpect(MockMvcResultMatchers.jsonPath("$.body[" + i + "].view").value(0));
        }
    }

    @Test
    @WithMockUser
    void 전체기간_갓생_자극_페이지_API_조회_조회결과가_없는_경우() throws Exception {
        //given
        List<GodLifeStimulationBoardBriefResponse> responses = new ArrayList<>();
        BDDMockito.given(boardService.getAllTimePopularStimulusBoardList()).willReturn(responses);

        //when
        ResultActions result = mockMvc.perform(get("/popular/stimulus/boards/all-time"));


        //then
        result.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.body").isArray())
                .andExpect(MockMvcResultMatchers.jsonPath("$.body.length()").value(0));
    }

    @Test
    @WithMockUser
    void 전체기간_갓생_자극_페이지_조회수가_많음() throws Exception {
        //given
        List<GodLifeStimulationBoardBriefResponse> responses = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            responses.add(new GodLifeStimulationBoardBriefResponse("title" + i, Integer.toUnsignedLong(i), "thumb" + i, "intro" + i, "nick" + i,
                    0, i * 10));
        }
        responses.sort(Comparator.comparing(GodLifeStimulationBoardBriefResponse::getView).reversed());
        BDDMockito.given(boardService.getMostViewedStimulusBoardList()).willReturn(responses);

        //when
        ResultActions result = mockMvc.perform(get("/popular/stimulus/boards/view"));


        //then
        result.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.body").isArray())
                .andExpect(MockMvcResultMatchers.jsonPath("$.body.length()").value(5));

        for (int i = 0; i < 5; i++) {
            result.andExpect(MockMvcResultMatchers.jsonPath("$.body[" + i + "].view").value(50 - (i * 10)));
            result.andExpect(MockMvcResultMatchers.jsonPath("$.body[" + i + "].godLifeScore").value(0));
        }
    }

}
