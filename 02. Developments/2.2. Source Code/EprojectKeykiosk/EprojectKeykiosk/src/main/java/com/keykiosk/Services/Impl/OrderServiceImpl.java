package com.keykiosk.Services.Impl;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.keykiosk.Models.DTO.OrderDTO;
import com.keykiosk.Models.DTO.SoftwareAccountDTO;
import com.keykiosk.Models.DTO.SoftwareLicenseKeyDTO;
import com.keykiosk.Models.Entity.OrderEntity;
import com.keykiosk.Models.Entity.SoftwareAccountEntity;
import com.keykiosk.Models.Entity.SoftwareLicenseKeyEntity;
import com.keykiosk.Models.EnumType.ProductTypeCode;
import com.keykiosk.Models.Repository.OrderRepository;
import com.keykiosk.Models.Repository.SoftwareAccountRepository;
import com.keykiosk.Models.Repository.SoftwareLicenseKeyRepository;
import com.keykiosk.Services.OrderService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private SoftwareAccountRepository softwareAccountRepository;
    @Autowired
    private SoftwareLicenseKeyRepository softwareLicenseKeyRepository;
    @Autowired
    private ModelMapper modelMapper;

    @Override
    public List<OrderDTO> getAllOrders() {
        List<OrderEntity> orderEntities = orderRepository.findAll();
        return orderEntities.stream()
                .map(orderEntity -> modelMapper.map(orderEntity, OrderDTO.class))
                .collect(Collectors.toList());
    }



    @Override
    public OrderDTO createOrder(OrderDTO order) {
        OrderEntity orderEntity = modelMapper.map(order, OrderEntity.class);
        return convertToDTO(orderRepository.save(orderEntity));
    }


    private OrderDTO convertToDTO(OrderEntity orderEntity) {
        return modelMapper.map(orderEntity, OrderDTO.class);
    }

    @Override
    public void deleteOrder(OrderDTO orderDTO) {
        orderRepository.deleteById(orderDTO.getOrderId());
    }

    @Override
    public void updateOrder(OrderDTO orderDTO) {
        OrderEntity orderEntity = modelMapper.map(orderDTO, OrderEntity.class);
        orderRepository.save(orderEntity);
    }


    @Override
    public void exportOrderToPDF(OrderDTO order, String pdfFilePath, List<SoftwareAccountDTO> selectedAccounts, List<SoftwareLicenseKeyDTO> selectedKeys) throws IOException {
        PdfWriter writer = new PdfWriter(pdfFilePath);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);
        float indent = 50;
        float indentTk = 170;
        document.add(new Paragraph("--------------------------------------------------KeyKiosk Order Details-------------------------------------------------"));
        document.add(new Paragraph("Order ID: " + order.getCodeId()).setFirstLineIndent(indent));
        document.add(new Paragraph("Product: " + order.getNameProduct()).setFirstLineIndent(indent));
        document.add(new Paragraph("Quantity: " + order.getQuantity()).setFirstLineIndent(indent));
        document.add(new Paragraph("Total Amount: " + order.getTotalAmount()).setFirstLineIndent(indent));
        document.add(new Paragraph("Payment Method: " + order.getPaymentMethod()).setFirstLineIndent(indent));
        if (order.getProductType() == ProductTypeCode.ACCOUNT) {
            for (SoftwareAccountDTO account : selectedAccounts) {
                document.add(new Paragraph("Account Info: " + account.getAccountInfo()).setFirstLineIndent(indent));
            }
        } else if (order.getProductType() == ProductTypeCode.KEY) {
            for (SoftwareLicenseKeyDTO key : selectedKeys) {
                document.add(new Paragraph("License Key: " + key.getLicenseKey()).setFirstLineIndent(indent));
            }
        }
        document.add(new Paragraph("Order Date: " + order.getOrderDateFormatted()).setFirstLineIndent(indent));
        document.add(new Paragraph("-------------------------------------------------------------------------------------------------------------------------\n"));
        document.add(new Paragraph("Thank you shopping with KeyKiosk!").setFirstLineIndent(indentTk));
        document.close();
    }

    @Override
    public List<SoftwareAccountDTO> getDeletedAccountsByProductIdAndQuantity(Long productId, Integer quantity) {
        List<SoftwareAccountEntity> accountEntities = softwareAccountRepository.findByProduct_ProductIdAndDeleted(productId, true);
        return accountEntities.stream()
                .limit(quantity)
                .map(accountEntity -> modelMapper.map(accountEntity, SoftwareAccountDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<SoftwareLicenseKeyDTO> getDeletedKeysByProductIdAndQuantity(Long productId, Integer quantity) {
        List<SoftwareLicenseKeyEntity> keyEntities = softwareLicenseKeyRepository.findByProduct_ProductIdAndDeleted(productId, true);
        return keyEntities.stream()
                .limit(quantity)
                .map(keyEntity -> modelMapper.map(keyEntity, SoftwareLicenseKeyDTO.class))
                .collect(Collectors.toList());
    }



}
