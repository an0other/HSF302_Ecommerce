package com.hsf.hsf302_ecom.repository;

import com.hsf.hsf302_ecom.entity.Payments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentsRepo extends JpaRepository<Payments, Long> {

}
