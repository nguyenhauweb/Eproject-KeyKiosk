package com.keykiosk.Util;
import com.keykiosk.Models.Entity.CategoryEntity;
import com.keykiosk.Models.Entity.ImageEntity;
import com.keykiosk.Models.Entity.ProductEntity;
import com.keykiosk.Models.Entity.UserEntity;
import com.keykiosk.Models.EnumType.ImageType;
import com.keykiosk.Util.FileUtil;

import java.io.File;
import java.io.IOException;

public class ImageUtils {

    public static ImageEntity createImageEntity(File destinationFile, UserEntity currentUser, Object relatedEntity, ImageType imageType) {
        ImageEntity.ImageEntityBuilder builder = ImageEntity.builder()
                .imageUrl(destinationFile.getPath())
                .imageType(imageType)
                .account(currentUser);

        if (relatedEntity instanceof CategoryEntity) {
            builder.category((CategoryEntity) relatedEntity);
        } else if (relatedEntity instanceof ProductEntity) {
            builder.product((ProductEntity) relatedEntity);
        }

        return builder.build();
    }

    public static void updateImageEntity(ImageEntity existingImageEntity, File selectedFile) throws IOException {


//        if (existingImageEntity != null) {
//            FileUtil.deleteFile(new File(existingImageEntity.getImageUrl()));
//        }
//
//        File destinationFile = FileUtil.prepareDestinationFile(selectedFile, "src/main/resources/Images/Category");
//        FileUtil.copyFile(selectedFile, destinationFile);
//        existingImageEntity.setImageUrl(destinationFile.getPath());
    }
}
