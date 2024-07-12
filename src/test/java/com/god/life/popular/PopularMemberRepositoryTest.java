package com.god.life.popular;

import com.god.life.config.JpaAuditingConfiguration;
import com.god.life.domain.*;
import com.god.life.dto.PopularMemberResponse;
import com.god.life.repository.BoardRepository;
import com.god.life.repository.GodLifeScoreRepository;
import com.god.life.repository.ImageRepository;
import com.god.life.repository.MemberRepository;
import jakarta.persistence.EntityManager;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.List;

@ActiveProfiles("test")
@DataJpaTest
@TestPropertySource(locations = "classpath:application-test.yaml")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaAuditingConfiguration.class)// 생성시간/수정시간 자동 주입 설정파일 임포트
public class PopularMemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private GodLifeScoreRepository godLifeScoreRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private ImageRepository imageRepository;

    @Test
    @DisplayName("1주간 갓생 받은거 테스트_아무도 없을때")
    public void 한주간_인기_멤버가_없음(){
        List<PopularMemberResponse> weeklyPopularMember = memberRepository.findWeeklyPopularMember();
        Assertions.assertThat(weeklyPopularMember.size()).isEqualTo(0);
    }

    @Test
    @DisplayName("전체 기간 받은거 테스트_아무도 없을때")
    public void 전체기간_인기_멤버가_없음(){
        List<PopularMemberResponse> weeklyPopularMember = memberRepository.findAllTimePopularMember();
        Assertions.assertThat(weeklyPopularMember.size()).isEqualTo(0);
    }

    @Test
    @DisplayName("1주간 갓생 받은거 테스트")
    public void 한주간_인기_멤버_테스트(){
        List<Member> all = memberRepository.findAll();

        Member member = createMember("1", "1");
        createImage(member, "profile1");
        Member member1 = createMember("2", "2");
        createImage(member1, "profile2");
        Member member2 = createMember("3", "3");
        Member member3 = createMember("4", "4");

        //회원1 -> 게시글 3개 작성
        Board boardMember1_1 = createBoard(member);
        Board boardMember1_2 = createBoard(member);
        Board boardMember1_3 = createBoard(member);

        //회원2 -> 게시글 2개 작성
        Board boardMember2_1 = createBoard(member1);
        Board boardMember2_2 = createBoard(member1);

        //회원3 -> 게시글 1개 작성
        Board boardMember3_1 = createBoard(member2);

        //회원4 --> 게시글 2개 작성
        Board boardMember4_1 = createBoard(member3);
        Board boardMember4_2 = createBoard(member3);

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
        // 회원 1 -> 게시판 3개 : 따봉 5개 ==> 16점,
        // 회원 2 -> 게시판 2개 : 따봉 1개 ==> 6점,
        // 회원 3 -> 게시판 1개 : 따봉 2개 받아야함 ==> 6점,
        // 회원 4 -> 게시판 2개 ==> 4점
        List<PopularMemberResponse> weeklyPopularMember = memberRepository.findWeeklyPopularMember();

        Assertions.assertThat(weeklyPopularMember.size()).isEqualTo(4);
        Assertions.assertThat(weeklyPopularMember.get(0).getGodLifeScore()).isEqualTo(16); // 10점
        Assertions.assertThat(weeklyPopularMember.get(1).getGodLifeScore()).isEqualTo(6); // 4점
        Assertions.assertThat(weeklyPopularMember.get(2).getGodLifeScore()).isEqualTo(6);; // 2점
        Assertions.assertThat(weeklyPopularMember.get(3).getGodLifeScore()).isEqualTo(4);; // 2점
    }
    @Test
    @DisplayName("전체기간 갓생 받은거 테스트")
    public void 전체_기간_테스트(){
        Member member = createMember("1", "1");
        createImage(member, "profile" + member.getProfileName());
        createImage(member, "background" + member.getBackgroundName());
        Member member1 = createMember("2", "2");
        createImage(member1, "profile2");
        Member member2 = createMember("3", "3");
        Member member3 = createMember("4", "4");

        //회원1 -> 게시글 3개 작성
        Board boardMember1_1 = createBoard(member);
        Board boardMember1_2 = createBoard(member);
        Board boardMember1_3 = createBoard(member);

        //회원2 -> 게시글 2개 작성
        Board boardMember2_1 = createBoard(member1);
        Board boardMember2_2 = createBoard(member1);

        //회원3 -> 게시글 1개 작성
        Board boardMember3_1 = createBoard(member2);

        //회원4 --> 게시글 2개 작성
        Board boardMember4_1 = createBoard(member3);
        Board boardMember4_2 = createBoard(member3);

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

        List<PopularMemberResponse> allTimePopularMember = memberRepository.findAllTimePopularMember();

        Assertions.assertThat(allTimePopularMember.size()).isEqualTo(4);
        Assertions.assertThat(allTimePopularMember.get(0).getGodLifeScore()).isEqualTo(16); // 10점
        Assertions.assertThat(allTimePopularMember.get(1).getGodLifeScore()).isEqualTo(6); // 4점
        Assertions.assertThat(allTimePopularMember.get(2).getGodLifeScore()).isEqualTo(6);; // 2점
        Assertions.assertThat(allTimePopularMember.get(3).getGodLifeScore()).isEqualTo(4);; // 2점
        Assertions.assertThat(allTimePopularMember.get(0).getProfileURL()).isEqualTo("profile" + member.getProviderId());
        Assertions.assertThat(allTimePopularMember.get(0).getBackgroundUrl()).isEqualTo("background" + member.getProviderId());
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
                .profileName("profile" + providerId)
                .backgroundName("background" + providerId)
                .whoAmI("ASDF").build();

        memberRepository.save(member);
        return member;
    }

    private Board createBoard(Member member){
        Board board = Board
                .builder()
                .title("test")
                .content("test1")
                .member(member)
                .view(0)
                .totalScore(Board.WRITE_POINT)
                .build();
        boardRepository.save(board);
        GodLifeScore godLifeScore = GodLifeScore.likeMemberToBoard(member, board);
        godLifeScoreRepository.save(godLifeScore);
        return board;
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

    private GodLifeScore createLike(Member member, Board board, LocalDateTime when) {
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
