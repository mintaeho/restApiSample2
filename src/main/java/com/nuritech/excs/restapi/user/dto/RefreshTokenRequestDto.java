package com.nuritech.excs.restapi.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@NoArgsConstructor //need default constructor for JSON Parsing
@AllArgsConstructor
public class RefreshTokenRequestDto implements Serializable {

    private static final long serialVersionUID = 5926468583005150707L;

    private Long userId;
    private String email;
    private String refreshToken;
}