package com.god.life.member;

import com.god.life.config.JpaAuditingConfiguration;
import com.god.life.domain.*;
import com.god.life.dto.member.response.MemberInfoResponse;
import com.god.life.dto.popular.PopularMemberResponse;
import com.god.life.repository.*;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

@ActiveProfiles("test")
@DataJpaTest
@TestPropertySource(locations = "classpath:application-test.yaml")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaAuditingConfiguration.class)// 생성시간/수정시간 자동 주입 설정파일 임포트
public class MemberRepositoryTest {

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private GodLifeScoreRepository godLifeScoreRepository;

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @BeforeEach
    void init(){
        Category godPageCategory = new Category(CategoryType.GOD_LIFE_STIMULUS);
        Category godStimulusCategory = new Category(CategoryType.GOD_LIFE_PAGE);

        categoryRepository.save(godPageCategory);
        categoryRepository.save(godStimulusCategory);

        Member member = createMember("1", "1");
        Image profile = createImage(member, null, "profile");
        member.updateProfileImageName(profile.getServerName());
        Image background = createImage(member, null, "background");
        member.updateBackgroundImageName(background.getServerName());
        Member member1 = createMember("2", "2");
        Member member2 = createMember("3", "3");
        Member member3 = createMember("4", "4");

        Board boardMember1_1 = createBoard(member, godPageCategory);
        Board boardMember1_2 = createBoard(member, godPageCategory);
        Board boardMember1_3 = createBoard(member, godPageCategory);
        Board boardMember2_1 = createBoard(member1, godPageCategory);
        Board boardMember2_2 = createBoard(member1, godPageCategory);
        Board boardMember3_1 = createBoard(member2, godStimulusCategory);
        Board boardMember4_1 = createBoard(member3, godPageCategory);
        Board boardMember4_2 = createBoard(member3, godStimulusCategory);

        GodLifeScore like = createLike(member, boardMember1_1);
        GodLifeScore like1 = createLike(member1, boardMember1_1);
        GodLifeScore like2 = createLike(member2, boardMember1_1);
        GodLifeScore like3 = createLike(member1, boardMember1_2);
        GodLifeScore like4 = createLike(member2, boardMember1_2);
        GodLifeScore like5 = createLike(member, boardMember2_1);
        GodLifeScore like6 = createLike(member, boardMember3_1);
        GodLifeScore like7 = createLike(member1, boardMember3_1);

        boardRepository.incrementGodLifeScore(boardMember1_1.getId());
        boardRepository.incrementGodLifeScore(boardMember1_1.getId());
        boardRepository.incrementGodLifeScore(boardMember1_1.getId());
        boardRepository.incrementGodLifeScore(boardMember1_2.getId());
        boardRepository.incrementGodLifeScore(boardMember1_2.getId());
        boardRepository.incrementGodLifeScore(boardMember2_1.getId());
        boardRepository.incrementGodLifeScore(boardMember3_1.getId());
        boardRepository.incrementGodLifeScore(boardMember3_1.getId());
    }


    @Test
    void 회원_정보_조회_테스트() {
        //given : 1번 회원 정보 조회
        Member member = memberRepository.findByProviderId("1").get();

        //when
        MemberInfoResponse memberInfo = memberRepository.getMemberInfo(member.getId());

        //then
        Assertions.assertThat(memberInfo.getMemberBoardCount()).isEqualTo(3);
        Assertions.assertThat(memberInfo.getGodLifeScore()).isEqualTo(10);
        Assertions.assertThat(memberInfo.getNickname()).isEqualTo("1");
        Assertions.assertThat(memberInfo.getBackgroundImageURL()).isEqualTo("background");
        Assertions.assertThat(memberInfo.getProfileImageURL()).isEqualTo("profile");
    }


    @Test
    void 한주간_인기있는_회원_정보_조회(){
        //given : beforeEach

        //when
        List<PopularMemberResponse> weeklyPopularMember = memberRepository.findWeeklyPopularMember();

        //then
        Assertions.assertThat(weeklyPopularMember).size().isEqualTo(3);
        Assertions.assertThat(weeklyPopularMember.get(0).getGodLifeScore()).isEqualTo(10);
        Assertions.assertThat(weeklyPopularMember.get(1).getGodLifeScore()).isEqualTo(4);
        Assertions.assertThat(weeklyPopularMember.get(2).getGodLifeScore()).isEqualTo(2);
        for (PopularMemberResponse popularMemberResponse : weeklyPopularMember) {
            System.out.println(popularMemberResponse);
        }
    }

    @Test
    void 전체기간_인기있는_회원_정보_조회(){
        //given : beforeEach

        //when
        List<PopularMemberResponse> weeklyPopularMember = memberRepository.findWeeklyPopularMember();

        //then
        Assertions.assertThat(weeklyPopularMember).size().isEqualTo(3);
        Assertions.assertThat(weeklyPopularMember.get(0).getGodLifeScore()).isEqualTo(10);
        Assertions.assertThat(weeklyPopularMember.get(1).getGodLifeScore()).isEqualTo(4);
        Assertions.assertThat(weeklyPopularMember.get(2).getGodLifeScore()).isEqualTo(2);
        for (PopularMemberResponse popularMemberResponse : weeklyPopularMember) {
            System.out.println(popularMemberResponse);
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
                .totalScore(0)
                .category(category)
                .status(BoardStatus.S)
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
                .whoAmI("whoAmI").build();

        memberRepository.save(member);
        return member;
    }
}
