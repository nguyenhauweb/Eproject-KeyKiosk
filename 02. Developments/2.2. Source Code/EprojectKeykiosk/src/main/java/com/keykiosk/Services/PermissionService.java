package com.keykiosk.Services;

import com.keykiosk.Models.DTO.PermissionDTO;
import com.keykiosk.Models.Entity.UserEntity;
import com.keykiosk.Models.EnumType.PermissionType;

import java.util.List;

public interface PermissionService {
    List<PermissionDTO> getPermissionsByUser(UserEntity user);
    void savePermissions(UserEntity user, List<PermissionDTO> permissionDTOs);
    void deleteByPermissionTypeAndAccount(PermissionType type, UserEntity userEntity);
    boolean permissionExists(PermissionDTO permissionDTO, UserEntity userEntity);
}