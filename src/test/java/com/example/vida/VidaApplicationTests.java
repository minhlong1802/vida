package com.example.vida;

import com.example.vida.dto.response.UserDto;
import com.example.vida.entity.User;
import com.example.vida.exception.UserNotFoundException;
import com.example.vida.repository.UserRepository;
import com.example.vida.service.impl.UserDetailServiceImpl;
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
class VidaApplicationTests {

	@Test
	void contextLoads() {
	}

	@Mock
	UserRepository userRepository;

	@InjectMocks
	UserServiceImpl userService;

	@InjectMocks
	UserDetailServiceImpl userDetailService;
	@Test
	void whenUserNotFound_shouldThrowUserNotFoundException() {
		when(userRepository.findUserByEmailAndPassword(anyString(), anyString())).thenReturn(null);
        assertThrows(UserNotFoundException.class, () -> userService.getUserByEmailAndPassword("john.doe@example.com", "123456"));
	}

	@Test
	void whenUserFound_shouldReturnUserDetails() {
		User user = new User();
		user.setEmail("john.doe@example.com");
		user.setUsername("john.doe");
		user.setPassword("123456");
		when(userRepository.findUserByEmailAndPassword(anyString(), anyString())).thenReturn(user);
		UserDetails userDetail = userService.getUserByEmailAndPassword("john.doe@example.com","123456");
		assertEquals(userDetail.getUsername(), user.getUsername());
	}

	@Test
	void whenUserNotFound_shouldThrowUserNotFound(){
		String invalidUsername="longdz";
		when(userRepository.findByUsername(anyString())).thenReturn(null);
		assertThrows(UserNotFoundException.class,()-> userDetailService.loadUserByUsername(invalidUsername));
	}

	@Test
	void whenUserFound_shouldReturnUserDto(){
		User user = new User();
		user.setId(123);
		user.setEmail("john.doe@example.com");
		user.setUsername("john.doe");
		user.setPassword("123456");
		when(userRepository.findByUsername(anyString())).thenReturn(user);
		UserDto userDto= userDetailService.loadUserByUsername("john.doe");
		assertEquals(userDto.getPassword(),user.getPassword());
	}

}
