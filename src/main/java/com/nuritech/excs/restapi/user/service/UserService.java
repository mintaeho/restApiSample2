package com.nuritech.excs.restapi.user.service;

import com.nuritech.excs.restapi.common.util.JwtTokenUtil;
import com.nuritech.excs.restapi.user.ApiTokenType;
import com.nuritech.excs.restapi.user.Authority;
import com.nuritech.excs.restapi.user.dto.RefreshTokenUpdateRequestDto;
import com.nuritech.excs.restapi.user.dto.SignInResponseDto;
import com.nuritech.excs.restapi.user.domain.ApiTokenEntity;
import com.nuritech.excs.restapi.user.domain.ApiTokenRepository;
import com.nuritech.excs.restapi.user.domain.UserEntity;
import com.nuritech.excs.restapi.user.domain.UserRepository;
import com.nuritech.excs.restapi.user.dto.SignUpRequestDto;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Collection;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final ApiTokenRepository apiTokenRepository;

    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;


    /**
     * 사용자 정보를 조회한다.
     * Spring Security 필수 메소드 구현
     * 기본적인 반환 타입은 UserDetails, UserDetails를 상속받은 UserInfo로 반환 타입 지정 (자동으로 다운 캐스팅됨)
     * @param email 이메일
     * @return UserEntity
     * @throws UsernameNotFoundException 유저가 없을 때 예외 발생
     */
    @Override
    @Transactional
    public UserEntity loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException((email)));
    }

    /**
     * 회원정보를 저장한다.
     * @param request 회원정보가 들어있는 DTO
     * @return 저장되는 회원의 PK
     */
    @Transactional
    public Long save(SignUpRequestDto request) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        UserEntity userEntity = new UserEntity(request.getEmail(),  encoder.encode(request.getPassword()), Authority.USER);
        userRepository.save(userEntity);
        return userEntity.getUserId();
    }

    /**
     * 로그인 하는 사용자를 인증하고, token을 발급한다.
     * @param username
     * @param password
     * @return
     * @throws Exception
     */
    @Transactional
    public Long authenticate(String username, String password) throws Exception {
        try {
            UserEntity userEntity = this.loadUserByUsername(username);
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
            issueToken(userEntity.getUserId(), userEntity.getEmail());
            return userEntity.getUserId();
        } catch (DisabledException e) {
            throw new Exception("USER_DISABLED", e);
        } catch (BadCredentialsException e) {
            throw new Exception("INVALID_CREDENTIALS", e);
        }
    }

    /**
     * 로그인하는 사용자의 권한을 조회한다.
     * @param userDetails
     * @return
     * @throws Exception
     */
    @Transactional
    public Collection<? extends GrantedAuthority> getAuthorities(UserDetails userDetails) throws Exception {
        return userDetails.getAuthorities();
    }

    private void issueToken(Long userId, String email) {
        apiTokenRepository.removeByUserId(userId);

        ApiTokenEntity entity = ApiTokenEntity.builder()
                .userId(userId)
                .accessToken(jwtTokenUtil.generateToken(email, ApiTokenType.ACCESS_TOKEN))
                .refreshToken(jwtTokenUtil.generateToken(email, ApiTokenType.REFRESH_TOKEN))
                .build();

        apiTokenRepository.save(entity);
    }

    /**
     * 사용자의 token 정보를 조회한다.
     * @param userId
     * @return
     */
    public SignInResponseDto findFirstByUserId(Long userId) {
        ApiTokenEntity entity = apiTokenRepository.findFirstByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 token 입니다."));
        return new SignInResponseDto(entity);
    }

    /**
     * refresh token으로 access token 및 refresh token을 재발급 한다.
     * @param request
     * @return
     */
    @Transactional
    public Long refreshToken(RefreshTokenUpdateRequestDto request) {
        ApiTokenEntity apiTokenEntity = apiTokenRepository.findFirstByUserId(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 User ID 입니다. ID="+request.getUserId()));
        apiTokenEntity.update(request.getUserId(),
                request.getAccessToken(),
                request.getRefreshToken()
        );
        return request.getUserId();

    }

    /**
     * 발급된 token과 제출된 token의 일치여부를 확인한다.
     * @param userId
     * @param token
     * @return
     */
    public boolean compareIssuedToken(Long userId, String token, ApiTokenType type) {
        SignInResponseDto issuedToken = this.findFirstByUserId(userId);
        if(type == ApiTokenType.ACCESS_TOKEN) return StringUtils.equals(token, issuedToken.getAccessToken());
        else return StringUtils.equals(token, issuedToken.getRefreshToken());
    }

}
