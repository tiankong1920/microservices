package com.inventory.authservice.service;

import com.inventory.authservice.entity.User;
import com.inventory.authservice.repository.IUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.lang.reflect.Field;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserDetailsServiceImplTest {

    @Mock
    private IUserRepository userRepository;

    private UserDetailsServiceImpl userDetailsService;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        userDetailsService = new UserDetailsServiceImpl();
        Field field = UserDetailsServiceImpl.class.getDeclaredField("userRepository");
        field.setAccessible(true);
        field.set(userDetailsService, userRepository);
    }

    @Test
    void testLoadUserByUsername_Success() {
        // Arrange
        String username = "testuser";
        User mockUser = new User();
        mockUser.setUsername(username);
        mockUser.setPassword("encodedPassword");
        mockUser.setAccountNonExpired(true);
        mockUser.setAccountNonLocked(true);
        mockUser.setCredentialsNonExpired(true);
        mockUser.setEnabled(true);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(mockUser));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        // Assert
        assertNotNull(userDetails);
        assertEquals(username, userDetails.getUsername());
        assertEquals("encodedPassword", userDetails.getPassword());
        assertTrue(userDetails.isAccountNonExpired());
        assertTrue(userDetails.isAccountNonLocked());
        assertTrue(userDetails.isCredentialsNonExpired());
        assertTrue(userDetails.isEnabled());

        verify(userRepository, times(1)).findByUsername(username);
    }

    @Test
    void testLoadUserByUsername_UserNotFound() {
        // Arrange
        String username = "nonexistentuser";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UsernameNotFoundException.class, () -> {
            userDetailsService.loadUserByUsername(username);
        });

        verify(userRepository, times(1)).findByUsername(username);
    }
}
