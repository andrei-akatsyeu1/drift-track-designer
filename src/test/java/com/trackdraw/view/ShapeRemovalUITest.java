package com.trackdraw.view;

import com.trackdraw.config.ShapeConfig;
import com.trackdraw.model.ShapeInstance;
import com.trackdraw.model.ShapeSequence;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.awt.event.KeyEvent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.swing.core.KeyPressInfo.keyCode;

/**
 * UI tests for shape removal functionality.
 * Tests that after removing a shape, the next shape becomes active.
 */
@DisplayName("Shape Removal UI Tests")
public class ShapeRemovalUITest extends BaseUITest {
    
    /**
     * Helper method to add shapes to the active sequence for testing.
     */
    private void addShapesToSequence(String... shapeKeys) {
        ShapeSequence activeSequence = mainWindow.getAllSequences().get(0);
        ShapeConfig shapeConfig = new ShapeConfig();
        
        try {
            shapeConfig.loadShapes();
        } catch (Exception e) {
            // Ignore if shapes already loaded
        }
        
        for (String key : shapeKeys) {
            ShapeInstance template = shapeConfig.getShape(key);
            if (template != null) {
                ShapeInstance shape = template.copy();
                activeSequence.insertShape(activeSequence.size(), shape);
            }
        }
        
        // Update the controller's active sequence
        mainWindow.getSequenceController().setActiveSequence(activeSequence);
        waitForUI();
    }
    
    @Test
    @DisplayName("Removing middle shape should activate next shape")
    public void testRemoveMiddleShapeActivatesNext() {
        waitForUI();
        
        // Add three shapes
        addShapesToSequence("05", "10", "15");
        ShapeSequence sequence = mainWindow.getAllSequences().get(0);
        
        // Verify we have 3 shapes
        assertThat(sequence.size()).isEqualTo(3);
        
        // Activate the first shape (index 0)
        sequence.activateShape(0);
        mainWindow.getSequenceController().setActiveSequence(sequence);
        mainWindow.getShapeListPanel().setSelectedIndex(0);
        waitForUI();
        
        // Verify first shape is active
        assertThat(sequence.getShape(0).isActive()).isTrue();
        assertThat(sequence.getActiveShapeIndex()).isEqualTo(0);
        
        // Remove the first shape (index 0)
        mainWindow.getSequenceController().removeSelectedOrLastShape();
        waitForUI();
        
        // Verify sequence now has 2 shapes
        assertThat(sequence.size()).isEqualTo(2);
        
        // Verify the next shape (previously at index 1, now at index 0) is active
        assertThat(sequence.getActiveShapeIndex()).isEqualTo(0);
        assertThat(sequence.getShape(0).isActive()).isTrue();
        // Verify it's the shape that was previously at index 1 (key "10")
        assertThat(sequence.getShape(0).getKey()).isEqualTo("10");
    }
    
    @Test
    @DisplayName("Removing last shape should activate previous shape")
    public void testRemoveLastShapeActivatesPrevious() {
        waitForUI();
        
        // Add three shapes
        addShapesToSequence("05", "10", "15");
        ShapeSequence sequence = mainWindow.getAllSequences().get(0);
        
        // Verify we have 3 shapes
        assertThat(sequence.size()).isEqualTo(3);
        
        // Activate the last shape (index 2)
        sequence.activateShape(2);
        mainWindow.getSequenceController().setActiveSequence(sequence);
        mainWindow.getShapeListPanel().setSelectedIndex(2);
        waitForUI();
        
        // Verify last shape is active
        assertThat(sequence.getShape(2).isActive()).isTrue();
        assertThat(sequence.getActiveShapeIndex()).isEqualTo(2);
        
        // Remove the last shape
        mainWindow.getSequenceController().removeSelectedOrLastShape();
        waitForUI();
        
        // Verify sequence now has 2 shapes
        assertThat(sequence.size()).isEqualTo(2);
        
        // Verify the previous shape (now last, index 1) is active
        assertThat(sequence.getActiveShapeIndex()).isEqualTo(1);
        assertThat(sequence.getShape(1).isActive()).isTrue();
        // Verify it's the shape that was previously at index 1 (key "10")
        assertThat(sequence.getShape(1).getKey()).isEqualTo("10");
    }
    
    @Test
    @DisplayName("Removing only shape should leave sequence empty")
    public void testRemoveOnlyShapeLeavesEmpty() {
        waitForUI();
        
        // Add one shape
        addShapesToSequence("05");
        ShapeSequence sequence = mainWindow.getAllSequences().get(0);
        
        // Verify we have 1 shape
        assertThat(sequence.size()).isEqualTo(1);
        
        // Activate the shape
        sequence.activateShape(0);
        mainWindow.getSequenceController().setActiveSequence(sequence);
        mainWindow.getShapeListPanel().setSelectedIndex(0);
        waitForUI();
        
        // Remove the only shape
        mainWindow.getSequenceController().removeSelectedOrLastShape();
        waitForUI();
        
        // Verify sequence is now empty
        assertThat(sequence.size()).isEqualTo(0);
        assertThat(sequence.isEmpty()).isTrue();
        assertThat(sequence.getActiveShapeIndex()).isEqualTo(-1);
    }
    
    @Test
    @DisplayName("Delete key should remove active shape")
    public void testDeleteKeyRemovesActiveShape() {
        waitForUI();
        
        // Add two shapes
        addShapesToSequence("05", "10");
        ShapeSequence sequence = mainWindow.getAllSequences().get(0);
        
        // Verify we have 2 shapes
        assertThat(sequence.size()).isEqualTo(2);
        
        // Activate the first shape
        sequence.activateShape(0);
        mainWindow.getSequenceController().setActiveSequence(sequence);
        mainWindow.getShapeListPanel().setSelectedIndex(0);
        waitForUI();
        
        // Press Delete key
        window.pressAndReleaseKey(keyCode(KeyEvent.VK_DELETE));
        waitForUI();
        
        // Verify sequence now has 1 shape
        assertThat(sequence.size()).isEqualTo(1);
        
        // Verify the next shape (previously at index 1, now at index 0) is active
        assertThat(sequence.getActiveShapeIndex()).isEqualTo(0);
        assertThat(sequence.getShape(0).isActive()).isTrue();
        assertThat(sequence.getShape(0).getKey()).isEqualTo("10");
    }
    
    @Test
    @DisplayName("Backspace key should remove active shape")
    public void testBackspaceKeyRemovesActiveShape() {
        waitForUI();
        
        // Add two shapes
        addShapesToSequence("05", "10");
        ShapeSequence sequence = mainWindow.getAllSequences().get(0);
        
        // Verify we have 2 shapes
        assertThat(sequence.size()).isEqualTo(2);
        
        // Activate the first shape
        sequence.activateShape(0);
        mainWindow.getSequenceController().setActiveSequence(sequence);
        mainWindow.getShapeListPanel().setSelectedIndex(0);
        waitForUI();
        
        // Press Backspace key
        window.pressAndReleaseKey(keyCode(KeyEvent.VK_BACK_SPACE));
        waitForUI();
        
        // Verify sequence now has 1 shape
        assertThat(sequence.size()).isEqualTo(1);
        
        // Verify the next shape is active
        assertThat(sequence.getActiveShapeIndex()).isEqualTo(0);
        assertThat(sequence.getShape(0).isActive()).isTrue();
    }
    
    @Test
    @DisplayName("Removing shape when none selected should remove last shape")
    public void testRemoveWhenNoneSelectedRemovesLast() {
        waitForUI();
        
        // Add three shapes
        addShapesToSequence("05", "10", "15");
        ShapeSequence sequence = mainWindow.getAllSequences().get(0);
        
        // Clear selection
        mainWindow.getShapeListPanel().clearSelection();
        waitForUI();
        
        // Remove without selection (should remove last)
        mainWindow.getSequenceController().removeSelectedOrLastShape();
        waitForUI();
        
        // Verify sequence now has 2 shapes
        assertThat(sequence.size()).isEqualTo(2);
        
        // Verify the last shape (now at index 1) is active
        assertThat(sequence.getActiveShapeIndex()).isEqualTo(1);
        assertThat(sequence.getShape(1).isActive()).isTrue();
        assertThat(sequence.getShape(1).getKey()).isEqualTo("10");
    }
}

