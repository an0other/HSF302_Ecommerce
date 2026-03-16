package com.hsf.hsf302_ecom.repository;

import com.hsf.hsf302_ecom.entity.OrderItems;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemsRepo extends JpaRepository<OrderItems, Long> {
    List<OrderItems> findByOrder_Id(Long orderId);
}
