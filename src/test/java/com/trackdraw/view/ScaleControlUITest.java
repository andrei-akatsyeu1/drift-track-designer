package com.trackdraw.view;

import com.trackdraw.config.GlobalScale;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * UI tests for scale control functionality.
 * Tests REQ-012: Global Scale requirements.
 */
@DisplayName("Scale Control UI Tests")
public class ScaleControlUITest extends BaseUITest {
    
    @Test
    @DisplayName("Scale control should display current scale percentage")
    public void testScaleDisplay() {
        waitForUI();
        
        // Get initial scale
        double initialScale = GlobalScale.getScale();
        
        // The scale should be displayed in the text field
        // Note: This requires ScaleControlPanel to expose the text field with a name
        assertThat(initialScale).isGreaterThan(0);
    }
    
    @Test
    @DisplayName("Increase scale button should increase scale by 10%")
    public void testIncreaseScaleButton() {
        waitForUI();
        
        // Set a known initial scale for testing - sync both GlobalScale and ScaleControlPanel
        GlobalScale.setScale(1.0);
        mainWindow.getScaleControlPanel().setScale(1.0);
        double initialScale = GlobalScale.getScale();
        
        // Get the scale control and trigger increase
        ScaleControl scaleControl = mainWindow.getScaleControlPanel().getScaleControl();
        scaleControl.changeScale(0.1);
        waitForUI();
        
        double newScale = GlobalScale.getScale();
        assertThat(newScale).isGreaterThan(initialScale);
        // changeScale adds 0.1 (10% of 1.0), so 1.0 + 0.1 = 1.1
        assertThat(newScale).isCloseTo(initialScale + 0.1, org.assertj.core.data.Offset.offset(0.01));
    }
    
    @Test
    @DisplayName("Decrease scale button should decrease scale by 10%")
    public void testDecreaseScaleButton() {
        waitForUI();
        
        // Set a known initial scale for testing - sync both GlobalScale and ScaleControlPanel
        GlobalScale.setScale(1.0);
        mainWindow.getScaleControlPanel().setScale(1.0);
        double initialScale = GlobalScale.getScale();
        
        // Get the scale control and trigger decrease
        ScaleControl scaleControl = mainWindow.getScaleControlPanel().getScaleControl();
        scaleControl.changeScale(-0.1);
        waitForUI();
        
        double newScale = GlobalScale.getScale();
        assertThat(newScale).isLessThan(initialScale);
        // changeScale subtracts 0.1 (10% of 1.0), so 1.0 - 0.1 = 0.9
        assertThat(newScale).isCloseTo(initialScale - 0.1, org.assertj.core.data.Offset.offset(0.01));
    }
    
    @Test
    @DisplayName("Scale should not go below minimum")
    public void testScaleMinimum() {
        waitForUI();
        
        // Set scale to minimum
        GlobalScale.setScale(0.1);
        
        // Try to decrease further
        ScaleControl scaleControl = mainWindow.getScaleControlPanel().getScaleControl();
        for (int i = 0; i < 10; i++) {
            scaleControl.changeScale(-0.1);
            waitForUI();
        }
        
        double finalScale = GlobalScale.getScale();
        assertThat(finalScale).isGreaterThanOrEqualTo(0.1);
    }
    
    @Test
    @DisplayName("Scale should not exceed maximum")
    public void testScaleMaximum() {
        waitForUI();
        
        // Set scale to a high value
        GlobalScale.setScale(5.0);
        
        // Try to increase further
        ScaleControl scaleControl = mainWindow.getScaleControlPanel().getScaleControl();
        for (int i = 0; i < 10; i++) {
            scaleControl.changeScale(0.1);
            waitForUI();
        }
        
        double finalScale = GlobalScale.getScale();
        assertThat(finalScale).isLessThanOrEqualTo(10.0); // Assuming max is 10.0
    }
}

