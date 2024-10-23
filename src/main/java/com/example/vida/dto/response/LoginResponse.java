package com.example.vida.dto.response;

import java.io.Serializable;

public class LoginResponse implements Serializable {
    private static final long serialVersionUID = -8091879091924046844L;
    private final String token;

    public LoginResponse(String token) {
        this.token = token;
    }

    public String getToken() {
        return this.token;
    }
}
