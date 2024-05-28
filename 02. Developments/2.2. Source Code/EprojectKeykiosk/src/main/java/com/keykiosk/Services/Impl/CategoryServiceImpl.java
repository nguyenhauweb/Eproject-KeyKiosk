package com.keykiosk.Services.Impl;

//import com.keykiosk.Controllers.Admin.CategoryObservable;
import com.keykiosk.Config.ConfigUrl;
import com.keykiosk.Models.DTO.CategoryDTO;
import com.keykiosk.Models.Entity.CategoryEntity;
import com.keykiosk.Models.Entity.ImageEntity;
import com.keykiosk.Models.Entity.UserEntity;
import com.keykiosk.Models.EnumType.ImageType;
import com.keykiosk.Models.EnumType.Status;
import com.keykiosk.Models.Model;
import com.keykiosk.Models.Repository.CategoryRepository;
import com.keykiosk.Models.Repository.ImageRepository;
import com.keykiosk.Observer.CategoryObservable;
import com.keykiosk.Services.CategoryService;
import com.keykiosk.Util.FileUtil;
import jakarta.persistence.criteria.Join;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService {
    private static final Logger logger = LoggerFactory.getLogger(CategoryServiceImpl.class);

    private final CategoryRepository categoryRepository;

    private final ImageRepository imageRepository;
    private final ModelMapper modelMapper;

    @Autowired
    private CategoryObservable categoryObservable;

    @Autowired
    public CategoryServiceImpl(CategoryRepository categoryRepository, ImageRepository imageRepository, ModelMapper modelMapper) {
        this.categoryRepository = categoryRepository;
        this.imageRepository = imageRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public List<CategoryDTO> getAllImagesWithCategory() {
        return categoryRepository.findAll().stream()
                .map(this::convertToDTOWithImage)
                .collect(Collectors.toList());
    }

    @Override
    public void insertImageAndCategory(CategoryDTO dto, File selectedFile) throws IOException {
        UserEntity currentUser = Model.getInstance().getLoggedInUser();
        File destinationFile = FileUtil.prepareDestinationFile(selectedFile, ConfigUrl.BASE_IMAGE_DIR + "/Category");

        CategoryEntity category = createCategoryEntity(dto, currentUser);
        category = categoryRepository.save(category);

        ImageEntity imageEntity = createImageEntity(category, destinationFile, currentUser);
        imageRepository.save(imageEntity);

        Files.copy(selectedFile.toPath(), destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        categoryObservable.notifyObservers();
    }

    @Override
    public void updateImageAndCategory(CategoryDTO dto, File selectedFile) throws IOException {
        CategoryEntity category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + dto.getCategoryId()));

        updateCategoryEntityFromDTO(category, dto);

        if (selectedFile != null) {
            handleImageUpdate(category, selectedFile);
        }

        categoryRepository.save(category);
    }

    @Override
    public void deleteCategory(CategoryDTO dto) {
        CategoryEntity categoryEntity = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + dto.getCategoryId()));

        ImageEntity imageEntity = imageRepository.findByCategoryAndImageType(categoryEntity, ImageType.CATEGORY_IMAGE);
        if (imageEntity != null) {
            deleteExistingImage(imageEntity);
        }
        categoryRepository.delete(categoryEntity);
    }

    @Override
    public List<CategoryDTO> searchCategories(String keyword) {
        Specification<ImageEntity> spec = (root, query, criteriaBuilder) -> {
            Join<ImageEntity, CategoryEntity> categoryJoin = root.join("category");
            return criteriaBuilder.like(categoryJoin.get("name"), "%" + keyword + "%");
        };
        return imageRepository.findAll(spec).stream()
                .map(this::convertImageEntityToCategoryDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void exportDataToExcel(String excelFilePath) throws IOException {
        List<CategoryDTO> categories = getAllImagesWithCategory();
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Categories");
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            CellStyle headerCellStyle = workbook.createCellStyle();
            headerCellStyle.setFont(headerFont);
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Image URL");
            headerRow.createCell(1).setCellValue("Category Name");
            headerRow.createCell(2).setCellValue("Description");
            headerRow.createCell(3).setCellValue("Status");

            for (int i = 0; i < categories.size(); i++) {
                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(categories.get(i).getImageUrl());
                row.createCell(1).setCellValue(categories.get(i).getCategoryName());
                row.createCell(2).setCellValue(categories.get(i).getDescription());
                row.createCell(3).setCellValue(String.valueOf(categories.get(i).getStatus()));
            }

            try (FileOutputStream fileOut = new FileOutputStream(excelFilePath)) {
                workbook.write(fileOut);
            }
        } catch (IOException e) {
            logger.error("Export to Excel failed!", e);
            throw e;
        }
    }

    @Override
    public void importDataFromExcel(String excelFilePath) throws IOException {
        if (excelFilePath == null) {
            throw new IllegalArgumentException("File path cannot be null");
        }

        try (FileInputStream fis = new FileInputStream(excelFilePath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();

            if (rows.hasNext()) {
                rows.next();
            }

            while (rows.hasNext()) {
                Row currentRow = rows.next();
                Cell imageUrlCell = currentRow.getCell(0);
                Cell categoryNameCell = currentRow.getCell(1);
                Cell descriptionCell = currentRow.getCell(2);
                Cell statusCell = currentRow.getCell(3);

                File destinationFile = new File(imageUrlCell.getStringCellValue());

                CategoryDTO categoryDTO = new CategoryDTO();
                categoryDTO.setImageUrl(imageUrlCell.getStringCellValue());
                categoryDTO.setCategoryName(getCellValue(categoryNameCell));
                categoryDTO.setDescription(getCellValue(descriptionCell));
                categoryDTO.setStatus(Status.valueOf(getCellValue(statusCell)));
                categoryDTO.setImageType(ImageType.CATEGORY_IMAGE);

                if (categoryRepository.findAllByName(categoryDTO.getCategoryName()).isEmpty()) {
                    insertImageAndCategory(categoryDTO, destinationFile);
                }
            }

            logger.info("Imported categories from Excel successfully!");
        } catch (IOException e) {
            logger.error("Import from Excel failed!", e);
            throw e;
        }
    }

    @Override
    public CategoryDTO getCategoryByName(String categoryName) {
        CategoryEntity categoryEntity = categoryRepository.findByName(categoryName);
        return convertToDTO(categoryEntity);
    }

    @Override
    public List<CategoryDTO> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private CategoryDTO convertToDTO(CategoryEntity categoryEntity) {
        return modelMapper.map(categoryEntity, CategoryDTO.class);
    }

    private CategoryDTO convertToDTOWithImage(CategoryEntity categoryEntity) {
        CategoryDTO categoryDTO = convertToDTO(categoryEntity);
        ImageEntity imageEntity = imageRepository.findByCategoryAndImageType(categoryEntity, ImageType.CATEGORY_IMAGE);
        if (imageEntity != null) {
            categoryDTO.setImageUrl(imageEntity.getImageUrl());
            categoryDTO.setImageId(imageEntity.getImageId());
        }
        return categoryDTO;
    }

    private CategoryDTO convertImageEntityToCategoryDTO(ImageEntity imageEntity) {
        CategoryDTO categoryDTO = new CategoryDTO();
        categoryDTO.setImageId(imageEntity.getImageId());
        categoryDTO.setImageUrl(imageEntity.getImageUrl());
        categoryDTO.setImageType(imageEntity.getImageType());
        if (imageEntity.getCategory() != null) {
            categoryDTO.setCategoryId(imageEntity.getCategory().getCategoryId());
            categoryDTO.setCategoryName(imageEntity.getCategory().getName());
            categoryDTO.setDescription(imageEntity.getCategory().getDescription());
            categoryDTO.setStatus(imageEntity.getCategory().getStatus());
        }
        return categoryDTO;
    }

    private void updateCategoryEntityFromDTO(CategoryEntity category, CategoryDTO dto) {
        category.setName(dto.getCategoryName());
        category.setDescription(dto.getDescription());
        category.setStatus(dto.getStatus());
    }

    private void handleImageUpdate(CategoryEntity category, File selectedFile) throws IOException {
        ImageEntity existingImageEntity = imageRepository.findByCategoryAndImageType(category, ImageType.CATEGORY_IMAGE);
        if (existingImageEntity != null) {
            deleteExistingImage(existingImageEntity);
        }

        File destinationFile = FileUtil.prepareDestinationFile(selectedFile, ConfigUrl.BASE_IMAGE_DIR + "/Category");
        ImageEntity newImageEntity = createImageEntity(category, destinationFile, Model.getInstance().getLoggedInUser());
        imageRepository.save(newImageEntity);
        Files.copy(selectedFile.toPath(), destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    private void deleteExistingImage(ImageEntity existingImageEntity) {
        String existingImageUrl = existingImageEntity.getImageUrl();
        File existingImageFile = new File(existingImageUrl);
        if (existingImageFile.exists() && !existingImageFile.isDirectory() && !existingImageFile.delete()) {
            logger.error("Failed to delete image file: {}", existingImageUrl);
        }
        imageRepository.delete(existingImageEntity);
    }

    private ImageEntity createImageEntity(CategoryEntity category, File destinationFile, UserEntity currentUser) {
        return ImageEntity.builder()
                .imageUrl(destinationFile.getPath())
                .imageType(ImageType.CATEGORY_IMAGE)
                .category(category)
                .account(currentUser)
                .build();
    }

    private CategoryEntity createCategoryEntity(CategoryDTO dto, UserEntity currentUser) {
        return CategoryEntity.builder()
                .name(dto.getCategoryName())
                .description(dto.getDescription())
                .status(dto.getStatus())
                .account(currentUser)
                .build();
    }


    private String getCellValue(Cell cell) {
        if (cell.getCellType() == CellType.STRING) {
            return cell.getStringCellValue();
        } else if (cell.getCellType() == CellType.NUMERIC) {
            return String.valueOf(cell.getNumericCellValue());
        } else {
            return "";
        }
    }
}
