package com.trackdraw.view;

import com.trackdraw.config.FileManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.io.File;
import java.awt.event.KeyEvent;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * UI tests for background image functionality with actual image file.
 * Tests REQ-016: Image Loading with real image file.
 */
@DisplayName("Background Image With File UI Tests")
public class BackgroundImageWithFileUITest extends BaseUITest {
    
    private static final String TEST_IMAGE_NAME = "9c126f88-19ce-430c-857e-496ca3330e4c.jpg";
    
    /**
     * Gets the path to the test image file.
     */
    private String getTestImagePath() {
        // Try to find the image in the images directory
        File imagesDir = FileManager.getImagesDirectory();
        File testImage = new File(imagesDir, TEST_IMAGE_NAME);
        
        if (testImage.exists()) {
            return testImage.getAbsolutePath();
        }
        
        // Fallback: try relative path from project root
        File relativeImage = new File("images", TEST_IMAGE_NAME);
        if (relativeImage.exists()) {
            return relativeImage.getAbsolutePath();
        }
        
        throw new RuntimeException("Test image not found: " + TEST_IMAGE_NAME);
    }
    
    @Test
    @DisplayName("Should load background image from file")
    public void testLoadBackgroundImageFromFile() {
        waitForUI();
        
        DrawingPanel drawingPanel = mainWindow.getDrawingPanel();
        
        // Initially no image
        assertThat(drawingPanel.getBackgroundImage()).isNull();
        assertThat(drawingPanel.getBackgroundImagePath()).isNull();
        
        // Load the test image
        String imagePath = getTestImagePath();
        drawingPanel.setBackgroundImage(imagePath);
        waitForUI();
        
        // Verify image is loaded
        BufferedImage loadedImage = drawingPanel.getBackgroundImage();
        assertThat(loadedImage).isNotNull();
        assertThat(drawingPanel.getBackgroundImagePath()).isEqualTo(imagePath);
        assertThat(loadedImage.getWidth()).isGreaterThan(0);
        assertThat(loadedImage.getHeight()).isGreaterThan(0);
    }
    
    @Test
    @DisplayName("Should set initial scale to 1.0 when image is loaded")
    public void testInitialScaleOnImageLoad() {
        waitForUI();
        
        DrawingPanel drawingPanel = mainWindow.getDrawingPanel();
        
        // Load the test image
        String imagePath = getTestImagePath();
        drawingPanel.setBackgroundImage(imagePath);
        waitForUI();
        
        // Verify initial scale is 1.0
        assertThat(drawingPanel.getBackgroundImageScale()).isEqualTo(1.0);
    }
    
    @Test
    @DisplayName("Should clear background image and reset scale")
    public void testClearBackgroundImage() {
        waitForUI();
        
        DrawingPanel drawingPanel = mainWindow.getDrawingPanel();
        
        // Load the test image
        String imagePath = getTestImagePath();
        drawingPanel.setBackgroundImage(imagePath);
        drawingPanel.setBackgroundImageScale(1.5);
        waitForUI();
        
        assertThat(drawingPanel.getBackgroundImage()).isNotNull();
        assertThat(drawingPanel.getBackgroundImageScale()).isEqualTo(1.5);
        
        // Clear the image
        drawingPanel.setBackgroundImage(null);
        waitForUI();
        
        // Verify image is cleared
        assertThat(drawingPanel.getBackgroundImage()).isNull();
        assertThat(drawingPanel.getBackgroundImagePath()).isNull();
        
        // Scale should remain as set (clearing doesn't automatically reset scale in DrawingPanel)
        // But MainWindow.clearBackgroundImage() does reset it
        assertThat(drawingPanel.getBackgroundImageScale()).isEqualTo(1.5);
    }
    
    @Test
    @DisplayName("Should scale background image with Page Up/Down keys")
    public void testScaleBackgroundImageWithKeyboard() {
        waitForUI();
        
        DrawingPanel drawingPanel = mainWindow.getDrawingPanel();
        
        // Load the test image
        String imagePath = getTestImagePath();
        drawingPanel.setBackgroundImage(imagePath);
        drawingPanel.setBackgroundImageScale(1.0);
        waitForUI();
        
        assertThat(drawingPanel.getBackgroundImage()).isNotNull();
        double initialScale = drawingPanel.getBackgroundImageScale();
        assertThat(initialScale).isEqualTo(1.0);
        
        // Press Page Up to increase scale
        window.panel("drawingPanel").focus();
        window.robot().pressKey(KeyEvent.VK_PAGE_UP);
        window.robot().releaseKey(KeyEvent.VK_PAGE_UP);
        waitForUI();
        
        double newScale = drawingPanel.getBackgroundImageScale();
        assertThat(newScale).isEqualTo(initialScale + 0.1);
        
        // Press Page Down to decrease scale
        window.robot().pressKey(KeyEvent.VK_PAGE_DOWN);
        window.robot().releaseKey(KeyEvent.VK_PAGE_DOWN);
        waitForUI();
        
        double finalScale = drawingPanel.getBackgroundImageScale();
        assertThat(finalScale).isEqualTo(initialScale);
    }
    
    @Test
    @DisplayName("Should scale background image with Ctrl+Page Up/Down by 1%")
    public void testScaleBackgroundImageWithCtrlKeyboard() {
        waitForUI();
        
        DrawingPanel drawingPanel = mainWindow.getDrawingPanel();
        
        // Load the test image
        String imagePath = getTestImagePath();
        drawingPanel.setBackgroundImage(imagePath);
        drawingPanel.setBackgroundImageScale(1.0);
        waitForUI();
        
        double initialScale = drawingPanel.getBackgroundImageScale();
        
        // Press Ctrl+Page Up to increase scale by 1%
        window.panel("drawingPanel").focus();
        window.robot().pressKey(KeyEvent.VK_CONTROL);
        window.robot().pressKey(KeyEvent.VK_PAGE_UP);
        window.robot().releaseKey(KeyEvent.VK_PAGE_UP);
        window.robot().releaseKey(KeyEvent.VK_CONTROL);
        waitForUI();
        
        double newScale = drawingPanel.getBackgroundImageScale();
        assertThat(newScale).isCloseTo(initialScale + 0.01, org.assertj.core.data.Offset.offset(0.001));
        
        // Press Ctrl+Page Down to decrease scale by 1%
        window.robot().pressKey(KeyEvent.VK_CONTROL);
        window.robot().pressKey(KeyEvent.VK_PAGE_DOWN);
        window.robot().releaseKey(KeyEvent.VK_PAGE_DOWN);
        window.robot().releaseKey(KeyEvent.VK_CONTROL);
        waitForUI();
        
        double finalScale = drawingPanel.getBackgroundImageScale();
        assertThat(finalScale).isCloseTo(initialScale, org.assertj.core.data.Offset.offset(0.001));
    }
    
    @Test
    @DisplayName("Should preserve image path after scaling")
    public void testImagePathPreservedAfterScaling() {
        waitForUI();
        
        DrawingPanel drawingPanel = mainWindow.getDrawingPanel();
        
        // Load the test image
        String imagePath = getTestImagePath();
        drawingPanel.setBackgroundImage(imagePath);
        drawingPanel.setBackgroundImageScale(1.0);
        waitForUI();
        
        String initialPath = drawingPanel.getBackgroundImagePath();
        
        // Scale the image multiple times
        window.panel("drawingPanel").focus();
        for (int i = 0; i < 5; i++) {
            window.robot().pressKey(KeyEvent.VK_PAGE_UP);
            window.robot().releaseKey(KeyEvent.VK_PAGE_UP);
            waitForUI();
        }
        
        // Verify path is still preserved
        assertThat(drawingPanel.getBackgroundImagePath()).isEqualTo(initialPath);
        assertThat(drawingPanel.getBackgroundImage()).isNotNull();
    }
    
    @Test
    @DisplayName("Should handle image reloading")
    public void testImageReloading() {
        waitForUI();
        
        DrawingPanel drawingPanel = mainWindow.getDrawingPanel();
        
        // Load the test image
        String imagePath = getTestImagePath();
        drawingPanel.setBackgroundImage(imagePath);
        drawingPanel.setBackgroundImageScale(1.5);
        waitForUI();
        
        BufferedImage firstImage = drawingPanel.getBackgroundImage();
        assertThat(firstImage).isNotNull();
        assertThat(drawingPanel.getBackgroundImageScale()).isEqualTo(1.5);
        
        // Reload the same image
        drawingPanel.setBackgroundImage(imagePath);
        waitForUI();
        
        // Image should still be loaded
        BufferedImage reloadedImage = drawingPanel.getBackgroundImage();
        assertThat(reloadedImage).isNotNull();
        assertThat(reloadedImage.getWidth()).isEqualTo(firstImage.getWidth());
        assertThat(reloadedImage.getHeight()).isEqualTo(firstImage.getHeight());
        // Scale should remain as set (not automatically reset on reload in DrawingPanel)
        assertThat(drawingPanel.getBackgroundImageScale()).isEqualTo(1.5);
    }
    
    @Test
    @DisplayName("Should handle invalid image path gracefully")
    public void testInvalidImagePath() {
        waitForUI();
        
        DrawingPanel drawingPanel = mainWindow.getDrawingPanel();
        
        // Try to load non-existent image
        drawingPanel.setBackgroundImage("nonexistent/image.jpg");
        waitForUI();
        
        // Image should be null, but no exception should be thrown
        assertThat(drawingPanel.getBackgroundImage()).isNull();
        assertThat(drawingPanel.getBackgroundImagePath()).isEqualTo("nonexistent/image.jpg");
    }
    
    @Test
    @DisplayName("Should maintain image when sequences are updated")
    public void testImageMaintainedWhenSequencesUpdated() {
        waitForUI();
        
        DrawingPanel drawingPanel = mainWindow.getDrawingPanel();
        
        // Load the test image
        String imagePath = getTestImagePath();
        drawingPanel.setBackgroundImage(imagePath);
        waitForUI();
        
        assertThat(drawingPanel.getBackgroundImage()).isNotNull();
        String imagePathBefore = drawingPanel.getBackgroundImagePath();
        
        // Update sequences
        drawingPanel.setSequences(mainWindow.getAllSequences());
        waitForUI();
        
        // Image should still be loaded
        assertThat(drawingPanel.getBackgroundImage()).isNotNull();
        assertThat(drawingPanel.getBackgroundImagePath()).isEqualTo(imagePathBefore);
    }
}

