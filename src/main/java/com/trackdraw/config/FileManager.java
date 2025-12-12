package com.trackdraw.config;

import org.apache.commons.lang3.StringUtils;

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
    private static final String EXPORT_DIR = "export";
    
    /**
     * Gets the working directory (project root).
     * @return File representing the working directory
     */
    public static File getWorkingDirectory() {
        return new File(System.getProperty("user.dir"));
    }
    
    /**
     * Gets or creates a directory under the working directory.
     * @param dirName Name of the directory
     * @return File representing the directory
     */
    private static File getOrCreateDirectory(String dirName) {
        File dir = new File(getWorkingDirectory(), dirName);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }
    
    /**
     * Gets the saves directory, creating it if it doesn't exist.
     * @return File representing the saves directory
     */
    public static File getSavesDirectory() {
        return getOrCreateDirectory(SAVES_DIR);
    }
    
    /**
     * Gets the images directory, creating it if it doesn't exist.
     * @return File representing the images directory
     */
    public static File getImagesDirectory() {
        return getOrCreateDirectory(IMAGES_DIR);
    }
    
    /**
     * Gets the export directory, creating it if it doesn't exist.
     * @return File representing the export directory
     */
    public static File getExportDirectory() {
        return getOrCreateDirectory(EXPORT_DIR);
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
        if (StringUtils.isEmpty(absolutePath)) {
            return null;
        }
        
        try {
            Path imagesPath = getImagesDirectory().toPath().toAbsolutePath().normalize();
            Path filePath = Paths.get(absolutePath).toAbsolutePath().normalize();
            
            if (filePath.startsWith(imagesPath)) {
                return imagesPath.relativize(filePath).toString().replace('\\', '/');
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
        if (StringUtils.isEmpty(relativePath)) {
            return null;
        }
        
        try {
            File imageFile = new File(getImagesDirectory(), relativePath);
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
    public static String getBaseName(String fileName) {
        if (fileName == null) {
            return "";
        }
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot > 0) {
            return fileName.substring(0, lastDot);
        }
        return fileName;
    }
    
    /**
     * Gets the base name of a file (without extension).
     * @param file File object
     * @return Base name without extension
     */
    public static String getBaseName(File file) {
        if (file == null) {
            return "";
        }
        return getBaseName(file.getName());
    }
    
    /**
     * Gets the extension of a file (including the dot).
     * @param fileName File name
     * @return Extension (e.g., ".png") or empty string if no extension
     */
    private static String getExtension(String fileName) {
        if (fileName == null) {
            return "";
        }
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot > 0 && lastDot < fileName.length() - 1) {
            return fileName.substring(lastDot);
        }
        return "";
    }
    
    /**
     * Ensures a file path has the specified extension.
     * If the path already has the extension, returns it unchanged.
     * Otherwise, appends the extension.
     * @param filePath File path
     * @param extension Extension to ensure (with or without leading dot, e.g., ".png" or "png")
     * @return File path with the extension
     */
    public static String ensureExtension(String filePath, String extension) {
        if (StringUtils.isEmpty(filePath)) {
            return filePath;
        }
        
        // Normalize extension (ensure it starts with a dot)
        String normalizedExtension = extension;
        if (!normalizedExtension.startsWith(".")) {
            normalizedExtension = "." + normalizedExtension;
        }
        
        // Check if path already has this extension (case-insensitive)
        String lowerPath = filePath.toLowerCase();
        String lowerExt = normalizedExtension.toLowerCase();
        if (lowerPath.endsWith(lowerExt)) {
            return filePath;
        }
        
        return filePath + normalizedExtension;
    }
    
}

