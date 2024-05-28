package com.keykiosk.Util;

import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import static com.keykiosk.Util.AlertUtil.showAlert;

public class FileUtil {
    private static final Logger logger = LoggerFactory.getLogger(FileUtil.class);

    public static File selectImage(ImageView imageView) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Image File");
        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile == null) {
            return null; // Nếu người dùng bấm "Cancel" hoặc "X", trả về null
        }

        if (selectedFile.length() > 1 * 1024 * 1024) {
            showAlert(Alert.AlertType.ERROR, "Error", "The file is too large. Please select a file that is less than 1MB.");
            return null; // Trả về null nếu file quá lớn
        }

        Image image = new Image(selectedFile.toURI().toString());
        imageView.setImage(image);

        return selectedFile;
    }

    public static File prepareDestinationFile(File selectedFile, String baseDir) throws IOException {
        String imageName = replaceSpacesWithHyphens(selectedFile.getName());
        int dotIndex = imageName.lastIndexOf(".");
        String baseName = dotIndex > 0 ? imageName.substring(0, dotIndex) : imageName;
        String extension = dotIndex > 0 ? imageName.substring(dotIndex) : "";

        File destinationDirectory = new File(baseDir);
        if (!destinationDirectory.exists()) {
            destinationDirectory.mkdirs();
        }

        return getUniqueDestinationFile(destinationDirectory, baseName, extension);
    }

    public static void copyFile(File sourceFile, File destinationFile) throws IOException {
        Files.copy(sourceFile.toPath(), destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    public static String replaceSpacesWithHyphens(String fileName) {
        return fileName.replace(" ", "-");
    }

    public static File getUniqueDestinationFile(File destinationDirectory, String baseName, String extension) {
        int counter = 0;
        File destinationFile;
        do {
            String newImageName = baseName + (counter > 0 ? "_" + counter : "") + extension;
            destinationFile = new File(destinationDirectory, newImageName);
            counter++;
        } while (destinationFile.exists());

        return destinationFile;
    }

    public static void deleteFile(File file) {
        if (file.exists() && !file.isDirectory() && !file.delete()) {
            logger.error("Failed to delete file: {}", file.getPath());
        }
    }
    public class GetPath {
        public static String getPath(String file) {
            File dir = new File("");
            String path = dir.getAbsolutePath() + file;
            return path;
        }
    }
}
