package com.sprint.mission.discodeit.auth.jwt;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.sprint.mission.discodeit.auth.DiscodeitUserDetails;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class JwtTokenProvider {

    public static final String REFRESH_TOKEN_COOKIE_NAME = "REFRESH_TOKEN";
    private final int accessTokenExpirationMs;
    private final int refreshTokenExpirationMs;
    private final boolean isCookieSecured;

    private final JWSSigner accessTokenSigner;
    private final JWSSigner refreshTokenSigner;
    private final JWSVerifier accessTokenVerifier;
    private final JWSVerifier refreshTokenVerifier;

    public JwtTokenProvider(
        @Value("${jwt.access-token.secret}") String accessTokenSecret,
        @Value("${jwt.refresh-token.secret}") String refreshTokenSecret,
        @Value("${jwt.access-token.exp}") int accessTokenExpirationMs,
        @Value("${jwt.refresh-token.exp}") int refreshTokenExpirationMs,
        @Value("${jwt.cookie.secure}") boolean isCookieSecured
    ) throws JOSEException {
        this.accessTokenExpirationMs = accessTokenExpirationMs;
        this.refreshTokenExpirationMs = refreshTokenExpirationMs;
        this.isCookieSecured = isCookieSecured;

        byte[] accessSecretBytes = accessTokenSecret.getBytes(StandardCharsets.UTF_8);
        this.accessTokenSigner = new MACSigner(accessSecretBytes);
        this.accessTokenVerifier = new MACVerifier(accessSecretBytes);

        byte[] refreshSecretBytes = refreshTokenSecret.getBytes(StandardCharsets.UTF_8);
        this.refreshTokenSigner = new MACSigner(refreshSecretBytes);
        this.refreshTokenVerifier = new MACVerifier(refreshSecretBytes);

        log.debug("[JwtTokenProvider] 초기화 완료");
    }

    public String generateAccessToken(DiscodeitUserDetails discodeitUserDetails)
        throws JOSEException {
        log.debug("[JwtTokenProvider] AccessToken 발급 시작, 유저: {}",
            discodeitUserDetails.getUsername());
        return generateToken(discodeitUserDetails, accessTokenExpirationMs, accessTokenSigner,
            "access");
    }

    public String generateRefreshToken(DiscodeitUserDetails discodeitUserDetails)
        throws JOSEException {
        log.debug("[JwtTokenProvider] RefreshToken 발급 시작, 유저: {}",
            discodeitUserDetails.getUsername());
        return generateToken(discodeitUserDetails, refreshTokenExpirationMs, refreshTokenSigner,
            "refresh");
    }

    private String generateToken(DiscodeitUserDetails discodeitUserDetails, int expirationMs,
        JWSSigner signer, String tokenType) throws JOSEException {
        log.debug("[JwtTokenProvider] 토큰 생성 시작");

        String jti = UUID.randomUUID().toString();
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + expirationMs);

        // 토큰 클레임 설정
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
            .subject(discodeitUserDetails.getUsername())
            .jwtID(jti)
            .issueTime(now)
            .notBeforeTime(now)
            .expirationTime(expirationDate)
            .claim("userId", discodeitUserDetails.getId().toString())
            .claim("type", tokenType)
            .build();

        // 토큰 생성 및 서명
        SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet);
        signedJWT.sign(signer);

        log.debug("[TokenProvider] 토큰 생성 완료, 유저: {}, 토큰 타입: {}, 토큰ID: {}",
            discodeitUserDetails.getUsername(), tokenType, jti);
        return signedJWT.serialize();
    }

    public Cookie generateRefreshTokenCookie(String refreshToken) {
        log.debug("[TokenProvider] Refresh Token 쿠키 생성 시작");

        Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE_NAME, refreshToken);
        log.debug("[TokenProvider] Refresh Token 쿠키 응답에 추가");
        cookie.setHttpOnly(true);
        cookie.setSecure(isCookieSecured);    // 개발 환경이라 false 처리
        cookie.setPath("/");
        cookie.setMaxAge(refreshTokenExpirationMs / 1000);

        log.debug("[TokenProvider] Refresh Token 쿠키 생성 완료");
        return cookie;
    }

    public void addRefreshCookie(HttpServletResponse response, String refreshToken) {
        log.debug("[TokenProvider] Refresh Token 쿠키 응답에 추가 시작");
        Cookie cookie = generateRefreshTokenCookie(refreshToken);
        response.addCookie(cookie);
    }

    public boolean validateAccessToken(String accessToken) {
        log.debug("[TokenProvider] Access Token 유효성 검사 시작");
        boolean result = validateToken(accessToken, accessTokenVerifier, "access");
        log.debug("[TokenProvider] Access Token 유효성 검사 완료 : {}", result);
        return result;
    }

    public boolean validateRefreshToken(String refreshToken) {
        log.debug("[TokenProvider] Refresh Token 유효성 검사 시작");
        boolean result = validateToken(refreshToken, refreshTokenVerifier, "refresh");
        log.debug("[TokenProvider] Refresh Token 유효성 검사 완료 : {}", result);
        return result;
    }

    public boolean validateToken(String token, JWSVerifier verifier, String type) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);

            if (!signedJWT.verify(verifier)) {
                log.debug("[TokenProvider] 서명 검증 실패");
                return false;
            }

            String tokenType = signedJWT.getJWTClaimsSet().getStringClaim("type");
            if (type.equals(tokenType)) {
                log.debug("[TokenProvider] 토큰 타입 검증 실패");
                return false;
            }

            Date exp = signedJWT.getJWTClaimsSet().getExpirationTime();
            if (exp == null && exp.before(new Date())) {
                log.debug("[TokenProvider] 만료 시간 검증 실패");
                return false;
            }

            return true;
        } catch (Exception e) {
            log.error("[TokenProvider] 토큰 유효성 검증 예외 발생");
            return false;
        }
    }

    public String getUsernameFromToken(String token) {
        try {
            log.debug("[TokenProvider] 사용자명 추출 시작");

            SignedJWT signedJWT = SignedJWT.parse(token);
            String subject = signedJWT.getJWTClaimsSet().getSubject();

            log.debug("[TokenProvider] 사용자명 추출 완료: {}", subject);
            return subject;
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }
}
