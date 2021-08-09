package com.nuritech.excs.restapi.user.web;

import com.nuritech.excs.restapi.common.util.JwtTokenUtil;
import com.nuritech.excs.restapi.user.dto.*;
import com.nuritech.excs.restapi.user.ApiTokenType;
import com.nuritech.excs.restapi.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@Slf4j
@RestController
@RequiredArgsConstructor
public class UserApiController {

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private UserService userService;

    /**
     * 사용자 등록 : 사용자 정보를 등록한다.
     *
     * @param request
     * @return
     */
    @PostMapping("/signUp")
    public SignUpResponseDto signUp(@RequestBody SignUpRequestDto request) {
        SignUpResponseDto response = new SignUpResponseDto();

        try {
            userService.save(request);
            response.setResponse("success");
            response.setMessage("회원가입을 성공적으로 완료했습니다.");
        } catch (Exception e) {
            response.setResponse("failed");
            response.setMessage("회원가입을 하는 도중 오류가 발생했습니다.");
            response.setData(e.toString());
        }
        return response;
    }

    /**
     * 사용자 로그인 : 사용자 인증을 수행한다.
     * 인증을 통해 access token과 refresh token을 발급받을 수 있고,
     * 유효한 token을 보유하고 있다면, 인증 절차 없이 API를 사용할 수 있다.
     * token으로 인증을 대체하기 때문이다.
     *
     * @param reqeust
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/authenticate", method = RequestMethod.POST)
    public ResponseEntity<?> createAuthenticationToken(@RequestBody SignInRequestDto reqeust) throws Exception {

        Long userId = userService.authenticate(reqeust.getUsername(), reqeust.getPassword());
/*
        final UserDetails userDetails = userDetailsService
                .loadUserByUsername(reqeust.getUsername());

        final String token = jwtTokenUtil.generateToken(userDetails, ApiTokenType.ACCESS_TOKEN);
        */
        SignInResponseDto signInResponse = userService.findFirstByUserId(userId);
        return ResponseEntity.ok(signInResponse);
    }

    /**
     * 테스트를 위한 메소드
     * @return
     */
    @GetMapping("/contents")
    public String contents() { // 회원 추가
        return "welcome to hell";
    }


    /**
     * acdess token이 만료된 경우 refresh token을 이용하여 access token 재발급 한다.
     * refresh token이 존재하면 refresh token의 유효성을 검증한 후 access token을 재발급 한다.
     * 이때 refresh token의 만료기한이 7일 이내면 refresh token도 재발급한다.
     * token의 기간만료여부와 token의 이메일과 request의 이메일이 동일한지 검증하고,
     * 이상없는 경우 access token을 발급
     *
     * @param request
     * @return
     * @throws Exception
     */
    @PostMapping("/refreshToken")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequestDto request) throws Exception {

        String refreshToken = request.getRefreshToken();
        String email = request.getEmail();
        Long userId = request.getUserId();
        long remainingDays = 1000 * 60 * 60 * 24 * 7;

        if (ObjectUtils.isEmpty(userId))
            throw new IllegalArgumentException("The user id could not be verified.");
        if ( StringUtils.isEmpty(email) )
            throw new IllegalArgumentException("The email could not be verified.");
        if ( StringUtils.isEmpty(refreshToken) )
            throw new IllegalArgumentException("The refresh token could not be verified.");

        // 요청에 포함된 refresh token이 사용자가 발급받은 토큰과 일치하는지 여부 검사
        if ( !userService.compareIssuedToken(userId, refreshToken, ApiTokenType.REFRESH_TOKEN) )
            throw new IllegalArgumentException("발급된 Refersh Token과 다릅니다.");

        // refresh token의 기간만료를 검사하고, 토큰에서 추출한 email과 요청자 email 일치여부를 검사하고,
        // 유효하면 access token 발급
        String accessToken = jwtTokenUtil.refreshToken(email, refreshToken);
        String newRefreshToken = "";

        // refresh token의 기간만료일이 7일 이내면 refresh token도 재발급
        Date expiredDate = jwtTokenUtil.getExpirationDateFromToken(refreshToken);
        long curTime = System.currentTimeMillis();

        if ( expiredDate.getTime() * 1000 - curTime <= remainingDays ) {
            newRefreshToken = jwtTokenUtil.generateToken(email, ApiTokenType.REFRESH_TOKEN);
        }

        // 테이블의 token 정보 갱신
        RefreshTokenUpdateRequestDto refreshTokenUpdateRequest =
                RefreshTokenUpdateRequestDto.builder()
                        .userId(request.getUserId())
                        .accessToken(accessToken)
                        .refreshToken(StringUtils.isNotEmpty(newRefreshToken)?newRefreshToken:refreshToken)
                        .build();
        userService.refreshToken(refreshTokenUpdateRequest);

        return ResponseEntity.ok(refreshTokenUpdateRequest);

    }
}