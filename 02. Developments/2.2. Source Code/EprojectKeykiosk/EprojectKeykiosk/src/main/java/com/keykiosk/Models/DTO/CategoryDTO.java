package com.keykiosk.Models.DTO;

import com.keykiosk.Models.Entity.ImageEntity;
import com.keykiosk.Models.Entity.UserEntity;
import com.keykiosk.Models.EnumType.ImageType;
import com.keykiosk.Models.EnumType.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryDTO implements Serializable {
    private Long imageId;
    private String imageUrl;
    private ImageType imageType;
    private Long categoryId;
    private String categoryName;
    private String description;
    private Status status;
    private UserEntity accountId;

}