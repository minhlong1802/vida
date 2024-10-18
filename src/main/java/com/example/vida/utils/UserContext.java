package com.example.vida.utils;

import org.springframework.security.core.userdetails.UserDetails;

public class UserContext {
    private static final ThreadLocal<UserDetails> userHolder = new ThreadLocal<>();

    public static void setUser(UserDetails userDetails) {
        userHolder.set(userDetails);
    }

    public static UserDetails getUser() {
        return userHolder.get();
    }

    public static void clear() {
        userHolder.remove();
    }
}