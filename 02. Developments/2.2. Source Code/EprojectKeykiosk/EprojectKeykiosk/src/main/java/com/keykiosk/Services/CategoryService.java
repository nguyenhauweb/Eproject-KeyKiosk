package com.keykiosk.Services;

//import com.keykiosk.Controllers.Admin.CategoryObserver;
import com.keykiosk.Controllers.Admin.ProductController;
import com.keykiosk.Models.DTO.CategoryDTO;
import com.keykiosk.Models.Entity.CategoryEntity;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface CategoryService {
    List<CategoryDTO> getAllImagesWithCategory();

    void deleteCategory(CategoryDTO imageId);

    void insertImageAndCategory(CategoryDTO dto, File selectedFile) throws Exception;

    void updateImageAndCategory(CategoryDTO dto, File selectedFile) throws IOException;

    List<CategoryDTO> searchCategories(String keyword);

    List<CategoryDTO> getAllCategories();

    void exportDataToExcel(String excelFilePath) throws IOException;

    void importDataFromExcel(String excelFilePath) throws IOException;

    CategoryDTO getCategoryByName(String categoryName);

}
