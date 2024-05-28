package com.keykiosk.Services.Impl;

import com.keykiosk.Models.DTO.*;
import com.keykiosk.Models.Entity.ProductEntity;
import com.keykiosk.Models.Entity.SoftwareAccountEntity;
import com.keykiosk.Models.Entity.SoftwareLicenseKeyEntity;
import com.keykiosk.Models.Entity.UserEntity;
import com.keykiosk.Models.EnumType.ImageType;
import com.keykiosk.Models.EnumType.PaymentStatus;
import com.keykiosk.Models.EnumType.Status;
import com.keykiosk.Models.Model;
import com.keykiosk.Models.Repository.SoftwareAccountRepository;
import com.keykiosk.Observer.ProductObservable;
import com.keykiosk.Services.ProductService;
import com.keykiosk.Services.SoftwareAccountService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import static com.keykiosk.Util.RandomCodeUtil.generateUniqueRandomCode;

@Service
public class SoftwareAccountServiceImpl implements SoftwareAccountService {

    @Autowired
    private SoftwareAccountRepository softwareAccountRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductObservable productObservable;

    @Override
    public List<SoftwareAccountDTO> getAllSoftwareAccounts() {
        List<SoftwareAccountEntity> softwareAccountEntities = softwareAccountRepository.findAllSortedByCreationDate();
        return softwareAccountEntities.stream()
                .filter(key -> !key.isDeleted())
                .map(softwareAccounts -> modelMapper.map(softwareAccounts, SoftwareAccountDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public void deleteSoftwareAccountDTO(SoftwareAccountDTO softwareAccountDTO) {
        SoftwareAccountEntity entity = softwareAccountRepository.findById(softwareAccountDTO.getSoftwareAccountId()).orElseThrow(() -> new RuntimeException("Account not found"));
        entity.setDeleted(true);
        softwareAccountRepository.save(entity);
        productObservable.notifyProductObservers();
    }

    @Override
    public SoftwareAccountDTO createSoftwareAccount(SoftwareAccountDTO softwareAccountDTO) {
        SoftwareAccountEntity softwareAccountEntity = modelMapper.map(softwareAccountDTO, SoftwareAccountEntity.class);
        return convertToDTO(softwareAccountRepository.save(softwareAccountEntity));
    }
    private SoftwareAccountDTO convertToDTO(SoftwareAccountEntity softwareAccountEntity) {
        return modelMapper.map(softwareAccountEntity, SoftwareAccountDTO.class);
    }
    @Override
    public void updateSoftwareAccount(SoftwareAccountDTO softwareAccountDTO) {
        SoftwareAccountEntity softwareAccountEntity = modelMapper.map(softwareAccountDTO, SoftwareAccountEntity.class);
        softwareAccountRepository.save(softwareAccountEntity);
    }

    @Override
    public List<SoftwareAccountDTO> searchsoftwareAccount(String keyword) {
        List<SoftwareAccountEntity> softwareAccountEntities = softwareAccountRepository.findAllByAccountInfoContaining(keyword);
        return softwareAccountEntities.stream()
                .map(userEntity -> modelMapper.map(userEntity, SoftwareAccountDTO.class))
                .collect(Collectors.toList());
    }


    @Override
    public void exportDataToExcel(String excelFilePath) throws IOException {
        List<SoftwareAccountDTO> softwareAccounts = getAllSoftwareAccounts();
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Software Account");
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            CellStyle headerCellStyle = workbook.createCellStyle();
            headerCellStyle.setFont(headerFont);
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Account Info");
            headerRow.createCell(1).setCellValue("Product Name");

            for (int i = 0; i < softwareAccounts.size(); i++) {
                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(softwareAccounts.get(i).getAccountInfo());
                row.createCell(1).setCellValue(softwareAccounts.get(i).getNameProduct());
            }

            try (FileOutputStream fileOut = new FileOutputStream(excelFilePath)) {
                workbook.write(fileOut);
            }
        } catch (IOException e) {
            throw e;
        }
    }

    @Override
    public List<SoftwareAccountDTO> findByProduct_NameProduct(String nameProduct) {
        List<SoftwareAccountEntity> softwareAccountEntities = softwareAccountRepository.findByProduct_NameProductAndDeletedFalse(nameProduct);
        List<SoftwareAccountDTO> softwareAccountDTOS = softwareAccountEntities.stream()
                .map(userEntity -> modelMapper.map(userEntity, SoftwareAccountDTO.class))
                .collect(Collectors.toList());
        return softwareAccountDTOS;
    }



    @Override
    public List<SoftwareAccountDTO> findAccountsWithMinIdByProduct(String productName, int count) {
        List<SoftwareAccountEntity> accountsWithMinId = softwareAccountRepository.findByProduct_NameProductOrderBySoftwareAccountIdAsc(productName);
        return accountsWithMinId.stream()
                .filter(account -> !account.isDeleted())
                .limit(count)
                .map(account -> modelMapper.map(account, SoftwareAccountDTO.class))
                .collect(Collectors.toList());
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
                Cell accountInfoCell = currentRow.getCell(0);
                Cell productNameCell = currentRow.getCell(1);
                if (softwareAccountRepository.findAllByAccountInfo(accountInfoCell.getStringCellValue()).isEmpty()) {
                    createSoftwareAccount(SoftwareAccountDTO.builder()
                            .accountInfo(accountInfoCell.getStringCellValue())
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

}
