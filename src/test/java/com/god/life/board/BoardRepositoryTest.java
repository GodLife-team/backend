package com.god.life.board;


import com.god.life.config.JpaAuditingConfiguration;
import com.god.life.domain.*;
import com.god.life.dto.board.response.BoardSearchResponse;
import com.god.life.dto.board.response.GodLifeStimulationBoardBriefResponse;
import com.god.life.dto.board.response.GodLifeStimulationBoardResponse;
import com.god.life.dto.board.request.StimulationBoardSearchCondition;
import com.god.life.error.NotFoundResource;
import com.god.life.repository.*;
import jakarta.persistence.EntityManager;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
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
import org.springframework.test.util.ReflectionTestUtils;

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

    private final String IMAGE_NAME = "IMAGE_TEST";

    @BeforeEach
    void init(){
        Category category1 = new Category(CategoryType.GOD_LIFE_STIMULUS);
        Category category2 = new Category(CategoryType.GOD_LIFE_PAGE);

        categoryRepository.save(category1);
        categoryRepository.save(category2);
    }

    @AfterEach
    void afterEach(){
        categoryRepository.deleteAll();
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
        createTestCase();

        em.flush();
        em.clear();

        // 최종적으로
        // 회원 1 -> 따봉 5개 ==> 10점,
        // 회원 3 -> 따봉 2개 ==> 4점,
        // 회원 2 -> 따봉 1개 받아야함 ==> 2점,
        List<BoardSearchResponse> weeklyPopularBoard = boardRepository.findWeeklyPopularBoard();
        for (BoardSearchResponse boardSearchResponse : weeklyPopularBoard) {
            System.out.println(boardSearchResponse);
        }

        Assertions.assertThat(weeklyPopularBoard.size()).isEqualTo(3);
        Assertions.assertThat(weeklyPopularBoard.get(0).getGodScore()).isEqualTo(4); // 10점
        Assertions.assertThat(weeklyPopularBoard.get(1).getGodScore()).isEqualTo(4); // 4점
        Assertions.assertThat(weeklyPopularBoard.get(2).getGodScore()).isEqualTo(2);; // 2점
    }

    private void createTestCase() {
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
        //갓생 자극 게시물은 포인트 부여 X
        Board boardMember3_1 = createBoard(member2, godStimulusCategory);

        //회원4 --> 게시글 2개 작성
        Board boardMember4_1 = createBoard(member3, godPageCategory);
        Board boardMember4_2 = createBoard(member3, godStimulusCategory);

        // boardMember1_1에 따봉 3개
        GodLifeScore like = createLike(member, boardMember1_1);
        GodLifeScore like1 = createLike(member1, boardMember1_1);
        GodLifeScore like2 = createLike(member2, boardMember1_1);
        boardRepository.incrementGodLifeScore(boardMember1_1.getId());
        boardRepository.incrementGodLifeScore(boardMember1_1.getId());
        boardRepository.incrementGodLifeScore(boardMember1_1.getId());

        // boardMember1_2에 따봉 2개
        GodLifeScore like3 = createLike(member1, boardMember1_2);
        GodLifeScore like4 = createLike(member2, boardMember1_2);
        boardRepository.incrementGodLifeScore(boardMember1_2.getId());
        boardRepository.incrementGodLifeScore(boardMember1_2.getId());

        // boardMember2_1에 따봉 1개
        GodLifeScore like5 = createLike(member, boardMember2_1);
        boardRepository.incrementGodLifeScore(boardMember2_1.getId());

        // boardMember3_1에 따봉 2개
        GodLifeScore like6 = createLike(member, boardMember3_1);
        GodLifeScore like7 = createLike(member1, boardMember3_1);
        boardRepository.incrementGodLifeScore(boardMember3_1.getId());
        boardRepository.incrementGodLifeScore(boardMember3_1.getId());
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
        boardRepository.incrementGodLifeScore(stimulusBoard.getId());
        boardRepository.incrementGodLifeScore(stimulusBoard.getId());

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
        Assertions.assertThat(findBoard.getGodLifeScore()-2).isEqualTo(4);
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

        //when
        List<GodLifeStimulationBoardBriefResponse> boards =
                boardRepository.findStimulusBoardPaging(PageRequest.of(0, 10, Sort.by("create_date")))
                        .getContent();

        //then
        Assertions.assertThat(boards.size()).isEqualTo(2);
        Assertions.assertThat(boards.get(0).getBoardId()).isEqualTo(stimulusBoard2.getId());
        Assertions.assertThat(boards.get(1).getBoardId()).isEqualTo(stimulusBoard1.getId());
    }

    @Test
    void 현재_임시_작성중인_갓생_자극_게시물은_상세_조회되면_안된다(){
        //given : boardStatus가 t인 게시판은 조회되면 안됨.
        Category stimulus = categoryRepository.findByCategoryType(CategoryType.GOD_LIFE_STIMULUS);
        Member member1 = createMember("1234", "aaaa");

        Board board = createBoard(member1, stimulus);
        ReflectionTestUtils.setField(
                board,
                "status",
                BoardStatus.T
        );
        boardRepository.save(board);
        em.flush();
        em.clear();

        //when
        //게시물이 조회되지 않는 trhow가 반환되어야 함
        org.junit.jupiter.api.Assertions.assertThrows(NotFoundResource.class, () ->
                boardRepository.findStimulusBoardEqualsBoardId(board.getId(), member1));
    }

    @Test
    void 현재_임시_작성중인_게시물은_갓생자극_리스트에_조회되면안됨(){
        createTestCase();
        Category category = categoryRepository.findByCategoryType(CategoryType.GOD_LIFE_STIMULUS);
        Member member = createMember("213321321", "TESTERER");
        Board board = createBoard(member, category);
        ReflectionTestUtils.setField(
                board,
                "status",
                BoardStatus.T
        );
        boardRepository.save(board);
        em.flush();
        em.clear();

        //when
        List<GodLifeStimulationBoardBriefResponse> boards =
                boardRepository.findStimulusBoardPaging(PageRequest.of(0, 10, Sort.by("create_date")))
                        .getContent();

        //then
        Assertions.assertThat(boards.size()).isEqualTo(2);
    }

    @Test
    void 미완료_게시믈_삭제_테스트() {
        Category category = categoryRepository.findByCategoryType(CategoryType.GOD_LIFE_STIMULUS);
        //given
        Member member = createMember("1234", "aaaa");
        createBoard(member, category); //완료된 게시물

        //현재 작성중인 게시물 (30분 전에 작성하기 시작 ==> 삭제되면 XX)
        LocalDateTime createAt30minute = LocalDateTime.now().minusMinutes(30);
        Board incompleteBoardWrittenWithinOneHour = createBoard(member, category);
        em.createNativeQuery("update Board b set b.create_date = :createAt, b.status = 'T' where b.board_id = :boardId")
                .setParameter("createAt", createAt30minute)
                .setParameter("boardId", incompleteBoardWrittenWithinOneHour.getId()).executeUpdate();

        //하루 전에 작성하다가 취소한 게시물
        LocalDateTime createAt = LocalDateTime.now().minusDays(1).minusHours(1).minusMinutes(30);
        Board incompleteBoardWrittenWithinOneDay = createBoard(member, category);
        em.createNativeQuery("update Board b set b.create_date = :createAt, b.status = 'T' where b.board_id = :boardId")
                .setParameter("createAt", createAt)
                .setParameter("boardId", incompleteBoardWrittenWithinOneDay.getId()).executeUpdate();

        em.flush();
        em.clear();

        //when : 하루 이전까지 작성 중단된 게시물 사져옴
        LocalDateTime deleteBoardBeforeDate = LocalDateTime.now().minusDays(1);
        List<Long> incompleteBoardsBeforeDate = boardRepository
                .findIncompleteBoardsBeforeDate(deleteBoardBeforeDate, BoardStatus.T, CategoryType.GOD_LIFE_STIMULUS);

        //then
        Assertions.assertThat(incompleteBoardsBeforeDate.size()).isEqualTo(1);
    }

    @Test
    void 갓생_자극_게시물_제목으로만_조건_검색_테스트() {
        createGodStimulateBoard();

        //when
        StimulationBoardSearchCondition request = new StimulationBoardSearchCondition("test1", null, null);
        List<GodLifeStimulationBoardBriefResponse> responses = boardRepository.findStimulusBoardSearchCondition(request);

        //then
        // test1, test11 ==> 2개
//        for (GodLifeStimulationBoardResponse response : responses) {
//            System.out.println(response);
//        }
        Assertions.assertThat(responses).size().isEqualTo(12);
    }

    @Test
    void 갓생_자극_게시물_제목과닉네임으로_조건검색_테스트() {
        createGodStimulateBoard();

        //when
        StimulationBoardSearchCondition request = new StimulationBoardSearchCondition("test1", "tester", null);
        List<GodLifeStimulationBoardBriefResponse> responses = boardRepository.findStimulusBoardSearchCondition(request);

        Assertions.assertThat(responses).size().isEqualTo(12);
    }

    @Test
    void 갓생_자극_게시물_모든조건으로__조건검색_테스트() {
        createGodStimulateBoard();

        //when
        StimulationBoardSearchCondition request =
                new StimulationBoardSearchCondition("title test1", "tester", "introduction test1");
        List<GodLifeStimulationBoardBriefResponse> responses = boardRepository.findStimulusBoardSearchCondition(request);

        Assertions.assertThat(responses).size().isEqualTo(12);
        Assertions.assertThat(responses.stream().mapToInt(GodLifeStimulationBoardBriefResponse::getGodLifeScore).sum())
                .isEqualTo(0);
        Assertions.assertThat(responses.stream().mapToInt(GodLifeStimulationBoardBriefResponse::getView).sum())
                .isEqualTo(0);
    }

    @Test
    void 갓생_자극_게시물_검색조건에_맞는_게시물이_없는_경우() {
        createGodStimulateBoard();

        //when
        StimulationBoardSearchCondition request =
                new StimulationBoardSearchCondition("asdf", "asdf", "asdf");
        List<GodLifeStimulationBoardBriefResponse> responses = boardRepository.findStimulusBoardSearchCondition(request);

        Assertions.assertThat(responses).size().isEqualTo(0);
    }

    @Test
    void 전체기간_갓생자극_베스트_게시물_조회() {
        //given
        createTestCase();

        //when
        List<GodLifeStimulationBoardBriefResponse> result = boardRepository.findAllTimePopularStimulusBoardList();

        //then
        Assertions.assertThat(result).size().isEqualTo(2);
        Assertions.assertThat(result.get(0).getGodLifeScore()-Board.WRITE_POINT).isEqualTo(4);
        Assertions.assertThat(result.get(0).getThumbnailUrl()).isEqualTo(IMAGE_NAME);
        Assertions.assertThat(result.get(1).getGodLifeScore()-Board.WRITE_POINT).isEqualTo(0);
        Assertions.assertThat(result.get(1).getThumbnailUrl()).isEqualTo(IMAGE_NAME);
    }

    @Test
    void 전체기간_갓생자극_최대조회수_게시물_조회(){
        //given
        Category category = categoryRepository.findByCategoryType(CategoryType.GOD_LIFE_STIMULUS);
        Member member = createMember("1234", "tester");
        createBoard(member, category);
        createBoard(member, category);
        createBoard(member, category);
        createBoard(member, category);
        em.createNativeQuery("update board set board.view = board.board_id * 10").executeUpdate();
        em.flush();
        em.clear();

        //when
        List<GodLifeStimulationBoardBriefResponse> mostViewedBoardList = boardRepository.findMostViewedBoardList();

        //then
        List<Integer> expectedViewCount = mostViewedBoardList.stream()
                .map(GodLifeStimulationBoardBriefResponse::getBoardId)
                .map(a -> (int)(a * 10))
                .toList();

        Assertions.assertThat(mostViewedBoardList).size().isEqualTo(4);
        Assertions.assertThat(mostViewedBoardList.stream().map(GodLifeStimulationBoardBriefResponse::getView).toList())
                .containsExactlyElementsOf(expectedViewCount);
    }

    private void createGodStimulateBoard() {
        Category category = categoryRepository.findByCategoryType(CategoryType.GOD_LIFE_STIMULUS);
        //given
        Member member = createMember("1234", "tester");
        int idx = 1;
        for (int i = 1; i <= 10; i++) {
            for (int j = 1; j <= 10; j++) {
                Board board = Board
                        .builder()
                        .title("title test" + idx)
                        .content("content test" +  idx)
                        .introduction("introduction test" + idx++)
                        .member(member)
                        .view(0)
                        .totalScore(0)
                        .category(category)
                        .status(BoardStatus.S)
                        .category(category)
                        .build();
                boardRepository.save(board);
            }
        }
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

    private Image createImage(Member member, Board board, String serverName) {
        Image image = Image.builder()
                .serverName(serverName)
                .member(member)
                .build();
        return imageRepository.save(image);
    }


    private Board createBoard(Member member, Category category){
        Board board = Board
                .builder()
                .title("test")
                .content("test1")
                .member(member)
                .view(0)
                .totalScore(2)
                .category(category)
                .thumbnailUrl(IMAGE_NAME)
                .status(BoardStatus.S)
                .build();
        boardRepository.save(board);
        GodLifeScore godLifeScore = GodLifeScore.likeMemberToBoard(member, board);
        godLifeScoreRepository.save(godLifeScore);
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

}
