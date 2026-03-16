package com.hsf.hsf302_ecom.repository;

import com.hsf.hsf302_ecom.entity.Carts;
import com.hsf.hsf302_ecom.enums.CartStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartsRepo extends JpaRepository<Carts, Long> {

    Optional<Carts> findByUserIdAndStatus(Long userId, CartStatus status);
}