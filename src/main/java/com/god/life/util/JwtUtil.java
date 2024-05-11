package com.god.life.util;

import com.god.life.exception.JwtInvalidException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;


@Component
public class JwtUtil {


    @Value("${jwt.secret.key}")
    private String secret;

    @Value("${jwt.secret.issuer}")
    private String ISSUER;

    @Value("${jwt.secret.subject}")
    private String SUBJECT;

    @Value("${jwt.secret.expire.access}")
    private int ACCESS_EXPIRATION_TIME;

    @Value("${jwt.secret.expire.refresh}")
    private int REFRESH_EXPIRATION_TIME;

    private SecretKey secretKey;



    @PostConstruct
    public void init() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    // ACCESS TOKEN 생성
    public String createAccessToken(String id, String nickname) {
        return createJWT(id, nickname, ACCESS_EXPIRATION_TIME);
    }

    // REFRESH TOKEN 생성
    public String createRefreshToken() {
        return createJWT("refresh", "refresh", REFRESH_EXPIRATION_TIME);
    }

    public String testCreateToken(String id, String nickname) {
        return createJWT(id, nickname, 10);
    }


    private String createJWT(String id, String nickname, int time) {
        Date now = new Date();
        return Jwts.builder()
                .issuer(ISSUER)
                .subject(SUBJECT)
                .claim("nickname", nickname)
                .claim("id", id)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + time))
                .signWith(secretKey, Jwts.SIG.HS512).compact();
    }

    public Claims getClaims(String jwt) {
        try{
            return Jwts
                    .parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(jwt)
                    .getPayload();
        } catch (SignatureException signatureException) {
            throw new JwtInvalidException("서명 키가 잘못되었습니다.");
        } catch (ExpiredJwtException exception) {
            throw new JwtInvalidException("JWT 토큰이 만료되었습니다.");
        } catch (MalformedJwtException malformedJwtException) {
            throw new JwtInvalidException("JWT가 조작되었습니다.");
        } catch (IllegalArgumentException illegalArgumentException) {
            throw new IllegalArgumentException("잘못된 JWT 입니다. (비어있는 경우 등)");
        }
    }


    public String getId(String jwt){
        return getContent(jwt, "id");
    }

    public String getRole(String jwt) {
        return getContent(jwt, "role");
    }

    public String getTokenType(String jwt) {
        return getContent(jwt, "type");
    }


    private String getContent(String jwt, String content) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(jwt).getPayload().get(content, String.class);
    }

    // Jwt 의 만료 시간 확인
    // 현재 시간보다 before 값이 참이라면 만료된 Jwt 임.
    public boolean validateExpiredJwt(String jwt) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(jwt).getPayload().getExpiration().before(new Date());
    }




}
