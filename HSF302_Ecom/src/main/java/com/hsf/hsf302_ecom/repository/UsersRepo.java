package com.hsf.hsf302_ecom.repository;

import com.hsf.hsf302_ecom.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsersRepo extends JpaRepository<Users, Long> {

    boolean existsByEmail(String email);

    Users findByEmailIgnoreCase(String email);
}
