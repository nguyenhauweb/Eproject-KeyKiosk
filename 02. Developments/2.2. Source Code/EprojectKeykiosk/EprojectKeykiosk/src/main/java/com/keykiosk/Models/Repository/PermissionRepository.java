package com.keykiosk.Models.Repository;

import com.keykiosk.Models.Entity.PermissionEntity;
import com.keykiosk.Models.Entity.UserEntity;
import com.keykiosk.Models.EnumType.PermissionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PermissionRepository extends JpaRepository<PermissionEntity, Long> {
    List<PermissionEntity> findByAccount(UserEntity user);

    List<PermissionEntity> findByPermissionTypeAndAccount(PermissionType type, UserEntity userEntity);
}