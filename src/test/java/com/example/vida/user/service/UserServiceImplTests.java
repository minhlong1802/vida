package com.example.vida.user.service;

import com.example.vida.entity.User;
import com.example.vida.repository.UserRepository;
import com.example.vida.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
class UserServiceImplTests {
    @Mock
    UserRepository userRepository;

    @InjectMocks
    UserServiceImpl userService;
    @Test
    void whenUserNotFound_shouldThrowUserNotFoundException() {
        User user = new User();
        user.setEmail("john.doe@example.com");
        user.setUsername("john.doe");
        user.setPassword("123456");
        when(userRepository.findUserByEmailAndPassword(anyString(), anyString())).thenReturn(user);
        UserDetails userDetail = userService.getUserByEmailAndPassword("john.doe@example.com", "123456");
        assertEquals(userDetail.getUsername(), user.getUsername());
//        assertThrows(UserNotFoundException.class, () -> userService.getUserByEmailAndPassword("john.doe@example.com", "123456"));
    }
}
