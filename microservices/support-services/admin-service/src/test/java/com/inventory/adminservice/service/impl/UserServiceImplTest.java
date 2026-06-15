package com.inventory.adminservice.service.impl;

import com.inventory.adminservice.dto.UserDTO;
import com.inventory.adminservice.entity.User;
import com.inventory.adminservice.entity.Role;
import com.inventory.adminservice.exception.UserNotFoundException;
import com.inventory.adminservice.repository.IRoleRepository;
import com.inventory.adminservice.repository.IUserRepository;
import com.inventory.adminservice.config.PasswordPolicyConfig;
import com.inventory.adminservice.repository.IPasswordHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@SuppressWarnings("null")
public class UserServiceImplTest {

    @Mock
    private IUserRepository userRepository;

    @Mock
    private IRoleRepository roleRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private IPasswordHistoryRepository passwordHistoryRepository;

    @Mock
    private PasswordPolicyConfig passwordPolicyConfig;

    private UserServiceImpl userService;

    private User testUser;
    private UserDTO testUserDTO;
    private Role testRole;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(
                userRepository,
                roleRepository,
                modelMapper,
                passwordEncoder,
                passwordPolicyConfig,
                passwordHistoryRepository
        );
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPhone("1234567890");
        testUser.setEnabled(true);
        testUser.setAccountNonExpired(true);
        testUser.setAccountNonLocked(true);
        testUser.setCredentialsNonExpired(true);
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());

        testUserDTO = new UserDTO();
        testUserDTO.setId(1L);
        testUserDTO.setUsername("testuser");
        testUserDTO.setEmail("test@example.com");
        testUserDTO.setPhone("1234567890");
        testUserDTO.setEnabled(true);

        testRole = new Role();
        testRole.setId(1L);
        testRole.setRoleName("ADMIN");
        testRole.setRoleCode("ADMIN");
        testRole.setDescription("Administrator role");
        testRole.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void testCreateUser() {
        testUserDTO.setPassword("testpassword");
        
        when(modelMapper.map(testUserDTO, User.class)).thenReturn(testUser);
        when(passwordEncoder.encode("testpassword")).thenReturn("encoded_password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(2L);
            return user;
        });
        when(modelMapper.map(any(User.class), eq(UserDTO.class))).thenAnswer(invocation -> {
            UserDTO dto = new UserDTO();
            dto.setId(2L);
            dto.setUsername("testuser");
            dto.setEmail("test@example.com");
            dto.setPhone("1234567890");
            dto.setEnabled(true);
            dto.setAccountNonExpired(true);
            dto.setAccountNonLocked(true);
            dto.setCredentialsNonExpired(true);
            return dto;
        });

        UserDTO result = userService.createUser(testUserDTO);

        assertNotNull(result);
        assertEquals(2L, result.getId());
        assertEquals("testuser", result.getUsername());
        assertEquals("test@example.com", result.getEmail());
        assertEquals("1234567890", result.getPhone());
        assertTrue(result.getEnabled());

        verify(modelMapper, times(1)).map(testUserDTO, User.class);
        verify(passwordEncoder, times(1)).encode("testpassword");
        verify(userRepository, times(1)).save(any(User.class));
        verify(modelMapper, times(1)).map(any(User.class), eq(UserDTO.class));
    }

    @Test
    void testUpdateUser() {
        UserDTO updatedUserDTO = new UserDTO();
        updatedUserDTO.setId(1L);
        updatedUserDTO.setUsername("updateduser");
        updatedUserDTO.setEmail("updated@example.com");
        updatedUserDTO.setPhone("9876543210");
        updatedUserDTO.setEnabled(false);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(modelMapper.map(testUser, UserDTO.class)).thenReturn(updatedUserDTO);

        UserDTO result = userService.updateUser(1L, updatedUserDTO);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("updateduser", result.getUsername());
        assertEquals("updated@example.com", result.getEmail());
        assertEquals("9876543210", result.getPhone());
        assertFalse(result.getEnabled());

        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).save(any(User.class));
        verify(modelMapper, times(1)).map(testUser, UserDTO.class);
    }

    @Test
    void testUpdateUser_NotFound() {
        UserDTO updatedUserDTO = new UserDTO();
        updatedUserDTO.setId(999L);

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> {
            userService.updateUser(999L, updatedUserDTO);
        });

        verify(userRepository, times(1)).findById(999L);
        verify(userRepository, never()).save(any());
        verify(modelMapper, never()).map(any(), any());
    }

    @Test
    void testDeleteUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        doNothing().when(userRepository).delete(any(User.class));

        userService.deleteUser(1L);

        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).delete(any(User.class));
    }

    @Test
    void testDeleteUser_NotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> {
            userService.deleteUser(999L);
        });

        verify(userRepository, times(1)).findById(999L);
        verify(userRepository, never()).delete(any());
    }

    @Test
    void testGetUserById() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(modelMapper.map(testUser, UserDTO.class)).thenReturn(testUserDTO);

        UserDTO result = userService.getUserById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("testuser", result.getUsername());
        assertEquals("test@example.com", result.getEmail());

        verify(userRepository, times(1)).findById(1L);
        verify(modelMapper, times(1)).map(testUser, UserDTO.class);
    }

    @Test
    void testGetUserById_NotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> {
            userService.getUserById(999L);
        });

        verify(userRepository, times(1)).findById(999L);
    }

    @Test
    void testGetUserByUsername() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(modelMapper.map(testUser, UserDTO.class)).thenReturn(testUserDTO);

        UserDTO result = userService.getUserByUsername("testuser");

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("testuser", result.getUsername());
        assertEquals("test@example.com", result.getEmail());

        verify(userRepository, times(1)).findByUsername("testuser");
        verify(modelMapper, times(1)).map(testUser, UserDTO.class);
    }

    @Test
    void testGetUserByUsername_NotFound() {
        when(userRepository.findByUsername("notfound")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> {
            userService.getUserByUsername("notfound");
        });

        verify(userRepository, times(1)).findByUsername("notfound");
    }

    @Test
    void testGetAllUsers() {
        List<User> users = Arrays.asList(testUser);
        Page<User> userPage = new PageImpl<>(users, PageRequest.of(0, 10), users.size());

        when(userRepository.findAll(any(Pageable.class))).thenReturn(userPage);
        when(modelMapper.map(testUser, UserDTO.class)).thenReturn(testUserDTO);

        Page<UserDTO> result = userService.getAllUsers(PageRequest.of(0, 10));

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("testuser", result.getContent().get(0).getUsername());

        verify(userRepository, times(1)).findAll(any(Pageable.class));
        verify(modelMapper, times(1)).map(any(User.class), eq(UserDTO.class));
    }

    @Test
    void testAssignRoles() {
        List<Long> roleIds = Arrays.asList(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(roleRepository.findById(1L)).thenReturn(Optional.of(testRole));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.assignRoles(1L, roleIds);

        verify(userRepository, times(1)).findById(1L);
        verify(roleRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testAssignRoles_UserNotFound() {
        List<Long> roleIds = Arrays.asList(1L);

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> {
            userService.assignRoles(999L, roleIds);
        });

        verify(userRepository, times(1)).findById(999L);
        verify(roleRepository, never()).findById(anyLong());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testAssignRoles_RoleNotFound() {
        List<Long> roleIds = Arrays.asList(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(roleRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            userService.assignRoles(1L, roleIds);
        });

        verify(userRepository, times(1)).findById(1L);
        verify(roleRepository, times(1)).findById(1L);
        verify(userRepository, never()).save(any());
    }

    @Test
    void testGetUsersByRoleId() {
        List<User> users = Arrays.asList(testUser);

        when(userRepository.findByRoles_Id(1L)).thenReturn(users);
        when(modelMapper.map(testUser, UserDTO.class)).thenReturn(testUserDTO);

        List<UserDTO> result = userService.getUsersByRoleId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("testuser", result.get(0).getUsername());

        verify(userRepository, times(1)).findByRoles_Id(1L);
        verify(modelMapper, times(1)).map(testUser, UserDTO.class);
    }

    @Test
    void testGetUsersByRoleId_RoleNotFound() {
        when(userRepository.findByRoles_Id(999L)).thenReturn(Arrays.asList());

        List<UserDTO> result = userService.getUsersByRoleId(999L);

        assertNotNull(result);
        assertEquals(0, result.size());

        verify(userRepository, times(1)).findByRoles_Id(999L);
        verify(modelMapper, never()).map(any(), any());
    }

    @Test
    void testFindUserByUsername() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        Optional<User> result = userService.findUserByUsername("testuser");

        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUsername());

        verify(userRepository, times(1)).findByUsername("testuser");
    }

    @Test
    void testFindUserByUsername_NotFound() {
        when(userRepository.findByUsername("notfound")).thenReturn(Optional.empty());

        Optional<User> result = userService.findUserByUsername("notfound");

        assertFalse(result.isPresent());

        verify(userRepository, times(1)).findByUsername("notfound");
    }
}
