package com.keykiosk.Services;

import com.keykiosk.Models.DTO.OrderDTO;
import com.keykiosk.Models.DTO.SoftwareAccountDTO;
import com.keykiosk.Models.DTO.SoftwareLicenseKeyDTO;

import java.io.IOException;
import java.util.List;

public interface OrderService {
    List<OrderDTO> getAllOrders();
    OrderDTO createOrder(OrderDTO order);
    void deleteOrder(OrderDTO orderDTO);
    void updateOrder(OrderDTO orderDTO);
    void exportOrderToPDF(OrderDTO order, String pdfFilePath, List<SoftwareAccountDTO> selectedAccounts, List<SoftwareLicenseKeyDTO> selectedKeys) throws IOException;

    List<SoftwareAccountDTO> getDeletedAccountsByProductIdAndQuantity(Long productId, Integer quantity);

    List<SoftwareLicenseKeyDTO> getDeletedKeysByProductIdAndQuantity(Long productId, Integer quantity);
}