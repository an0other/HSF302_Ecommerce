package com.hsf.hsf302_ecom.repository;

import com.hsf.hsf302_ecom.entity.Brands;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BrandsRepo extends JpaRepository<Brands, Long> {

    List<Brands> findDistinctByProductsStatusTrueAndProductsCategoryIdOrderByNameAsc(Long categoryId);
}