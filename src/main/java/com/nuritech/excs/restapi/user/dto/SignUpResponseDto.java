package com.nuritech.excs.restapi.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SignUpResponseDto {
    private String response;
    private String message;
    private Object data;

    public SignUpResponseDto(String response, String message, Object data) {
        this.response = response;
        this.message = message;
        this.data = data;
    }

}
