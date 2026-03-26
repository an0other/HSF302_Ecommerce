package com.hsf.hsf302_ecom.service;

import com.hsf.hsf302_ecom.entity.ProductImages;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProductImagesService {

    List<ProductImages> uploadImages(Long productId, List<MultipartFile> files);

    void deleteImage(Long imageId, Long productId);

    void setPrimary(Long imageId, Long productId);

    List<ProductImages> getImages(Long productId);
}