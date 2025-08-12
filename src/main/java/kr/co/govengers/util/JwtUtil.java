// src/main/java/kr/co/govengers/util/JwtUtil.java
package kr.co.govengers.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.Keys;
import kr.co.govengers.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    private final SecretKey secretKey;
    private final long accessTokenExpirationTime; // ms

    public JwtUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.accessTokenExpirationTime}") long accessTokenExpirationTime
    ) {
        // HS256은 최소 256비트(32자 이상) 시크릿 권장
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpirationTime = accessTokenExpirationTime;
    }

    /** 액세스 토큰 발급 */
    public String generateToken(User user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenExpirationTime);

        return Jwts.builder()
                .setSubject(user.getUid())               // uid
                .claim("role", user.getRole())           // USER / ADMIN
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /** 모든 클레임 파싱 (유효성/서명 검증 포함) */
    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /** uid(subject) 가져오기 — 기존 이름과 호환용으로 둘 다 제공 */
    public String getUidFromToken(String token) {
        return parseClaims(token).getSubject();
    }
    public String getUid(String token) { // alias
        return getUidFromToken(token);
    }

    /** role 클레임 가져오기 (필요 시 사용) */
    public String getRoleFromToken(String token) {
        Object r = parseClaims(token).get("role");
        return r == null ? null : String.valueOf(r);
    }

    /** 만료 여부 */
    private boolean isTokenExpired(String token) {
        return parseClaims(token).getExpiration().before(new Date());
    }

    /** 토큰 유효성(서명·형식·만료) */
    public boolean isTokenValid(String token) {
        try {
            // parse에서 서명/형식 검증, 아래에서 만료 체크
            return !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
