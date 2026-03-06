package com.hsf.hsf302_ecom.repository;

import com.hsf.hsf302_ecom.entity.ProductImages;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductImagesRepo extends JpaRepository<ProductImages, Long> {

}
