package com.keykiosk.Services;

import com.keykiosk.Models.DTO.CustomerDTO;

import java.util.List;

public interface CustomerService {
    List<CustomerDTO> getAllCustomers();
    void deleteCustomer(CustomerDTO customerDTO);
    CustomerDTO createCustomer(CustomerDTO customerDTO);
    void updateCustomer(CustomerDTO customerDTO);
    List<CustomerDTO> searchCustomers(String keyword);

    CustomerDTO findByEmail(String email); // Add this line
}
