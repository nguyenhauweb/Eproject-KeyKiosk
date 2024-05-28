package com.keykiosk.Models.DTO;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import lombok.Data;

@Data
public class PermissionDTO {
    private Long permissionId;
    private String permissionType;
    private UserDTO account;
    private BooleanProperty selected;
    public PermissionDTO() {
        this.selected = new SimpleBooleanProperty(false);
    }

    public PermissionDTO(Long id, String permissionType, Boolean selected) {
        this.permissionId = id;
        this.permissionType = permissionType;
        this.selected = new SimpleBooleanProperty(selected != null ? selected : false);
    }

}