package com.keykiosk.Models.Repository;

import com.keykiosk.Models.Entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long>, JpaSpecificationExecutor<UserEntity> {
    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    UserEntity findByUsername(String username);

    UserEntity findByEmail(String email);


}
