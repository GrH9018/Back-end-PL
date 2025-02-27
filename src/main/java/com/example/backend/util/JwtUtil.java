package com.example.backend.util;

import java.security.Key;
import java.util.Base64;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.example.backend.user.entity.UserRoleEnum;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class JwtUtil {
	// Header KEY 값
	public static final String AUTHORIZATION_HEADER = "Authorization";
	// 사용자 권한 값의 KEY
	public static final String AUTHORIZATION_KEY = "auth";
	// Token 식별자
	public static final String BEARER_PREFIX = "Bearer ";
	// 토큰 만료시간
	private final long ACCESS_TOKEN_TIME = 60 * 120 * 1000L; // 60분
	private final long TEMPORAL_TOKEN_TIME = 5 * 60 * 1000L; // 120분

	@Value("${jwt.secret.key}")
	private String secretKey;

	private Key key;

	@PostConstruct
	public void init() {
		byte[] bytes = Base64.getDecoder().decode(secretKey);
		key = Keys.hmacShaKeyFor(bytes);
	}

	// 토큰 생성
	public String createToken(String email,Long userId,String nickname, UserRoleEnum role) {
		Date date = new Date();

		return BEARER_PREFIX +
			Jwts.builder()
				.setSubject(email) // 사용자 식별자값(ID)
				.claim(AUTHORIZATION_KEY, role) // 사용자 권한
				.claim("nickname",nickname)
				.claim("userId",userId)
				.setExpiration(new Date(date.getTime() + ACCESS_TOKEN_TIME)) // 만료 시간
				.setIssuedAt(date) // 발급일
				.signWith(key, SignatureAlgorithm.HS256) // 암호화 알고리즘
				.compact();
	}
	public String createTemporalToken(String email, UserRoleEnum role) {
		Date date = new Date();
		return Jwts.builder()
						.setSubject(email) // 사용자 식별자값(ID)
						.claim(AUTHORIZATION_KEY, role) // 사용자 권한
						.setExpiration(new Date(date.getTime() + TEMPORAL_TOKEN_TIME)) // 만료 시간
						.setIssuedAt(date) // 발급일
						.signWith(key, SignatureAlgorithm.HS256) // 암호화 알고리즘
						.compact();
	}

	// jwt 토큰을 받아올때 - substring
	public String substringToken(String token) {
		if (StringUtils.hasText(token) && token.startsWith(BEARER_PREFIX)) { // 토큰이 공백이 아니고 Bearer로 시작할 때
			return token.substring(7);
		}
		throw new NullPointerException("토큰을 찾을 수 없습니다.");
	}

	// jwt 검증 메서드
	public boolean validateToken(String token) {
		try {
			Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token); // key로 token 검증
			return true;
		} catch (SecurityException | MalformedJwtException | SignatureException e) {
			log.error("Invalid JWT signature, 유효하지 않는 JWT 서명 입니다.");
		} catch (ExpiredJwtException e) {
			log.error("Expired JWT token, 만료된 JWT token 입니다.");
		} catch (UnsupportedJwtException e) {
			log.error("Unsupported JWT token, 지원되지 않는 JWT 토큰 입니다.");
		} catch (IllegalArgumentException e) {
			log.error("JWT claims is empty, 잘못된 JWT 토큰 입니다.");
		}
		return false;
	}

	public Claims getUserInfoFromToken(String token) {
		return Jwts
			.parserBuilder()
			.setSigningKey(key)
			.build()
			.parseClaimsJws(token) //Jws객체는 jwt토큰의 헤더,페이로드,서명을 담고있음
			.getBody(); //Jws 객체에서 페이로드를 반환. 이 페이로드는 Claims라는 객체로 표현됨.
	}
}
