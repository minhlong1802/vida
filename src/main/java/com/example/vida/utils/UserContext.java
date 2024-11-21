package com.example.vida.utils;


import com.example.vida.dto.response.UserDto;
import com.example.vida.dto.request.CreateUserDto;

public class UserContext {
    private static final ThreadLocal<UserDto> userHolder = new ThreadLocal<>();

    private static final ThreadLocal<CreateUserDto> createUserHolder = new ThreadLocal<>();

    public static void setUser(UserDto userDto) {
        userHolder.set(userDto);
    }

    public static UserDto getUser() {
        return userHolder.get();
    }

    public static void clear() {
        userHolder.remove();
    }

    public static CreateUserDto getCreateUser() { return createUserHolder.get(); }
    }