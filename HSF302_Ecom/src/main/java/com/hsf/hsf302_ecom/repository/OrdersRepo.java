package com.hsf.hsf302_ecom.repository;

import com.hsf.hsf302_ecom.entity.Orders;
import com.hsf.hsf302_ecom.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrdersRepo extends JpaRepository<Orders, Long> {

    List<Orders> findByUser_IdOrderByCreatedAtDesc(Long userId);

    Optional<Orders> findByIdAndUser_Id(Long orderId, Long userId);

    List<Orders> findAllByOrderByCreatedAtDesc();

    List<Orders> findByStatusOrderByCreatedAtDesc(OrderStatus status);

}
