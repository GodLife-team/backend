package com.god.life.board;


import com.god.life.domain.*;
import com.god.life.dto.ImageSaveResponse;
import com.god.life.exception.ForbiddenException;
import com.god.life.mockuser.MockUserCustom;
import com.god.life.repository.BoardRepository;
import com.god.life.repository.ImageRepository;
import com.god.life.repository.MemberRepository;
import com.god.life.service.ImageUploadService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class BoardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ImageUploadService mockImageUploadService;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private ImageRepository imageRepository;


    @BeforeAll
    public void init(){
        Member member = Member.builder()
                .email("TEST@naver.com")
                .age(10)
                .sex(Sex.MALE)
                .providerId("1234")
                .providerName(ProviderType.KAKAO)
                .nickname("TESTER")
                .build();
        memberRepository.save(member);
    }

    @BeforeEach
    public void setUp() {
        // mvc
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    @MockUserCustom
    @DisplayName("게시판 생성 테스트 (이미지는 한장)")
    @Transactional
    void createBoardSingleImage() throws Exception {
        //Given
        MockMultipartFile sendImage = createFile("images", "image.jpg", "jpg");

        BDDMockito.given(mockImageUploadService.uploads(anyList()))
                .willReturn(List.of(new ImageSaveResponse("image.jpg", "1234")));

        //when
        ResultActions perform = mockMvc.perform(multipart("/board")
                .file(sendImage)
                .param("title", "TEST TITLE")
                .param("content", "TEST CONTENT"));

        // then
        perform.andExpect(status().isOk());
        perform.andDo(print());
        List<Board> boards = boardRepository.findAll();
        List<Image> images = imageRepository.findAll();
        Assertions.assertThat(boards.size()).isEqualTo(1);
        Assertions.assertThat(images.size()).isEqualTo(1);
    }

    @Test
    @MockUserCustom
    @DisplayName("게시판 생성 테스트 (이미지는 두장)")
    @Transactional
    void createBoardWithTwoImages() throws Exception {

        //Given
        MockMultipartFile sendImage1 = createFile("images", "image.jpg", "jpg");
        MockMultipartFile sendImage2 = createFile("images2", "image2.png", "png");
        BDDMockito.given(mockImageUploadService.uploads(any()))
                .willReturn(List.of(new ImageSaveResponse("image.jpg", "TEST1"),
                        new ImageSaveResponse("image2.jpg", "TEST2")));

        //when
        ResultActions perform = mockMvc.perform(multipart("/board")
                .file(sendImage1).file(sendImage2)
                .param("title", "TEST TITLE")
                .param("content", "TEST CONTENT"));

        // then
        perform.andExpect(status().isOk());
        perform.andDo(print());
        List<Board> boards = boardRepository.findAll();
        List<Image> images = imageRepository.findAll();
        Assertions.assertThat(boards.size()).isEqualTo(1);
        Assertions.assertThat(images.size()).isEqualTo(2);
    }

    @Test
    @MockUserCustom
    @DisplayName("잘못된 파일 업로드")
    public void impossible_upload_to_board_with_invalid_file_type() throws Exception {
        //Given
        MockMultipartFile wrongFile = createFile("images", "video.mp4", "video");

        // 잘 못된 파일 업로드 --> 확장자 추출시 forbiddenException
        BDDMockito.given(mockImageUploadService.uploads(any())).willThrow(
                new ForbiddenException("업로드할 수 없는 확장자입니다.")
        );

        //when
        ResultActions perform = mockMvc.perform(multipart("/board")
                .file(wrongFile)
                .param("title", "TEST TITLE")
                .param("content", "TEST CONTENT"));

        perform.andDo(print())
                .andExpect(result -> org.junit.jupiter.api.Assertions.assertTrue(result.getResolvedException()
                        instanceof ForbiddenException))
                .andExpect(jsonPath("status").value(HttpStatus.FORBIDDEN.getReasonPhrase().toUpperCase()))
                .andExpect(jsonPath("message").value("업로드할 수 없는 확장자입니다."));
    }

    @Test
    @DisplayName("회원이 아니면 게시판 업로드 불가능")
    public void impossible_upload_to_board_with_no_login_member() throws Exception {
        //Given
        MockMultipartFile sendImage = createFile("images", "image.jpg", "jpg");
        BDDMockito.given(mockImageUploadService.uploads(anyList()))
                .willReturn(List.of(new ImageSaveResponse("image.jpg", "1234")));

        //when
        ResultActions perform = mockMvc.perform(multipart("/board")
                .file(sendImage)
                .param("title", "TEST TITLE")
                .param("content", "TEST CONTENT"));

        perform.andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("status").value(HttpStatus.UNAUTHORIZED.getReasonPhrase().toUpperCase()))
                .andExpect(jsonPath("message").value("로그인하지 않았습니다."));
    }


    @Test
    @DisplayName("관리자가 아니면 접근 불가")
    @WithMockUser
    public void no_ADMIN() throws Exception {
        //when
        ResultActions perform = mockMvc.perform(multipart("/admin")
                .param("title", "TEST TITLE")
                .param("content", "TEST CONTENT"));

        perform.andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("status").value(HttpStatus.FORBIDDEN.getReasonPhrase().toUpperCase()))
                .andExpect(jsonPath("message").value("해당 API 호출 불가능합니다."));
    }

    private MockMultipartFile createFile(String name, String originalName, String contentType) {
        return new MockMultipartFile(
                name,
                originalName,
                contentType,
                "ASDF".getBytes(StandardCharsets.UTF_8)
        );
    }

}
