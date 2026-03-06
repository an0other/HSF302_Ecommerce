package com.hsf.hsf302_ecom.repository;

import com.hsf.hsf302_ecom.entity.Inventories;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InventoriesRepo extends JpaRepository<Inventories, Long> {

}
