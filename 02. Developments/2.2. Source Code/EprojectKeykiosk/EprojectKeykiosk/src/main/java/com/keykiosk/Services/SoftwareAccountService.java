package com.keykiosk.Services;

import com.keykiosk.Models.DTO.SoftwareAccountDTO;

import java.io.IOException;
import java.util.List;

public interface SoftwareAccountService {
    List<SoftwareAccountDTO> getAllSoftwareAccounts();

    void deleteSoftwareAccountDTO(SoftwareAccountDTO softwareAccountDTO);

    SoftwareAccountDTO createSoftwareAccount(SoftwareAccountDTO softwareAccountDTO);

    void updateSoftwareAccount(SoftwareAccountDTO softwareAccountDTO);

    List<SoftwareAccountDTO> searchsoftwareAccount(String keyword);

    void importDataFromExcel(String excelFilePath) throws IOException;

    void exportDataToExcel(String excelFilePath) throws IOException;

    List<SoftwareAccountDTO> findByProduct_NameProduct(String nameProduct);



    List<SoftwareAccountDTO> findAccountsWithMinIdByProduct(String nameProduct, int count);
}