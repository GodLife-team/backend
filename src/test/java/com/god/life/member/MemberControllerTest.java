package com.god.life.member;

import com.god.life.controller.MemberController;
import com.god.life.dto.member.request.SignupRequest;
import com.god.life.dto.member.response.TokenResponse;
import com.god.life.service.GodLifeScoreService;
import com.god.life.service.ImageService;
import com.god.life.service.ImageUploadService;
import com.god.life.service.MemberService;
import com.god.life.util.JwtUtil;
import com.jayway.jsonpath.JsonPath;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@WebMvcTest(controllers = MemberController.class)
public class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MemberService memberService;

    @SpyBean
    private JwtUtil jwtUtil;

    @MockBean
    private ImageService imageService;

    @MockBean
    private ImageUploadService imageUploadService;

    @MockBean
    private GodLifeScoreService godLifeScoreService;


    @BeforeEach
    void init(){
        ReflectionTestUtils.setField(
                jwtUtil,
                "secret",
                "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        );
        ReflectionTestUtils.setField(
                jwtUtil,
                "ISSUER",
                "tester:8080"
        );
        ReflectionTestUtils.setField(
                jwtUtil,
                "SUBJECT",
                "godlife"
        );
        ReflectionTestUtils.setField(
                jwtUtil,
                "ACCESS_EXPIRATION_TIME",
                7_200_000
        );
        ReflectionTestUtils.setField(
                jwtUtil,
                "REFRESH_EXPIRATION_TIME",
                1_209_600_000
        );
        jwtUtil.init();
    }

    @Test
    @WithUserDetails
    @DisplayName("회원가입으로 반환되는 액세스 토큰의 유효기간은 2시간, 리프레시 토큰의 유효 기간은 2주이다.")
    void 회원_생성_테스트() throws Exception {
        //given : 회원 정보
        String memberInformation = """
                {
                    "nickname" : "tester",
                    "email" : "test@gmail.com",
                    "providerId" : "1234",
                    "providerName" : "kakao",
                    "age" : 20,
                    "sex" : "Female"
                }
                """;

        TokenResponse response = jwtUtil.createToken("1", "tester");
        BDDMockito.given(memberService.signUp(any(SignupRequest.class)))
                .willReturn(response);

        // when 회원가입 요청
        Date creatDate = new Date();
        MvcResult result = mockMvc.perform(post("/signup")
                .with(csrf()) // default Spring configuration이   적용돼 csrf 가 켜져있는 상태임
                .content(memberInformation)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // then : 결과값으로 반환되는 액세스 토큰, 리프레시 토큰 기간 비교
        String responseBody = result.getResponse().getContentAsString();
        String accessToken = JsonPath.parse(responseBody).read("$.body.accessToken");
        String refreshToken = JsonPath.parse(responseBody).read("$.body.refreshToken");
        Date accessDate = jwtUtil.getExpirationTime(accessToken);
        Date refreshDate = jwtUtil.getExpirationTime(refreshToken);
        LocalDate create = creatDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate refresh = refreshDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        long dayDiffRefresh = ChronoUnit.DAYS.between(create, refresh);
        Assertions.assertThat(dayDiffRefresh).isBetween(13L,14L);

        LocalTime createHour = creatDate.toInstant().atZone(ZoneId.systemDefault()).toLocalTime();
        LocalTime accessHour = accessDate.toInstant().atZone(ZoneId.systemDefault()).toLocalTime();
        long hourDiffAccess = ChronoUnit.HOURS.between(createHour, accessHour);
        Assertions.assertThat(hourDiffAccess).isBetween(1L, 2L); //오차로 인한 2시간보다 조금 더 될 수도 있음
    }

    @Test
    @WithUserDetails
    void 기존_가입_확인() throws Exception {
        //given : providerId = 1234인 유저(ID = 1, nickname = tester"가 현재 가입되어 있다고 가정
        String providerId = "1234";
        BDDMockito.given(memberService.checkAlreadySignup(providerId)).willReturn(true);
        TokenResponse response = jwtUtil.createToken("1", "tester");
        BDDMockito.given(memberService.reissueToken(providerId)).willReturn(response);

        //when : /check/id 호출
        Date creatDate = new Date();
        MvcResult result = mockMvc.perform(get("/check/id")
                        .with(csrf()).param("memberId", "1234"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();


        // then : 결과값으로 반환되는 액세스 토큰, 리프레시 토큰 기간 비교
        String responseBody = result.getResponse().getContentAsString();
        String accessToken = JsonPath.parse(responseBody).read("$.body.accessToken");
        String refreshToken = JsonPath.parse(responseBody).read("$.body.refreshToken");
        Date accessDate = jwtUtil.getExpirationTime(accessToken);
        Date refreshDate = jwtUtil.getExpirationTime(refreshToken);
        LocalDate create = creatDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate refresh = refreshDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        System.out.println("엑세스 토큰 만료 시간 : " + accessDate);
        System.out.println("리프레스 토큰 만료 시간 : " + refreshDate);
        long dayDiffRefresh = ChronoUnit.DAYS.between(create, refresh);
        Assertions.assertThat(dayDiffRefresh).isBetween(13L,14L); //오차로 인해 13~14일 사이

        LocalTime createHour = creatDate.toInstant().atZone(ZoneId.systemDefault()).toLocalTime();
        LocalTime accessHour = accessDate.toInstant().atZone(ZoneId.systemDefault()).toLocalTime();
        long hourDiffAccess = ChronoUnit.HOURS.between(createHour, accessHour);
        Assertions.assertThat(hourDiffAccess).isBetween(1L, 2L); //오차로 인한 2시간보다 조금 더 될 수도 있음
    }


}
