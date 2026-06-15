package com.inventory.adminservice.service;

import com.inventory.adminservice.dto.UserDTO;
import com.inventory.adminservice.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface IUserService {
    UserDTO createUser(UserDTO userDTO);
    UserDTO updateUser(Long id, UserDTO userDTO);
    void deleteUser(Long id);
    UserDTO getUserById(Long id);
    UserDTO getUserByUsername(String username);
    Page<UserDTO> getAllUsers(Pageable pageable);
    void assignRoles(Long userId, List<Long> roleIds);
    List<UserDTO> getUsersByRoleId(Long roleId);
    Optional<User> findUserByUsername(String username);
}
