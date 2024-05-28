package com.keykiosk.Services;

import com.keykiosk.Models.DTO.CategoryDTO;
import com.keykiosk.Models.DTO.ProductDTO;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface ProductService {
    List<ProductDTO> getAllProducts();

    ProductDTO createProduct(ProductDTO productDTO);
    int getSoftwareAccountCountByNameProduct(String nameProduct);
    void deleteProduct(ProductDTO productDTO);
    int getSoftwareLicenseKeyCountByNameProduct(String nameProduct);
    ProductDTO updateImageAndProduct(ProductDTO dto, File selectedFile) throws IOException;

    List<ProductDTO> getAllProductsWithImages();

    void insertImageAndProduct(ProductDTO dto, File selectedFile) throws Exception;

    List<ProductDTO> findByCategoryId(Long categoryId);
    ProductDTO getProductByName(String productName);
    void exportDataToExcel(String excelFilePath) throws IOException;
    ProductDTO getProductById(Long productId);
}
