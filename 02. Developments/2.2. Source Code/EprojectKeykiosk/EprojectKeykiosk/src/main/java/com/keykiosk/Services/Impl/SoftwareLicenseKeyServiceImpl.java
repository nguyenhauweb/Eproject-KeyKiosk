package com.keykiosk.Services.Impl;

import com.keykiosk.Models.DTO.SoftwareAccountDTO;
import com.keykiosk.Models.DTO.SoftwareLicenseKeyDTO;
import com.keykiosk.Models.Entity.SoftwareAccountEntity;
import com.keykiosk.Models.Entity.SoftwareLicenseKeyEntity;
import com.keykiosk.Models.EnumType.PaymentStatus;
import com.keykiosk.Models.EnumType.ProductTypeCode;
import com.keykiosk.Models.EnumType.Status;
import com.keykiosk.Models.Model;
import com.keykiosk.Models.Repository.SoftwareLicenseKeyRepository;
import com.keykiosk.Observer.ProductObservable;
import com.keykiosk.Services.ProductService;
import com.keykiosk.Services.SoftwareLicenseKeyService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import static com.keykiosk.Util.RandomCodeUtil.generateUniqueRandomCode;

@Service
public class SoftwareLicenseKeyServiceImpl implements SoftwareLicenseKeyService {
    @Autowired
    private SoftwareLicenseKeyRepository softwareLicenseKeyRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductObservable productObservable;

    @Override
    public List<SoftwareLicenseKeyDTO> getAllSoftwareLicenseKeys() {
        List<SoftwareLicenseKeyEntity> softwareLicenseKeyEntities = softwareLicenseKeyRepository.findAllSortedByCreationDate();
        return softwareLicenseKeyEntities.stream()
                .filter(key -> !key.isDeleted()) // Only get keys that are not deleted
                .map(softwareLicenseKeys -> modelMapper.map(softwareLicenseKeys, SoftwareLicenseKeyDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public void deleteKey(SoftwareLicenseKeyDTO softwareLicenseKeyDTO) {
        SoftwareLicenseKeyEntity entity = softwareLicenseKeyRepository.findById(softwareLicenseKeyDTO.getLicenseId())
                .orElseThrow(() -> new RuntimeException("Key not found"));
        entity.setDeleted(true);
        softwareLicenseKeyRepository.save(entity);
        productObservable.notifyProductObservers();
    }

    @Override
    public SoftwareLicenseKeyDTO createSoftwareLicenseKey(SoftwareLicenseKeyDTO softwareLicenseKeyDTO) {
        SoftwareLicenseKeyEntity softwareLicenseKeyEntity = modelMapper.map(softwareLicenseKeyDTO, SoftwareLicenseKeyEntity.class);
        return convertToDTO(softwareLicenseKeyRepository.save(softwareLicenseKeyEntity));
    }

    private SoftwareLicenseKeyDTO convertToDTO(SoftwareLicenseKeyEntity softwareLicenseKeyEntity) {
        return modelMapper.map(softwareLicenseKeyEntity, SoftwareLicenseKeyDTO.class);
    }

    @Override
    public void updateSoftwareLicenseKey(SoftwareLicenseKeyDTO selectedKey) {
        SoftwareLicenseKeyEntity softwareLicenseKeyEntity = modelMapper.map(selectedKey, SoftwareLicenseKeyEntity.class);
        softwareLicenseKeyRepository.save(softwareLicenseKeyEntity);
    }

    @Override
    public List<SoftwareLicenseKeyDTO> searchKeys(String keyword) {
        return List.of();
    }

    @Override
    public List<SoftwareLicenseKeyDTO> findByProduct_NameProducts(String nameProduct) {
        List<SoftwareLicenseKeyEntity> keyEntities = softwareLicenseKeyRepository.findByProduct_NameProductAndDeletedFalse(nameProduct);
        List<SoftwareLicenseKeyDTO> keyDTOS = keyEntities.stream()
                .map(keyEntity -> modelMapper.map(keyEntity, SoftwareLicenseKeyDTO.class))
                .collect(Collectors.toList());

        return keyDTOS;
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
                Cell licenseKeyCell = currentRow.getCell(0);
                Cell productNameCell = currentRow.getCell(1);

                if (softwareLicenseKeyRepository.findAllByLicenseKey(licenseKeyCell.getStringCellValue()).isEmpty()) {
                    createSoftwareLicenseKey(SoftwareLicenseKeyDTO.builder()
                            .licenseKey(licenseKeyCell.getStringCellValue())
                            .accountId(Model.getInstance().getLoggedInUser().getAccountId())
                            .productId(productService.getProductByName(productNameCell.getStringCellValue()).getProductId())
                            .codeId(generateUniqueRandomCode())
                            .paymentStatus(PaymentStatus.PENDING)
                            .status(Status.ACTIVE)
                            .build());
                }
            }
        } catch (IOException e) {
            throw e;
        }
    }

    @Override
    public void exportDataToExcel(String excelFilePath) throws IOException {
        List<SoftwareLicenseKeyDTO> softwareLicenseKeyDTOS = getAllSoftwareLicenseKeys();
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Software License Key");
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            CellStyle headerCellStyle = workbook.createCellStyle();
            headerCellStyle.setFont(headerFont);
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("License Key");
            headerRow.createCell(1).setCellValue("Product Name");

            for (int i = 0; i < softwareLicenseKeyDTOS.size(); i++) {
                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(softwareLicenseKeyDTOS.get(i).getLicenseKey());
                row.createCell(1).setCellValue(softwareLicenseKeyDTOS.get(i).getNameProduct());
            }

            try (FileOutputStream fileOut = new FileOutputStream(excelFilePath)) {
                workbook.write(fileOut);
            }
        } catch (IOException e) {
            throw e;
        }
    }

    @Override
    public List<SoftwareLicenseKeyDTO> findKeysWithMinIdByProduct(String productName, int count) {
        List<SoftwareLicenseKeyEntity> keysWithMinId = softwareLicenseKeyRepository.findByProduct_NameProductOrderByLicenseIdAsc(productName);
        return keysWithMinId.stream()
                .filter(key -> !key.isDeleted())
                .limit(count)
                .map(key -> modelMapper.map(key, SoftwareLicenseKeyDTO.class))
                .collect(Collectors.toList());
    }

    public void updateKey(SoftwareLicenseKeyDTO keyDTO) {
        SoftwareLicenseKeyEntity keyEntity = convertToEntity(keyDTO);
        softwareLicenseKeyRepository.save(keyEntity);
    }

    private SoftwareLicenseKeyEntity convertToEntity(SoftwareLicenseKeyDTO keyDTO) {
        return modelMapper.map(keyDTO, SoftwareLicenseKeyEntity.class);
    }
}
