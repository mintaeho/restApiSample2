package com.nuritech.excs.restapi.common.security;

import com.nuritech.excs.restapi.common.util.JwtTokenUtil;
import com.nuritech.excs.restapi.user.domain.UserEntity;
import com.nuritech.excs.restapi.user.ApiTokenType;
import com.nuritech.excs.restapi.user.service.UserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;

@Component
@Slf4j
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    private final String TOKEN_PREFIX = "Bearer ";
    private final String REFRESH_TOKEN = "REFRESH_TOKEN";

    @Autowired
    private UserService userService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
            String username = validateAuthorizationHeader(authorizationHeader);
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                doAuthenticate(getAccessTokenFromAuthorizationHeader(authorizationHeader));
            }
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private String validateAuthorizationHeader(String authorizationHeader) {
        String username = null;
        if ( authorizationHeader != null && authorizationHeader.startsWith(TOKEN_PREFIX) ) {
            try {
                username = jwtTokenUtil.getUsernameFromToken(this.getAccessTokenFromAuthorizationHeader(authorizationHeader));
            } catch (IllegalArgumentException e) {
                log.warn("Unable to get JWT Token");
            } catch (ExpiredJwtException e) {
                log.warn("JWT Token has expired");
            } catch (SignatureException e) {
                log.debug("JwtRequestFilter:validateAuthorizationHeader:{}", e.getMessage());
                throw e;
            }
        } else {
            log.warn("JWT Token does not begin with Bearer String");
        }
        return username;
    }

    private void doAuthenticate(String accessToken) {
        // access ?????? ???????????? ????????????, refresh token?????? ???????????? ???????????? ????????????
        // refresh token??????
        // token ????????? ??????
        jwtTokenUtil.validateToken(accessToken);
        String email = jwtTokenUtil.getClaimFromToken(accessToken, Claims::getAudience);
        UserDetails userDetails = userService.loadUserByUsername(email);

        // ????????? token??? ?????? ?????? ??????
        UserEntity userEntity = (UserEntity) userDetails;
        if ( !userService.compareIssuedToken(userEntity.getUserId(), accessToken, ApiTokenType.ACCESS_TOKEN) ) throw new IllegalArgumentException("????????? Token??? ????????????.");

        Collection<? extends GrantedAuthority> authorities = null;
        try {
            authorities = userService.getAuthorities(userDetails);
        } catch(Exception e) {
            log.debug(e.getMessage());
        }
        makeAuthenticated(new UsernamePasswordAuthenticationToken(userDetails, accessToken, authorities));
    }
    private String getAccessTokenFromAuthorizationHeader(String authorizationHeader) {
        return authorizationHeader.substring(TOKEN_PREFIX.length());
    }
    private void makeAuthenticated(Authentication authentication) {
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

}