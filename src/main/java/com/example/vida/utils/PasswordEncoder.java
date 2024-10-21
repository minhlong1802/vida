package com.example.vida.utils;

import org.springframework.stereotype.Component;

@Component
public class PasswordEncoder {

    public String encode(String password) {
        // Đây chỉ là một ví dụ đơn giản, không nên sử dụng trong production
        return password;
    }
}