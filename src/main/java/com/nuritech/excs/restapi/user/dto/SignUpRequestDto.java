package com.nuritech.excs.restapi.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class SignUpRequestDto {
    private String email;
    private String password;
    private String auth;
}