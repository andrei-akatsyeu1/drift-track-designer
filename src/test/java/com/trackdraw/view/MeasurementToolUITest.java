package com.trackdraw.view;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * UI tests for measurement tool functionality.
 * Tests REQ-018: Distance Measurement requirements.
 */
@DisplayName("Measurement Tool UI Tests")
public class MeasurementToolUITest extends BaseUITest {
    
    @Test
    @DisplayName("Measurement tool checkbox should toggle tool activation")
    public void testMeasurementToolToggle() {
        waitForUI();
        
        // Get the measurement tool checkbox
        // Note: This requires the checkbox to have a name or we find it by tooltip
        boolean initialState = mainWindow.getDrawingPanel().getMeasurementTool().isActive();
        
        // Toggle the checkbox
        mainWindow.getMeasureCheckBox().doClick();
        waitForUI();
        
        boolean newState = mainWindow.getDrawingPanel().getMeasurementTool().isActive();
        assertThat(newState).isNotEqualTo(initialState);
        
        // Toggle back
        mainWindow.getMeasureCheckBox().doClick();
        waitForUI();
        
        boolean finalState = mainWindow.getDrawingPanel().getMeasurementTool().isActive();
        assertThat(finalState).isEqualTo(initialState);
    }
    
    @Test
    @DisplayName("Measurement tool should be inactive by default")
    public void testMeasurementToolDefaultState() {
        waitForUI();
        
        boolean isActive = mainWindow.getDrawingPanel().getMeasurementTool().isActive();
        assertThat(isActive).isFalse();
    }
    
    @Test
    @DisplayName("Measurement tool checkbox should reflect tool state")
    public void testCheckboxStateSync() {
        waitForUI();
        
        // Initially unchecked
        assertThat(mainWindow.getMeasureCheckBox().isSelected()).isFalse();
        
        // Click to activate
        mainWindow.getMeasureCheckBox().doClick();
        waitForUI();
        
        // Should be checked and tool active
        assertThat(mainWindow.getMeasureCheckBox().isSelected()).isTrue();
        assertThat(mainWindow.getDrawingPanel().getMeasurementTool().isActive()).isTrue();
    }
}

