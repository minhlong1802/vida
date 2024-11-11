package com.example.vida.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
public class LoginRequest implements Serializable {
    private static final long serialVersionUID = 5926468583005150707L;
    @NotNull(message = "Email is required")
    @NotBlank(message = "Email is required")
    @Email(message = "Email is invalid")
    private String email;
    @NotBlank(message = "Password is required")
    @NotNull(message = "Password is required")
    private String password;
}
