package com.trackdraw.view;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.awt.event.KeyEvent;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * UI tests for background image scale keyboard shortcuts.
 * Tests REQ-013: Background Image Scale keyboard shortcuts.
 */
@DisplayName("Background Image Scale UI Tests")
public class BackgroundImageScaleUITest extends BaseUITest {
    
    @Test
    @DisplayName("Should increase image scale with Page Up key by 10%")
    public void testIncreaseImageScaleWithPageUp() {
        waitForUI();
        
        DrawingPanel drawingPanel = mainWindow.getDrawingPanel();
        double initialScale = drawingPanel.getBackgroundImageScale();
        
        // Focus drawing panel and press Page Up
        window.panel("drawingPanel").focus();
        window.robot().pressKey(KeyEvent.VK_PAGE_UP);
        window.robot().releaseKey(KeyEvent.VK_PAGE_UP);
        waitForUI();
        
        double newScale = drawingPanel.getBackgroundImageScale();
        assertThat(newScale).isEqualTo(initialScale + 0.1);
    }
    
    @Test
    @DisplayName("Should decrease image scale with Page Down key by 10%")
    public void testDecreaseImageScaleWithPageDown() {
        waitForUI();
        
        DrawingPanel drawingPanel = mainWindow.getDrawingPanel();
        // Set initial scale to something > 0.1
        drawingPanel.setBackgroundImageScale(1.0);
        waitForUI();
        
        double initialScale = drawingPanel.getBackgroundImageScale();
        
        // Press Page Down
        window.panel("drawingPanel").focus();
        window.robot().pressKey(KeyEvent.VK_PAGE_DOWN);
        window.robot().releaseKey(KeyEvent.VK_PAGE_DOWN);
        waitForUI();
        
        double newScale = drawingPanel.getBackgroundImageScale();
        assertThat(newScale).isEqualTo(initialScale - 0.1);
    }
    
    @Test
    @DisplayName("Should increase image scale with Ctrl+Page Up by 1%")
    public void testIncreaseImageScaleWithCtrlPageUp() {
        waitForUI();
        
        DrawingPanel drawingPanel = mainWindow.getDrawingPanel();
        drawingPanel.setBackgroundImageScale(1.0);
        waitForUI();
        
        double initialScale = drawingPanel.getBackgroundImageScale();
        
        // Press Ctrl+Page Up
        window.panel("drawingPanel").focus();
        window.robot().pressKey(KeyEvent.VK_CONTROL);
        window.robot().pressKey(KeyEvent.VK_PAGE_UP);
        window.robot().releaseKey(KeyEvent.VK_PAGE_UP);
        window.robot().releaseKey(KeyEvent.VK_CONTROL);
        waitForUI();
        
        double newScale = drawingPanel.getBackgroundImageScale();
        assertThat(newScale).isCloseTo(initialScale + 0.01, org.assertj.core.data.Offset.offset(0.001));
    }
    
    @Test
    @DisplayName("Should decrease image scale with Ctrl+Page Down by 1%")
    public void testDecreaseImageScaleWithCtrlPageDown() {
        waitForUI();
        
        DrawingPanel drawingPanel = mainWindow.getDrawingPanel();
        drawingPanel.setBackgroundImageScale(1.0);
        waitForUI();
        
        double initialScale = drawingPanel.getBackgroundImageScale();
        
        // Press Ctrl+Page Down
        window.panel("drawingPanel").focus();
        window.robot().pressKey(KeyEvent.VK_CONTROL);
        window.robot().pressKey(KeyEvent.VK_PAGE_DOWN);
        window.robot().releaseKey(KeyEvent.VK_PAGE_DOWN);
        window.robot().releaseKey(KeyEvent.VK_CONTROL);
        waitForUI();
        
        double newScale = drawingPanel.getBackgroundImageScale();
        assertThat(newScale).isCloseTo(initialScale - 0.01, org.assertj.core.data.Offset.offset(0.001));
    }
}

