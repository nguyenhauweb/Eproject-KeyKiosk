package com.keykiosk.Services.Impl;

import com.keykiosk.Models.DTO.CustomerDTO;
import com.keykiosk.Models.Entity.CustomerEntity;
import com.keykiosk.Models.Repository.CustomerRepository;
import com.keykiosk.Services.CustomerService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomerServiceImpl implements CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public List<CustomerDTO> getAllCustomers() {
        List<CustomerEntity> customerEntities = customerRepository.findAll();
        return customerEntities.stream()
                .map(customerEntity -> modelMapper.map(customerEntity, CustomerDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public void deleteCustomer(CustomerDTO customerDTO) {
        CustomerEntity customerEntity = customerRepository.findByEmail(customerDTO.getEmail());
        if (customerEntity != null) {
            customerRepository.delete(customerEntity);
        }
    }

    @Override
    public CustomerDTO createCustomer(CustomerDTO customerDTO) {
        CustomerEntity customerEntity = customerRepository.findByEmail(customerDTO.getEmail());
        if (customerEntity == null) {
            customerEntity = modelMapper.map(customerDTO, CustomerEntity.class);
            customerRepository.save(customerEntity);
            return convertToDTO(customerEntity);
        }
        return convertToDTO(customerEntity);
    }

    @Override
    public void updateCustomer(CustomerDTO customerDTO) {
        CustomerEntity customerEntity = customerRepository.findByEmail(customerDTO.getEmail());
        if (customerEntity != null) {
            // Update other fields except email
            customerEntity.setFullName(customerDTO.getFullName());
            customerEntity.setGender(customerDTO.getGender());
            customerEntity.setPhoneNumber(customerDTO.getPhoneNumber());
            customerEntity.setAddress(customerDTO.getAddress());
            customerRepository.save(customerEntity);
        }
    }

    @Override
    public List<CustomerDTO> searchCustomers(String keyword) {
//        // Assuming there's a method in the repository for searching customers
//        List<CustomerEntity> customerEntities = customerRepository.searchByKeyword(keyword);
//        return customerEntities.stream()
//                .map(customerEntity -> modelMapper.map(customerEntity, CustomerDTO.class))
//                .collect(Collectors.toList());
        return null;
    }

    @Override
    public CustomerDTO findByEmail(String email) {
        CustomerEntity customerEntity = customerRepository.findByEmail(email);
        return customerEntity != null ? modelMapper.map(customerEntity, CustomerDTO.class) : null;
    }

    private CustomerDTO convertToDTO(CustomerEntity customerEntity) {
        return modelMapper.map(customerEntity, CustomerDTO.class);
    }
}
