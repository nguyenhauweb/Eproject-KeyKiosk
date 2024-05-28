package com.keykiosk.Services.Impl;

import com.keykiosk.Models.DTO.PermissionDTO;
import com.keykiosk.Models.Entity.PermissionEntity;
import com.keykiosk.Models.Entity.UserEntity;
import com.keykiosk.Models.EnumType.PermissionType;
import com.keykiosk.Models.Repository.PermissionRepository;
import com.keykiosk.Observer.PermissionObservable;
import com.keykiosk.Services.PermissionService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PermissionServiceImpl implements PermissionService {
    private final PermissionRepository permissionRepository;
    private final ModelMapper modelMapper;
    @Autowired
    private TransactionTemplate transactionTemplate;
    @Autowired
    private PermissionObservable permissionObservable;

    public PermissionServiceImpl(PermissionRepository permissionRepository, ModelMapper modelMapper) {
        this.permissionRepository = permissionRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public List<PermissionDTO> getPermissionsByUser(UserEntity user) {
        List<PermissionEntity> permissionEntities = permissionRepository.findByAccount(user);
        return permissionEntities.stream()
                .map(entity -> modelMapper.map(entity, PermissionDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public void savePermissions(UserEntity user, List<PermissionDTO> permissionDTOs) {
        List<PermissionEntity> permissionEntities = permissionDTOs.stream()
                .map(dto -> modelMapper.map(dto, PermissionEntity.class))
                .collect(Collectors.toList());
        permissionEntities.forEach(entity -> entity.setAccount(user));
        permissionRepository.saveAll(permissionEntities);
        permissionObservable.notifyObservers();
    }

    @Override
    public void deleteByPermissionTypeAndAccount(PermissionType type, UserEntity userEntity) {
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                List<PermissionEntity> permissionsToDelete = permissionRepository.findByPermissionTypeAndAccount(type, userEntity);
                permissionRepository.deleteAll(permissionsToDelete);
            }
        });
    }

    @Override
    public boolean permissionExists(PermissionDTO permissionDTO, UserEntity userEntity) {
        PermissionType type = PermissionType.valueOf(permissionDTO.getPermissionType());
        List<PermissionEntity> existingPermissions = permissionRepository.findByPermissionTypeAndAccount(type, userEntity);
        return !existingPermissions.isEmpty();
    }
}