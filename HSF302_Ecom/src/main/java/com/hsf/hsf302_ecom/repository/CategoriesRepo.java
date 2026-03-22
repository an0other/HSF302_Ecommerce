package com.hsf.hsf302_ecom.repository;

import com.hsf.hsf302_ecom.entity.Categories;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoriesRepo extends JpaRepository<Categories, Long> {

    List<Categories> findByStatusTrueOrderByNameAsc();

    List<Categories> findDistinctByStatusTrueAndProductsStatusTrueOrderByNameAsc();

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    Page<Categories> findByNameContainingIgnoreCaseOrderByNameAsc(String keyword, Pageable pageable);

    Page<Categories> findAllByOrderByNameAsc(Pageable pageable);

    long countByStatusTrue();
}