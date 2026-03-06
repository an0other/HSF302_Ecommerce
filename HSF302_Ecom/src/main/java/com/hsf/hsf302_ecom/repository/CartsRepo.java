package com.hsf.hsf302_ecom.repository;

import com.hsf.hsf302_ecom.entity.Carts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CartsRepo extends JpaRepository<Carts, Long> {

}
