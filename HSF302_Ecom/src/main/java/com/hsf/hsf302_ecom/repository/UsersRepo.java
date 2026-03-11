package com.hsf.hsf302_ecom.repository;

import com.hsf.hsf302_ecom.entity.Users;
import com.hsf.hsf302_ecom.enums.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsersRepo extends JpaRepository<Users, Long> {

    Optional<Users> findByUsername(String username);

    Optional<Users> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    Optional<Users> findByUsernameAndStatus(String username, UserStatus status);

    Optional<Users> findByEmailAndStatus(String email, UserStatus status);
}