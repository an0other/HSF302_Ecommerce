package com.hsf.hsf302_ecom.repository;

import com.hsf.hsf302_ecom.entity.Orders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrdersRepo extends JpaRepository<Orders, Long> {

    List<Orders> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<Orders> findByIdAndUserId(Long orderId, Long userId);

}
