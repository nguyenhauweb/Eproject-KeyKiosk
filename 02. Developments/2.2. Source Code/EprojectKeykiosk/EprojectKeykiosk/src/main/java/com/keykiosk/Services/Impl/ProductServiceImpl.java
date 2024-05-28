package com.keykiosk.Services.Impl;

import com.keykiosk.Config.ConfigUrl;
import com.keykiosk.Models.DTO.CategoryDTO;
import com.keykiosk.Models.DTO.ProductDTO;
import com.keykiosk.Models.DTO.SoftwareLicenseKeyDTO;
import com.keykiosk.Models.Entity.CategoryEntity;
import com.keykiosk.Models.Entity.ImageEntity;
import com.keykiosk.Models.Entity.ProductEntity;
import com.keykiosk.Models.Entity.UserEntity;
import com.keykiosk.Models.EnumType.ImageType;
import com.keykiosk.Models.EnumType.ProductTypeCode;
import com.keykiosk.Models.Model;
import com.keykiosk.Models.Repository.ImageRepository;
import com.keykiosk.Models.Repository.ProductRepository;
import com.keykiosk.Models.Repository.SoftwareAccountRepository;
import com.keykiosk.Models.Repository.SoftwareLicenseKeyRepository;
import com.keykiosk.Services.ProductService;
import com.keykiosk.Util.FileUtil;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.checkerframework.checker.index.qual.PolyUpperBound;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.plaf.PanelUI;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductServiceImpl.class);

    @Autowired
    private SoftwareAccountRepository softwareAccountRepository;
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private SoftwareLicenseKeyRepository softwareLicenseKeyRepository;


    @Override
    public List<ProductDTO> getAllProducts() {
        List<ProductDTO> products = productRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return products;
    }

    @Override
    public List<ProductDTO> findByCategoryId(Long categoryId) {
        return productRepository.findByCategory_CategoryId(categoryId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }


    @Override
    public ProductDTO getProductByName(String productName) {
        ProductEntity productEntity = productRepository.findByNameProduct(productName);
        return convertToDTO(productEntity);
    }

    @Override
    public void exportDataToExcel(String excelFilePath) throws IOException {

        List<ProductDTO> productDTOS = getAllProducts();
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Product");
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            CellStyle headerCellStyle = workbook.createCellStyle();
            headerCellStyle.setFont(headerFont);
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Product Name");
            headerRow.createCell(1).setCellValue("Price");
            headerRow.createCell(2).setCellValue("Quantity");
            headerRow.createCell(3).setCellValue("Product Type");
            headerRow.createCell(4).setCellValue("Category");
            headerRow.createCell(5).setCellValue("Status");

            for (int i = 0; i < productDTOS.size(); i++) {
                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(productDTOS.get(i).getNameProduct());
                row.createCell(1).setCellValue(String.valueOf(productDTOS.get(i).getPrice()));
                if (productDTOS.get(i).getProductType() == ProductTypeCode.KEY) {
                    int count = getSoftwareLicenseKeyCountByNameProduct(productDTOS.get(i).getNameProduct());
                    row.createCell(2).setCellValue(count);
                } else if (productDTOS.get(i).getProductType() == ProductTypeCode.ACCOUNT) {
                    int count = getSoftwareAccountCountByNameProduct(productDTOS.get(i).getNameProduct());
                    row.createCell(2).setCellValue(count);
                }
                row.createCell(3).setCellValue(productDTOS.get(i).getProductType().toString());
                row.createCell(4).setCellValue(productDTOS.get(i).getCategoryName());
                row.createCell(5).setCellValue(String.valueOf(productDTOS.get(i).getStatus()));
            }

            try (FileOutputStream fileOut = new FileOutputStream(excelFilePath)) {
                workbook.write(fileOut);
            }
        } catch (IOException e) {
            throw e;
        }

    }

    @Override
    public ProductDTO getProductById(Long productId) {
        Optional<ProductEntity> productEntity = productRepository.findById(productId);
        return productEntity.map(this::convertToDTO).orElse(null);
    }

    @Override
    public List<ProductDTO> getAllProductsWithImages() {
        List<ProductDTO> products = productRepository.findAll().stream()
                .map(this::convertToDTOWithImage)
                .collect(Collectors.toList());

        for (ProductDTO product : products) {
            if (product.getProductType() == ProductTypeCode.KEY) {
                int count = getSoftwareLicenseKeyCountByNameProduct(product.getNameProduct());
                product.setQuantity(count);
            } else if (product.getProductType() == ProductTypeCode.ACCOUNT) {
                int count = getSoftwareAccountCountByNameProduct(product.getNameProduct());
                product.setQuantity(count);
            }
        }

        return products;
    }

    @Override
    public ProductDTO createProduct(ProductDTO productDTO) {
        ProductEntity productEntity = modelMapper.map(productDTO, ProductEntity.class);
        return convertToDTO(productRepository.save(productEntity));
    }


    @Override
    public ProductDTO updateImageAndProduct(ProductDTO dto, File selectedFile) throws IOException {
        ProductEntity product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + dto.getProductId()));

        updateProductEntityFromDTO(product, dto);

        if (selectedFile != null) {
            handleImageUpdate(product, selectedFile);
        } else {
            retainExistingImage(dto, product);
        }

        productRepository.save(product);
        return convertToDTO(product);
    }

    @Override
    public void deleteProduct(ProductDTO dto) {
        ProductEntity productEntity = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + dto.getProductId()));

        ImageEntity imageEntity = imageRepository.findByProductAndImageType(productEntity, ImageType.PRODUCT_IMAGE);
        if (imageEntity != null) {
            deleteExistingImage(imageEntity);
        }
        productRepository.delete(productEntity);
    }

    @Override
    public int getSoftwareLicenseKeyCountByNameProduct(String nameProduct) {
        return softwareLicenseKeyRepository.countByNameProduct(nameProduct);
    }

    @Override
    public int getSoftwareAccountCountByNameProduct(String nameProduct) {
        return softwareAccountRepository.countByNameProduct(nameProduct);
    }

    @Override
    public void insertImageAndProduct(ProductDTO dto, File selectedFile) throws Exception {
        UserEntity currentUser = Model.getInstance().getLoggedInUser();
        File destinationFile = FileUtil.prepareDestinationFile(selectedFile, ConfigUrl.BASE_IMAGE_DIR + "/Product");

        ProductEntity product = modelMapper.map(dto, ProductEntity.class);
        product.setAccount(currentUser);
        product = productRepository.save(product);

        ImageEntity imageEntity = createImageEntity(product, destinationFile, currentUser);
        imageRepository.save(imageEntity);

        Files.copy(selectedFile.toPath(), destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    private ProductDTO convertToDTO(ProductEntity productEntity) {
        return modelMapper.map(productEntity, ProductDTO.class);
    }

    private ProductDTO convertToDTOWithImage(ProductEntity productEntity) {
        ProductDTO productDTO = convertToDTO(productEntity);
        ImageEntity imageEntity = imageRepository.findByProductAndImageType(productEntity, ImageType.PRODUCT_IMAGE);
        if (imageEntity != null) {
            productDTO.setImageUrl(imageEntity.getImageUrl());
        }
        return productDTO;
    }

    private void updateProductEntityFromDTO(ProductEntity product, ProductDTO dto) {
        product.setNameProduct(dto.getNameProduct());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setStatus(dto.getStatus());
        product.setProductType(ProductTypeCode.valueOf(String.valueOf(dto.getProductType())));
    }

    private void handleImageUpdate(ProductEntity product, File selectedFile) throws IOException {
        ImageEntity existingImageEntity = imageRepository.findByProductAndImageType(product, ImageType.PRODUCT_IMAGE);
        if (existingImageEntity != null) {
            deleteExistingImage(existingImageEntity);
        }

        File destinationFile = FileUtil.prepareDestinationFile(selectedFile, ConfigUrl.BASE_IMAGE_DIR + "/Product");
        ImageEntity newImageEntity = createImageEntity(product, destinationFile, Model.getInstance().getLoggedInUser());
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

    private void retainExistingImage(ProductDTO dto, ProductEntity product) {
        ImageEntity existingImageEntity = imageRepository.findByProductAndImageType(product, ImageType.PRODUCT_IMAGE);
        if (existingImageEntity != null) {
            dto.setImageUrl(existingImageEntity.getImageUrl());
        }
    }

    private ImageEntity createImageEntity(ProductEntity product, File destinationFile, UserEntity currentUser) {
        return ImageEntity.builder()
                .imageUrl(destinationFile.getPath())
                .imageType(ImageType.PRODUCT_IMAGE)
                .product(product)
                .account(currentUser)
                .build();
    }
}
