package com.nuritech.excs.restapi.common.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class ErrorResponse  {
    private final int errorCode;
    private final String message;
    //private final String description;

    public String convertToJson() {
        ObjectMapper objectMapper = new ObjectMapper();
        String orderJson = null;
        try {
            orderJson = objectMapper.writeValueAsString(this);
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return orderJson;
    }

}