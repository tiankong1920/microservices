package com.inventory.authservice.repository;

import com.inventory.authservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IUserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.accountNonLocked = false")
    List<User> findByAccountNonLockedFalse();

    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.roleName = :roleName")
    List<User> findByRoleName(String roleName);
}
