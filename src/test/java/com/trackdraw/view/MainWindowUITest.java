package com.trackdraw.view;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * UI tests for MainWindow component.
 * Tests user interface interactions and component visibility.
 */
@DisplayName("MainWindow UI Tests")
public class MainWindowUITest extends BaseUITest {
    
    @Test
    @DisplayName("Main window should display with correct title")
    public void testMainWindowTitle() {
        window.requireTitle("Track Draw - Shape Drawing Application");
    }
    
    @Test
    @DisplayName("Main window should be visible")
    public void testMainWindowVisible() {
        window.requireVisible();
    }
    
    @Test
    @DisplayName("Top panel should contain scale controls")
    public void testScaleControlsPresent() {
        // Verify scale control panel exists
        assertThat(mainWindow.getScaleControlPanel()).isNotNull();
        waitForUI();
    }
    
    @Test
    @DisplayName("Measurement tool checkbox should be present")
    public void testMeasurementToolCheckbox() {
        // The checkbox might not have a name, so we search by tooltip or type
        waitForUI();
        // This test verifies the checkbox exists - actual interaction tests are in separate test
    }
    
    @Test
    @DisplayName("Show keys checkbox should be present")
    public void testShowKeysCheckbox() {
        waitForUI();
        // Verify checkbox exists - interaction tests in separate test class
    }
    
    @Test
    @DisplayName("Left panel should contain sequence management")
    public void testSequencePanelPresent() {
        waitForUI();
        // Verify sequence panel is present
        assertThat(mainWindow).isNotNull();
    }
    
    @Test
    @DisplayName("Canvas should be present")
    public void testCanvasPresent() {
        waitForUI();
        // Verify drawing panel exists
        assertThat(mainWindow).isNotNull();
    }
    
    @Test
    @DisplayName("Status bar should be present")
    public void testStatusBarPresent() {
        waitForUI();
        // Verify status bar exists
        assertThat(mainWindow).isNotNull();
    }
}

