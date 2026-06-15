package com.inventory.adminservice.service.impl;

import com.inventory.adminservice.config.PasswordPolicyConfig;
import com.inventory.adminservice.dto.UserDTO;
import com.inventory.adminservice.entity.PasswordHistory;
import com.inventory.adminservice.entity.User;
import com.inventory.adminservice.exception.UserNotFoundException;
import com.inventory.adminservice.repository.IPasswordHistoryRepository;
import com.inventory.adminservice.repository.IRoleRepository;
import com.inventory.adminservice.repository.IUserRepository;
import com.inventory.adminservice.service.IUserService;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class UserServiceImpl implements IUserService {

    private static final String USER_NOT_FOUND_BY_ID = "User not found with id: ";
    private static final String USER_NOT_FOUND_BY_USERNAME = "User not found with username: ";
    private static final String ROLE_NOT_FOUND_BY_ID = "Role not found with id: ";
    private static final String OLD_PASSWORD_INCORRECT = "Old password is incorrect";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final IUserRepository userRepository;
    private final IRoleRepository roleRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final PasswordPolicyConfig passwordPolicyConfig;
    private final IPasswordHistoryRepository passwordHistoryRepository;

    @Override
    @Transactional
    public UserDTO createUser(UserDTO userDTO) {
        log.info("Creating user with username: {}", userDTO.getUsername());

        String password = userDTO.getPassword();
        if (password == null || password.isEmpty()) {
            password = generateRandomPassword();
            log.info("Generated random password for user: {}", userDTO.getUsername());
        }

        String encodedPassword = passwordEncoder.encode(password);

        User user = modelMapper.map(userDTO, User.class);
        user.setPassword(encodedPassword);
        user.setEnabled(true);
        user.setAccountNonExpired(true);
        user.setAccountNonLocked(true);
        user.setCredentialsNonExpired(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);

        PasswordHistory passwordHistory = new PasswordHistory();
        passwordHistory.setUserId(savedUser.getId());
        passwordHistory.setPasswordHash(encodedPassword);
        passwordHistory.setCreatedAt(LocalDateTime.now());
        passwordHistoryRepository.save(passwordHistory);

        log.info("User created successfully with id: {}", savedUser.getId());
        return modelMapper.map(savedUser, UserDTO.class);
    }

    @Override
    @Transactional
    public UserDTO updateUser(Long id, UserDTO userDTO) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND_BY_ID + id));

        existingUser.setUsername(userDTO.getUsername());
        existingUser.setEmail(userDTO.getEmail());
        existingUser.setPhone(userDTO.getPhone());
        existingUser.setEnabled(userDTO.getEnabled());
        existingUser.setUpdatedAt(LocalDateTime.now());

        User updatedUser = userRepository.save(existingUser);
        return modelMapper.map(updatedUser, UserDTO.class);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND_BY_ID + id));
        userRepository.delete(user);
    }

    @Override
    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND_BY_ID + id));
        return modelMapper.map(user, UserDTO.class);
    }

    @Override
    public UserDTO getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND_BY_USERNAME + username));
        return modelMapper.map(user, UserDTO.class);
    }

    @Override
    public Page<UserDTO> getAllUsers(Pageable pageable) {
        Page<User> users = userRepository.findAll(pageable);
        return users.map(user -> modelMapper.map(user, UserDTO.class));
    }

    @Override
    @Transactional
    public void assignRoles(Long userId, List<Long> roleIds) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND_BY_ID + userId));

        user.setRoles(roleIds.stream()
                .map(roleId -> roleRepository.findById(roleId)
                        .orElseThrow(() -> new RuntimeException(ROLE_NOT_FOUND_BY_ID + roleId)))
                .collect(Collectors.toSet()));

        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    @Override
    public List<UserDTO> getUsersByRoleId(Long roleId) {
        List<User> users = userRepository.findByRoles_Id(roleId);
        return users.stream()
                .map(user -> modelMapper.map(user, UserDTO.class))
                .toList();
    }

    @Override
    public Optional<User> findUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    private String generateRandomPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
        StringBuilder password = new StringBuilder();
        for (int i = 0; i < 16; i++) {
            int index = SECURE_RANDOM.nextInt(chars.length());
            password.append(chars.charAt(index));
        }
        return password.toString();
    }

    @Transactional
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        log.info("Changing password for user id: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND_BY_ID + userId));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalStateException(OLD_PASSWORD_INCORRECT);
        }

        List<PasswordHistory> recentPasswords = passwordHistoryRepository
                .findByUserIdOrderByCreatedAtDesc(userId);

        int historySize = passwordPolicyConfig.getPasswordHistorySize();
        for (PasswordHistory history : recentPasswords) {
            if (passwordEncoder.matches(newPassword, history.getPasswordHash())) {
                throw new IllegalStateException(
                        "New password cannot be the same as the last " + historySize + " passwords used");
            }
        }

        String encodedNewPassword = passwordEncoder.encode(newPassword);
        user.setPassword(encodedNewPassword);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        PasswordHistory passwordHistory = new PasswordHistory();
        passwordHistory.setUserId(userId);
        passwordHistory.setPasswordHash(encodedNewPassword);
        passwordHistory.setCreatedAt(LocalDateTime.now());
        passwordHistoryRepository.save(passwordHistory);

        log.info("Password changed successfully for user id: {}", userId);
    }

    @Transactional
    public void incrementFailedLoginAttempts(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND_BY_USERNAME + username));

        int currentAttempts = user.getFailedLoginAttempts() != null ? user.getFailedLoginAttempts() : 0;
        user.setFailedLoginAttempts(currentAttempts + 1);

        if (user.getFailedLoginAttempts() >= passwordPolicyConfig.getMaxLoginAttempts()) {
            user.setAccountNonLocked(false);
            user.setLockoutTime(LocalDateTime.now().plusMinutes(passwordPolicyConfig.getLockoutDurationMinutes()));
            log.warn("Account locked for user: {} due to too many failed login attempts", username);
        }

        userRepository.save(user);
    }

    @Transactional
    public void resetFailedLoginAttempts(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND_BY_USERNAME + username));

        user.setFailedLoginAttempts(0);
        user.setAccountNonLocked(true);
        user.setLockoutTime(null);
        userRepository.save(user);

        log.info("Failed login attempts reset for user: {}", username);
    }

    public boolean isAccountLocked(String username) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return false;
        }

        User user = userOpt.get();
        if (!user.isAccountNonLocked()) {
            return true;
        }

        if (user.getLockoutTime() != null && user.getLockoutTime().isBefore(LocalDateTime.now())) {
            user.setAccountNonLocked(true);
            user.setLockoutTime(null);
            user.setFailedLoginAttempts(0);
            userRepository.save(user);
            return false;
        }

        return !user.isAccountNonLocked();
    }
}
