package com.nuritech.excs.restapi.common.util;

import com.nuritech.excs.restapi.user.ApiTokenType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * 토큰 관련 설정을 담당하는 클래스
 * - 토큰을 발급해주고, 자격증명을 관리
 */
@Slf4j
@Component
public class JwtTokenUtil implements Serializable {

    private static final long serialVersionUID = -2550185165626007488L;

    private int ACCESS_TOKEN_EXPIRATION_DATE = 60 * 60 * 3; // 3 hours
    private int REFRESH_TOKEN_EXPIRATION_DATE = 60 * 60 * 24 * 365; // 30 Days
    private SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    /**
     * 사용자의 토큰을 발급한다.
     * @param userDetails
     * @param type
     * @return
     */
    public String generateToken(UserDetails userDetails, ApiTokenType type) {
        Map<String, Object> claims = new HashMap<>();
        return doGenerateToken(claims, userDetails.getUsername(), type);
    }

    /**
     * 사용자의 토큰을 발급한다.
     * @param userName
     * @param type
     * @return
     */
    public String generateToken(String userName, ApiTokenType type) {
        Map<String, Object> claims = new HashMap<>();
        return doGenerateToken(claims, userName, type);
    }

    //while creating the token -
    //1. Define  claims of the token, like Issuer, Expiration, Subject, and the ID
    //2. Sign the JWT using the HS512 algorithm and secret key.
    //3. According to JWS Compact Serialization(https://tools.ietf.org/html/draft-ietf-jose-json-web-signature-41#section-3.1)
    //   compaction of the JWT to a URL-safe string
    private String doGenerateToken(Map<String, Object> claims, String subject, ApiTokenType type) {
        Date expiration;
        if(type == ApiTokenType.ACCESS_TOKEN)
            expiration = new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION_DATE * 1000);
        else
            expiration = new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION_DATE * 1000);

        log.debug("expiration={}", expiration);
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setAudience(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(expiration)
                .signWith(key)
                .compact();
    }

    public String refreshToken(String email, String refreshToken) {
        validateToken(refreshToken, email);
        return generateToken(email, ApiTokenType.ACCESS_TOKEN);
    }

    public void validateToken(String token) {
        if(isTokenExpired(token))
            throw new IllegalStateException("유효하지 않은 토큰입니다.");
    }
    //validate token
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = getUsernameFromToken(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
    public Boolean validateToken(String token, String userName) {
        final String username = getUsernameFromToken(token);
        return (username.equals(userName) && !isTokenExpired(token));
    }

    private Boolean isTokenExpired(String token) {
        Date tokenExpirationDate = getClaimFromToken(token, Claims::getExpiration);
        Date now = new Date();
        return !tokenExpirationDate.after(now);
    }

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimResolver) {
        final Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claimResolver.apply(claims);
    }

    //retrieve username from jwt token
    // jwt token으로부터 username을 획득한다.
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    //retrieve expiration date from jwt token
    // jwt token으로부터 만료일자를 알려준다.
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }




    /*
    public static final long JWT_TOKEN_VALIDITY = 5 * 60 * 60;
    private SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    @Value("${spring.jwt.secret}")
    private String secret;

    //retrieve username from jwt token
    // jwt token으로부터 username을 획득한다.
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    //retrieve expiration date from jwt token
    // jwt token으로부터 만료일자를 알려준다.
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }
    //for retrieveing any information from token we will need the secret key
    private Claims getAllClaimsFromToken(String token) {
        return  Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    //check if the token has expired
    // 토큰이 만료되었는지 확인한다.
    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    //generate token for user
    // 유저를 위한 토큰을 발급해준다.
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return doGenerateToken(claims, userDetails.getUsername());
    }

    //while creating the token -
    //1. Define  claims of the token, like Issuer, Expiration, Subject, and the ID
    //2. Sign the JWT using the HS512 algorithm and secret key.
    //3. According to JWS Compact Serialization(https://tools.ietf.org/html/draft-ietf-jose-json-web-signature-41#section-3.1)
    //   compaction of the JWT to a URL-safe string
    private String doGenerateToken(Map<String, Object> claims, String subject) {

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY * 1000))
                .signWith(key)
                .compact();
    }

    //validate token
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = getUsernameFromToken(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

     */
}