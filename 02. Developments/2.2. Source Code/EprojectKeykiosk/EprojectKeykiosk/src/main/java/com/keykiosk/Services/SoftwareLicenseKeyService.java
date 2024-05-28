package com.keykiosk.Services;

import com.keykiosk.Models.DTO.SoftwareLicenseKeyDTO;

import java.io.IOException;
import java.util.List;

public interface SoftwareLicenseKeyService {
    List<SoftwareLicenseKeyDTO> getAllSoftwareLicenseKeys();
    void deleteKey(SoftwareLicenseKeyDTO softwareLicenseKeyDTO);
    SoftwareLicenseKeyDTO createSoftwareLicenseKey(SoftwareLicenseKeyDTO softwareLicenseKeyDTO);
    void updateSoftwareLicenseKey(SoftwareLicenseKeyDTO selectedKey);
    List<SoftwareLicenseKeyDTO> searchKeys(String keyword);

    List<SoftwareLicenseKeyDTO> findByProduct_NameProducts(String nameProduct);

    void importDataFromExcel(String excelFilePath) throws IOException;

    void exportDataToExcel(String excelFilePath) throws IOException;


    List<SoftwareLicenseKeyDTO> findKeysWithMinIdByProduct(String nameProduct, int count);

    void updateKey(SoftwareLicenseKeyDTO key);
}
