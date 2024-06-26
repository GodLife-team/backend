package com.god.life.board;


import com.god.life.config.JpaAuditingConfiguration;
import com.god.life.domain.*;
import com.god.life.dto.BoardSearchResponse;
import com.god.life.dto.GodLifeStimulationBoardResponse;
import com.god.life.dto.PopularMemberResponse;
import com.god.life.repository.*;
import jakarta.persistence.EntityManager;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.fail;

@ActiveProfiles("test")
@DataJpaTest
@TestPropertySource(locations = "classpath:application-test.yaml")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaAuditingConfiguration.class)// 생성시간/수정시간 자동 주입 설정파일 임포트
public class BoardRepositoryTest {

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private GodLifeScoreRepository godLifeScoreRepository;

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private MemberRepository memberRepository;


    @BeforeEach
    void init(){
        Category category1 = new Category(CategoryType.GOD_LIFE_STIMULUS);
        Category category2 = new Category(CategoryType.GOD_LIFE_PAGE);

        categoryRepository.save(category1);
        categoryRepository.save(category2);
    }

    @Test
    @DisplayName("갓생 인정 카테고리 게시판 생성")
    void createGodLifeBoard() {
        //given
        Category category = categoryRepository.findByCategoryType(CategoryType.GOD_LIFE_PAGE);
        Category category1 = categoryRepository.findByCategoryType(CategoryType.GOD_LIFE_STIMULUS);

        Board b0 = Board.builder()
                .title("asdf")
                .content("asdf")
                .member(null)
                .thumbnailUrl("")
                .category(category)
                .build();

        Board b1 = Board.builder()
                .title("asdf")
                .content("asdf")
                .member(null)
                .thumbnailUrl("")
                .category(category1)
                .build();

        Board b2 = Board.builder()
                .title("asdf")
                .content("asdf")
                .member(null)
                .thumbnailUrl("")
                .category(category)
                .build();

        boardRepository.save(b0);
        boardRepository.save(b1);
        boardRepository.save(b2);

        // when
        List<Board> boards = boardRepository.getBoardsByCategory(category.getCategoryType());

        // then
        Assertions.assertThat(boards.size()).isEqualTo(2);
    }

    @Test
    @DisplayName("갓생 자극 카테고리 게시판 생성")
    void createGodLifeStimulusBoard() {
        //given
        Category page = categoryRepository.findByCategoryType(CategoryType.GOD_LIFE_PAGE);
        Category stimulus = categoryRepository.findByCategoryType(CategoryType.GOD_LIFE_STIMULUS);
        em.flush();

        Board b0 = Board.builder()
                .title("asdf")
                .content("asdf")
                .member(null)
                .thumbnailUrl("")
                .category(page)
                .build();

        Board b1 = Board.builder()
                .title("asdf")
                .content("asdf")
                .member(null)
                .thumbnailUrl("")
                .category(stimulus)
                .build();

        boardRepository.save(b0);
        boardRepository.save(b1);
        em.flush();

        // when
        List<Board> boards = boardRepository.getBoardsByCategory(stimulus.getCategoryType());

        // then
        Assertions.assertThat(boards.size()).isEqualTo(1);
    }


    @Test
    void 한_주간_인기_게시판_테스트(){
        Category godPageCategory = categoryRepository.findByCategoryType(CategoryType.GOD_LIFE_PAGE);
        Category godStimulusCategory = categoryRepository.findByCategoryType(CategoryType.GOD_LIFE_STIMULUS);

        Member member = createMember("1", "1");
        Member member1 = createMember("2", "2");
        Member member2 = createMember("3", "3");
        Member member3 = createMember("4", "4");

        //회원1 -> 게시글 3개 작성
        Board boardMember1_1 = createBoard(member, godPageCategory);
        Board boardMember1_2 = createBoard(member, godPageCategory);
        Board boardMember1_3 = createBoard(member, godPageCategory);

        //회원2 -> 게시글 2개 작성
        Board boardMember2_1 = createBoard(member1, godPageCategory);
        Board boardMember2_2 = createBoard(member1, godPageCategory);

        //회원3 -> 게시글 1개 작성
        Board boardMember3_1 = createBoard(member2, godStimulusCategory);

        //회원4 --> 게시글 2개 작성
        Board boardMember4_1 = createBoard(member3, godPageCategory);
        Board boardMember4_2 = createBoard(member3, godStimulusCategory);

        // boardMember1_1에 따봉 3개
        GodLifeScore like = createLike(member, boardMember1_1);
        GodLifeScore like1 = createLike(member1, boardMember1_1);
        GodLifeScore like2 = createLike(member2, boardMember1_1);

        // boardMember1_2에 따봉 2개
        GodLifeScore like3 = createLike(member1, boardMember1_2);
        GodLifeScore like4 = createLike(member2, boardMember1_2);

        // boardMember2_1에 따봉 1개
        GodLifeScore like5 = createLike(member, boardMember2_1);

        // boardMember3_1에 따봉 2개
        GodLifeScore like6 = createLike(member, boardMember3_1);
        GodLifeScore like7 = createLike(member1, boardMember3_1);

        em.flush();
        em.clear();

        // 최종적으로
        // 회원 1 -> 따봉 5개 ==> 10점,
        // 회원 3 -> 따봉 2개 ==> 4점,
        // 회원 2 -> 따봉 1개 받아야함 ==> 2점,
        List<BoardSearchResponse> weeklyPopularBoard = boardRepository.findWeeklyPopularBoard();
        System.out.println(weeklyPopularBoard.toString());

        Assertions.assertThat(weeklyPopularBoard.size()).isEqualTo(3);
        Assertions.assertThat(weeklyPopularBoard.get(0).getGodScore()).isEqualTo(6); // 10점
        Assertions.assertThat(weeklyPopularBoard.get(1).getGodScore()).isEqualTo(4); // 4점
        Assertions.assertThat(weeklyPopularBoard.get(2).getGodScore()).isEqualTo(2);; // 2점

    }

    private Board createBoard(Member member, Category category){
        Board board = Board
                .builder()
                .title("test")
                .content("test1")
                .member(member)
                .view(0)
                .totalScore(0)
                .category(category)
                .build();
        boardRepository.save(board);
        return board;
    }

    private Member createMember(String providerId, String nickname) {
        Member member = Member
                .builder()
                .sex(Sex.MALE)
                .providerName(ProviderType.KAKAO)
                .age(18)
                .nickname(nickname)
                .email("ASDF@example.com")
                .providerId(providerId)
                .whoAmI("ASDF").build();

        memberRepository.save(member);
        return member;
    }

    @Test
    void 갓생_자극_게시판_세부_내용_조회_테스트(){
        //given
        Category stimulus = categoryRepository.findByCategoryType(CategoryType.GOD_LIFE_STIMULUS);
        Category page = categoryRepository.findByCategoryType(CategoryType.GOD_LIFE_PAGE);
        Member member1 = createMember("1234", "aaaa");
        Member member2 = createMember("5678", "bbbb");

        Board stimulusBoard = createBoard(member1, stimulus);
        Board pageBoard = createBoard(member2, page);

        createLike(member1, stimulusBoard);
        createLike(member2, stimulusBoard);

        // when : stimulusboard 조회
        GodLifeStimulationBoardResponse findBoard = null;
        try {
            findBoard = boardRepository.findStimulusBoardEqualsBoardId(stimulusBoard.getId(), member1);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }

        Assertions.assertThat(findBoard.getBoardId()).isEqualTo(stimulusBoard.getId());
        Assertions.assertThat(findBoard.getNickname()).isEqualTo(member1.getNickname());
        Assertions.assertThat(findBoard.getGodLifeScore()).isEqualTo(4);
        Assertions.assertThat(findBoard.getWriterId()).isEqualTo(member1.getId());
        System.out.println(findBoard);
    }

    @Test
    void 갓생_자극_리스트_조회() throws InterruptedException {
        //given
        Category stimulus = categoryRepository.findByCategoryType(CategoryType.GOD_LIFE_STIMULUS);
        Category page = categoryRepository.findByCategoryType(CategoryType.GOD_LIFE_PAGE);
        Member member1 = createMember("1234", "aaaa");
        Member member2 = createMember("5678", "bbbb");

        Board stimulusBoard1 = createBoard(member1, stimulus);
        Thread.sleep(5000); //시간차 저장
        Board stimulusBoard2 = createBoard(member2, stimulus);
        Board pageBoard = createBoard(member1, page);

        createLike(member1, stimulusBoard1);
        createLike(member2, stimulusBoard1);

        em.flush();
        em.clear();

        List<Board> boards1 = boardRepository.findAll();
        List<GodLifeStimulationBoardResponse> boards =
                boardRepository.findStimulusBoardPaging(PageRequest.of(0, 10, Sort.by("create_date")))
                        .getContent();

        Assertions.assertThat(boards.size()).isEqualTo(2);
        Assertions.assertThat(boards.get(0).getBoardId()).isEqualTo(stimulusBoard2.getId());
        Assertions.assertThat(boards.get(0).getGodLifeScore()).isEqualTo(0);
        Assertions.assertThat(boards.get(1).getBoardId()).isEqualTo(stimulusBoard1.getId());
        Assertions.assertThat(boards.get(1).getGodLifeScore()).isEqualTo(4);
        System.out.println(boards.get(0));
        System.out.println(boards.get(1));
    }

    private GodLifeScore createLike(Member member, Board board) {
        GodLifeScore god = GodLifeScore.builder()
                .member(member)
                .board(board)
                .score(2)
                .build();

        GodLifeScore save = godLifeScoreRepository.save(god);
        return save;
    }

    private Image createImage(Member member, String serverName) {
        Image image = Image.builder()
                .serverName(serverName)
                .member(member)
                .build();
        return imageRepository.save(image);
    }



}
