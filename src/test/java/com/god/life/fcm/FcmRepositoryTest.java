package com.god.life.fcm;

import com.god.life.config.JpaAuditingConfiguration;
import com.god.life.domain.FcmAlarm;
import com.god.life.domain.Member;
import com.god.life.domain.ProviderType;
import com.god.life.domain.Sex;
import com.god.life.repository.FcmAlarmRepository;
import com.god.life.repository.MemberRepository;
import jakarta.persistence.EntityManager;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.ReflectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@ActiveProfiles("test")
@DataJpaTest
@TestPropertySource(locations = "classpath:application-test.yaml")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaAuditingConfiguration.class)// 생성시간/수정시간 자동 주입 설정파일 임포트
public class FcmRepositoryTest {

    @Autowired
    private FcmAlarmRepository fcmAlarmRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private EntityManager em;


    @Test
    public void 알람_생성_테스트_성공_케이스(){
        //given : 현재 시각에서 2시간 이후로 알람 설정
        Member member = createMember("1234", "tester", "testToken");
        LocalDateTime sendTime = LocalDateTime.now().plusHours(2);
        FcmAlarm f = new FcmAlarm(member, sendTime);

        fcmAlarmRepository.save(f);
        em.flush();
        em.clear();

        //when : DB에서 알람 조회
        List<FcmAlarm> fcmAlarms = fcmAlarmRepository.findAll();

        // then : 알람은 하나만 저장했으므로 size = 1;
        Assertions.assertThat(fcmAlarms.size()).isEqualTo(1);
    }

    @Test
    public void 알람_삭제_테스트(){
        // given
        Member member = createMember("1234", "tester", "testToken");
        LocalDateTime sendTime1 = LocalDateTime.now();
        FcmAlarm f = new FcmAlarm(member, sendTime1);

        LocalDateTime beforeAlarm = LocalDateTime.now().minusDays(1);
        FcmAlarm f1 = new FcmAlarm(member, LocalDateTime.now().plusHours(1));
        injectSendTime(beforeAlarm, f1);

        LocalDateTime beforeAlarm2 = LocalDateTime.now().minusDays(2);
        FcmAlarm f2 = new FcmAlarm(member, LocalDateTime.now().plusHours(1));
        injectSendTime(beforeAlarm2, f2);

        fcmAlarmRepository.save(f);
        fcmAlarmRepository.save(f1);
        fcmAlarmRepository.save(f2);
        em.flush();
        em.clear();

        // when : 금일 알람 삭제
        fcmAlarmRepository.deleteTodayAlarm(member);

        // then : 그럼 알림 테이블에는 어제, 엊그제 알람만 있어야 함
        List<FcmAlarm> alarms = fcmAlarmRepository.findAll();

        Assertions.assertThat(alarms.size()).isEqualTo(2);
        FcmAlarm fcmAlarm = alarms.get(0);
        Assertions.assertThat(fcmAlarm.getSendTime().truncatedTo(ChronoUnit.MINUTES)).isEqualTo(beforeAlarm.truncatedTo(ChronoUnit.MINUTES));
        FcmAlarm fcmAlarm1 = alarms.get(1);
        Assertions.assertThat(fcmAlarm1.getSendTime().truncatedTo(ChronoUnit.MINUTES)).isEqualTo(beforeAlarm2.truncatedTo(ChronoUnit.MINUTES));
    }

    @Test
    public void 금일_알람_조회_테스트(){
        //given
        Member member1 = createMember("1234", "tester1", "testToken1");
        Member member2 = createMember("5678", "tester2", "testToken2");

        LocalDateTime today = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);

        //오늘 알람
        LocalDateTime todaySendTime1 = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        LocalDateTime todaySendTime2 = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        FcmAlarm f1 = new FcmAlarm(member1, LocalDateTime.now().plusHours(1));
        FcmAlarm f2 = new FcmAlarm(member2, LocalDateTime.now().plusHours(1));
        injectSendTime(todaySendTime1, f1);
        injectSendTime(todaySendTime2, f2);

        //어제 알람
        LocalDateTime yesterdaySendTime1 = LocalDateTime.now().minusDays(1).truncatedTo(ChronoUnit.MINUTES);
        LocalDateTime yesterdaySendTime2 = LocalDateTime.now().minusDays(1).truncatedTo(ChronoUnit.MINUTES);
        FcmAlarm f3 = new FcmAlarm(member1, LocalDateTime.now().plusHours(1));
        injectSendTime(yesterdaySendTime1, f3);

        FcmAlarm f4 = new FcmAlarm(member2, LocalDateTime.now().plusHours(1));
        injectSendTime(yesterdaySendTime2, f4);

        //엊그제 알람
        LocalDateTime dayBeforeYesterdaySendTime1 = LocalDateTime.now().minusDays(2).truncatedTo(ChronoUnit.MINUTES);
        LocalDateTime dayBeforeYesterdaySendTime2 = LocalDateTime.now().minusDays(2).truncatedTo(ChronoUnit.MINUTES);
        FcmAlarm f5 = new FcmAlarm(member1, LocalDateTime.now().plusHours(1));
        injectSendTime(dayBeforeYesterdaySendTime1, f5);

        FcmAlarm f6 = new FcmAlarm(member2, LocalDateTime.now().plusHours(1));
        injectSendTime(dayBeforeYesterdaySendTime2, f5);

        fcmAlarmRepository.save(f1);
        fcmAlarmRepository.save(f2);
        fcmAlarmRepository.save(f3);
        fcmAlarmRepository.save(f4);
        fcmAlarmRepository.save(f5);
        fcmAlarmRepository.save(f6);

        em.flush();
        em.clear();

        List<FcmAlarm> all = fcmAlarmRepository.findAll();

        // when : 현재 시각/분에 보내야 할 알람 토큰값을 조회했을 때
        List<String> sendUserTokens = fcmAlarmRepository.findSendUserTokens(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES));

        // then : 2개 있어야함.
        Assertions.assertThat(sendUserTokens.size()).isEqualTo(2);
        Assertions.assertThat(sendUserTokens).containsExactly("testToken1", "testToken2");
    }

    private Member createMember(String providerId, String nickname, String fcmToken) {
        Member member = Member
                .builder()
                .sex(Sex.MALE)
                .providerName(ProviderType.KAKAO)
                .age(18)
                .nickname(nickname)
                .email("ASDF@example.com")
                .providerId(providerId)
                .whoAmI("ASDF")
                .fcmToken(fcmToken).build();

        memberRepository.save(member);
        return member;
    }

    private void injectSendTime(LocalDateTime beforeAlarm, FcmAlarm fcmAlarm) {
        ReflectionTestUtils.setField(
                fcmAlarm,
                "sendTime",
                beforeAlarm,
                LocalDateTime.class
        );
    }

}
