package com.example.vida.dto.request;

import com.example.vida.dto.response.LoginResponse;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
public class LoginRequest implements Serializable {
    private static final long serialVersionUID = 5926468583005150707L;
    private String email;
    private String password;
    public LoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }
}
