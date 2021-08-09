package com.nuritech.excs.restapi;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 스프링시큐리티와 JwtToken을 이용한 API 인증을 구현 및 refresh token 구현
 * - 테스트 방법 : localhost:8080/index
 *
 * 1) 사용자 회원가입하면 auth0 JWT를 이용해 Token 생성하고, 사용자정보와 함께 저장
 * 2) 사용자가 ID, PW를 이용해 로그인을 하면 서버에서 사용자의 Token을 반환
 * 3) Client에 Token을 저장한 뒤, 검증이 필요한 서비스 이용 시 Header에 Token을 포함시켜 호출
 * 4) 서버에서 토큰을 검증한 후 API 응답을 수행
 * 5) token이 만료된 경우 refresh 토큰을 이용하여 access token 재발급
 *
 * 구현환경 : IntelliJ, H2, JPA
 *
 * 참고사이트 : https://samtao.tistory.com/49?category=921295
 *            https://galid1.tistory.com/755?category=782583
 *
 * test : 1) post : localhost:8080/signUp
 * {
 *     "email":"user_id2",
 *     "password":"user_pw2",
 *     "auth":"ROLE_USER"
 * }
 *
 * 2) post localhost:8080/authenticate
 * {
 *     "username":"user_id2",
 *     "password":"user_pw2"
 * }
 *
 * 3) get : localhost:8080/contents
 *
 */
@Slf4j
@SpringBootApplication
public class RestApiSample2Application {
    public static void main(String[] args) throws Exception {
        SpringApplication.run(RestApiSample2Application.class, args);
    }
}
