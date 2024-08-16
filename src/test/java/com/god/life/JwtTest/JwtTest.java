package com.god.life.JwtTest;

import com.god.life.error.JwtInvalidException;
import com.god.life.util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

public class JwtTest {

    private JwtUtil jwtUtil;
    private String id = "1";
    private String nickname = "test";

    @BeforeEach
    public void init(){
        jwtUtil = new JwtUtil();
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
                10
        );
        ReflectionTestUtils.setField(
                jwtUtil,
                "REFRESH_EXPIRATION_TIME",
                1_000_000
        );
        jwtUtil.init();
    }


    @Test
    public void create_accessToken_test(){
        //토큰 생성 테스트
        Date now = new Date();

        String accessToken = jwtUtil.createAccessToken(id, nickname);
        Claims claims = jwtUtil.getClaims(accessToken);
        String id = (String) claims.get("id");
        String nickname = (String) claims.get("nickname");
        Date expireTime = claims.getExpiration();

        long diffTime = (expireTime.getTime() - now.getTime()) / (60 * 1000);

        assertThat(id).isEqualTo(this.id);
        assertThat(nickname).isEqualTo(this.nickname);
        assertThat(diffTime).isLessThan(120); //최대 2시간 남았는지 확인
    }


    @Test
    public void create_refreshToken_test(){
        //토큰 생성 테스트
        Date now = new Date();

        String accessToken = jwtUtil.createRefreshToken();
        Claims claims = jwtUtil.getClaims(accessToken);
        Date expireTime = claims.getExpiration();

        long diffTime = (expireTime.getTime() - now.getTime()) / (60 * 1000);

        assertThat(diffTime).isLessThanOrEqualTo(20_160); //최대 2주 남았는지 확인
    }

    @Test
    public void 재발급_테스트() throws InterruptedException {
        Date now = new Date();

        String accessToken = jwtUtil.createAccessToken(id, nickname);
        String refreshToken = jwtUtil.createRefreshToken(); // 추후 DB에 저장해야 함

        Thread.sleep(1000);  // 접근 토큰의 시간은 10초로 1초를 대기해 만료시킴 테스트
        String reIssuedToken = null;
        try {
            jwtUtil.getClaims(accessToken); // JWT expired Exception 발생
        } catch (JwtInvalidException exception) {
            // 토큰 재발급
            // 추후 DB 조회에서 재발급 여부를 확인해야 함
            reIssuedToken = jwtUtil.createRefreshToken();
        }

        System.out.println(accessToken);
        System.out.println(reIssuedToken);
        assertThat(accessToken).isNotEqualTo(reIssuedToken);
        assertThat(reIssuedToken).isNotEqualTo(refreshToken);
    }


}
