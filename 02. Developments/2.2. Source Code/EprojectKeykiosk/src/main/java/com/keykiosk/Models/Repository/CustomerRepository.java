package com.keykiosk.Models.Repository;

import com.keykiosk.Models.Entity.CustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<CustomerEntity, Long>{

    CustomerEntity findByEmail(String email);
}
