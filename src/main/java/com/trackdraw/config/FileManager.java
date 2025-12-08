package com.trackdraw.config;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Manages file operations for sequences and images.
 * Handles directory creation and relative/absolute path conversion.
 */
public class FileManager {
    private static final String SAVES_DIR = "saves";
    private static final String IMAGES_DIR = "images";
    
    /**
     * Gets the working directory (project root).
     * @return File representing the working directory
     */
    public static File getWorkingDirectory() {
        return new File(System.getProperty("user.dir"));
    }
    
    /**
     * Gets the saves directory, creating it if it doesn't exist.
     * @return File representing the saves directory
     */
    public static File getSavesDirectory() {
        File savesDir = new File(getWorkingDirectory(), SAVES_DIR);
        if (!savesDir.exists()) {
            savesDir.mkdirs();
        }
        return savesDir;
    }
    
    /**
     * Gets the images directory, creating it if it doesn't exist.
     * @return File representing the images directory
     */
    public static File getImagesDirectory() {
        File imagesDir = new File(getWorkingDirectory(), IMAGES_DIR);
        if (!imagesDir.exists()) {
            imagesDir.mkdirs();
        }
        return imagesDir;
    }
    
    /**
     * Copies an image file to the images directory with a unique name if needed.
     * @param sourceFile Source image file
     * @return File object representing the copied file in images directory
     * @throws IOException If copying fails
     */
    public static File copyImageToImagesDirectory(File sourceFile) throws IOException {
        File imagesDir = getImagesDirectory();
        String fileName = sourceFile.getName();
        File destFile = new File(imagesDir, fileName);
        
        // If file exists, generate unique name
        if (destFile.exists()) {
            String baseName = getBaseName(fileName);
            String extension = getExtension(fileName);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
            String timestamp = sdf.format(new Date());
            fileName = baseName + "_" + timestamp + extension;
            destFile = new File(imagesDir, fileName);
        }
        
        // Copy file
        Files.copy(sourceFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        return destFile;
    }
    
    /**
     * Converts an absolute path to a relative path from the images directory.
     * @param absolutePath Absolute path to the image file
     * @return Relative path (e.g., "image.png" or "subfolder/image.png"), or null if not in images directory
     */
    public static String toRelativeImagePath(String absolutePath) {
        if (absolutePath == null || absolutePath.isEmpty()) {
            return null;
        }
        
        try {
            File imagesDir = getImagesDirectory();
            Path imagesPath = imagesDir.toPath().toAbsolutePath().normalize();
            Path filePath = Paths.get(absolutePath).toAbsolutePath().normalize();
            
            if (filePath.startsWith(imagesPath)) {
                Path relativePath = imagesPath.relativize(filePath);
                return relativePath.toString().replace('\\', '/'); // Normalize to forward slashes
            }
        } catch (Exception e) {
            // If conversion fails, return null
        }
        
        return null;
    }
    
    /**
     * Converts a relative path (from images directory) to an absolute path.
     * @param relativePath Relative path (e.g., "image.png")
     * @return Absolute path to the image file, or null if invalid
     */
    public static String toAbsoluteImagePath(String relativePath) {
        if (relativePath == null || relativePath.isEmpty()) {
            return null;
        }
        
        try {
            File imagesDir = getImagesDirectory();
            File imageFile = new File(imagesDir, relativePath);
            if (imageFile.exists()) {
                return imageFile.getAbsolutePath();
            }
        } catch (Exception e) {
            // If conversion fails, return null
        }
        
        return null;
    }
    
    /**
     * Gets the base name of a file (without extension).
     * @param fileName File name
     * @return Base name
     */
    private static String getBaseName(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot > 0) {
            return fileName.substring(0, lastDot);
        }
        return fileName;
    }
    
    /**
     * Gets the extension of a file (including the dot).
     * @param fileName File name
     * @return Extension (e.g., ".png") or empty string if no extension
     */
    private static String getExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot > 0 && lastDot < fileName.length() - 1) {
            return fileName.substring(lastDot);
        }
        return "";
    }
}

