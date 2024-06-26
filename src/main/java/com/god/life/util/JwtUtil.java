package com.god.life.util;

import com.god.life.dto.TokenResponse;
import com.god.life.error.JwtInvalidException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.text.SimpleDateFormat;
import java.util.Date;


@Component
@Slf4j
@NoArgsConstructor
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

    public static final String AUTHORIZE_HEADER = "Authorization";
    public static final String AUTHORIZE_HEADER_PREFIX = "Bearer ";

    public static final String ACCESS = "access";
    public static final String REFRESH = "refresh";



    @PostConstruct
    public void init() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    // ACCESS TOKEN 생성
    public String createAccessToken(String id, String nickname) {
        return createJWT(id, nickname, ACCESS_EXPIRATION_TIME, ACCESS);
    }

    // REFRESH TOKEN 생성
    public String createRefreshToken() {
        return createJWT("refresh", "refresh", REFRESH_EXPIRATION_TIME, REFRESH);
    }

    public String testCreateToken(String id, String nickname) {
        return createJWT(id, nickname, 10, ACCESS);
    }


    private String createJWT(String id, String nickname, int time, String tokenType) {
        Date now = new Date();
        return Jwts.builder()
                .issuer(ISSUER)
                .subject(SUBJECT)
                .claim("nickname", nickname)
                .claim("id", id)
                .claim("type", tokenType)
                .claim("role", "ROLE_USER")
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
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(jwt).getPayload().getExpiration().before(new Date(System.currentTimeMillis()));
    }

    public static String parseJwt(String authorizeHeader) {
        if (StringUtils.hasText(authorizeHeader) &&
                authorizeHeader.startsWith(AUTHORIZE_HEADER_PREFIX)) {
            return authorizeHeader.substring(AUTHORIZE_HEADER_PREFIX.length());
        }

        return null;
    }

    public TokenResponse createToken(String id, String nickname){
        String accessToken = createAccessToken(id, nickname);
        String refreshToken = createRefreshToken();
        return new TokenResponse(accessToken, refreshToken);
    }

    public void validateRefreshJwt(String jwt) {
        if (jwt == null) {
            throw new JwtInvalidException("refresh 토큰이 존재하지 않습니다.");
        }

        // jwt 만료 확인
        try {
            validateExpiredJwt(jwt);
        } catch (ExpiredJwtException ex) {
            throw new JwtInvalidException("토큰이 만료됐습니다. 다시 로그인해 주세요.");
        }

        // jwt 토큰 종류 확인
        if (!getTokenType(jwt).equals(JwtUtil.REFRESH)) {
            log.info("잘못된 토큰!!! getTokenType(jwt).equals(JwtUtil.REFRESH) ");
            throw new JwtInvalidException("잘못된 토큰입니다.");
        }
    }

    public Date getExpirationTime(String accessToken) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(accessToken).getPayload()
                .getExpiration();
    }
}
