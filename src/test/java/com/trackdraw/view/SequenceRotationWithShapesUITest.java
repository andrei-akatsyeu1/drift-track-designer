package com.trackdraw.view;

import com.trackdraw.config.SequenceManager;
import com.trackdraw.model.AlignPosition;
import com.trackdraw.model.ShapeInstance;
import com.trackdraw.model.ShapeSequence;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * UI tests for sequence rotation with actual shapes loaded from test file.
 * Tests REQ-008: Sequence Rotation with real sequences.
 */
@DisplayName("Sequence Rotation With Shapes UI Tests")
public class SequenceRotationWithShapesUITest extends BaseUITest {
    
    private static final String TEST_FILE = "src/test/resources/test_sequence_with_shapes.json";
    
    @Test
    @DisplayName("Should rotate active sequence with shapes using + key by 10 degrees clockwise")
    public void testRotateSequenceWithShapesUsingPlusKey() throws IOException {
        waitForUI();
        
        // Load test sequence with shapes
        SequenceManager manager = new SequenceManager();
        SequenceManager.LoadResult result = loadTestSequence(manager);
        
        ShapeSequence seq = result.getSequences().get(0);
        assertThat(seq.getShapes().size()).isGreaterThan(0);
        
        seq.setActive(true);
        mainWindow.getAllSequences().clear();
        mainWindow.getAllSequences().add(seq);
        mainWindow.getShapeSequencePanel().setSequences(mainWindow.getAllSequences());
        mainWindow.getDrawingPanel().setSequences(mainWindow.getAllSequences());
        
        // Draw all sequences to initialize positions
        mainWindow.getDrawingPanel().drawAll();
        waitForUI();
        
        // Verify the sequence is active in the panel
        assertThat(mainWindow.getShapeSequencePanel().getActiveSequence()).isNotNull();
        
        AlignPosition initialPos = seq.getInitialAlignmentAsPosition();
        double initialAngle = initialPos.getAngle();
        
        // Call adjustAlignmentPosition directly (mocking + key - clockwise rotation decreases angle)
        AlignmentKeyboardController keyboardController = mainWindow.getKeyboardController();
        keyboardController.adjustAlignmentPosition(0, 0, -10, true); // Rotate clockwise by 10 degrees
        waitForUI();
        
        AlignPosition newPos = seq.getInitialAlignmentAsPosition();
        double expectedAngle = initialAngle - 10;
        // Normalize angle to 0-360 range
        while (expectedAngle < 0) expectedAngle += 360;
        while (expectedAngle >= 360) expectedAngle -= 360;
        assertThat(newPos.getAngle()).isCloseTo(expectedAngle, org.assertj.core.data.Offset.offset(0.1));
    }
    
    @Test
    @DisplayName("Should rotate active sequence with shapes using - key by 10 degrees counter-clockwise")
    public void testRotateSequenceWithShapesUsingMinusKey() throws IOException {
        waitForUI();
        
        SequenceManager manager = new SequenceManager();
        SequenceManager.LoadResult result = loadTestSequence(manager);
        
        ShapeSequence seq = result.getSequences().get(0);
        seq.setActive(true);
        mainWindow.getAllSequences().clear();
        mainWindow.getAllSequences().add(seq);
        mainWindow.getShapeSequencePanel().setSequences(mainWindow.getAllSequences());
        mainWindow.getDrawingPanel().setSequences(mainWindow.getAllSequences());
        
        // Trigger a repaint to ensure sequences are drawn and positions are initialized
        mainWindow.getDrawingPanel().repaint();
        waitForUI();
        
        // Verify the sequence is active in the panel
        assertThat(mainWindow.getShapeSequencePanel().getActiveSequence()).isNotNull();
        
        AlignPosition initialPos = seq.getInitialAlignmentAsPosition();
        double initialAngle = initialPos.getAngle();
        
        // Call adjustAlignmentPosition directly (mocking - key - counter-clockwise rotation)
        AlignmentKeyboardController keyboardController = mainWindow.getKeyboardController();
        keyboardController.adjustAlignmentPosition(0, 0, 10, true); // Rotate counter-clockwise by 10 degrees
        waitForUI();
        
        AlignPosition newPos = seq.getInitialAlignmentAsPosition();
        double expectedAngle = initialAngle + 10;
        // Normalize angle to 0-360 range
        while (expectedAngle < 0) expectedAngle += 360;
        while (expectedAngle >= 360) expectedAngle -= 360;
        assertThat(newPos.getAngle()).isCloseTo(expectedAngle, org.assertj.core.data.Offset.offset(0.1));
    }
    
    @Test
    @DisplayName("Should rotate active sequence with shapes using Ctrl+Plus by 1 degree")
    public void testRotateSequenceWithShapesUsingCtrlPlus() throws IOException {
        waitForUI();
        
        SequenceManager manager = new SequenceManager();
        SequenceManager.LoadResult result = loadTestSequence(manager);
        
        ShapeSequence seq = result.getSequences().get(0);
        seq.setActive(true);
        mainWindow.getAllSequences().clear();
        mainWindow.getAllSequences().add(seq);
        mainWindow.getShapeSequencePanel().setSequences(mainWindow.getAllSequences());
        mainWindow.getDrawingPanel().setSequences(mainWindow.getAllSequences());
        
        // Trigger a repaint to ensure sequences are drawn and positions are initialized
        mainWindow.getDrawingPanel().repaint();
        waitForUI();
        
        // Verify the sequence is active in the panel
        assertThat(mainWindow.getShapeSequencePanel().getActiveSequence()).isNotNull();
        
        AlignPosition initialPos = seq.getInitialAlignmentAsPosition();
        double initialAngle = initialPos.getAngle();
        
        // Call adjustAlignmentPosition directly (mocking Ctrl+Plus - 1 degree rotation)
        AlignmentKeyboardController keyboardController = mainWindow.getKeyboardController();
        keyboardController.adjustAlignmentPosition(0, 0, -1, true); // Rotate clockwise by 1 degree
        waitForUI();
        
        AlignPosition newPos = seq.getInitialAlignmentAsPosition();
        double expectedAngle = initialAngle - 1;
        // Normalize angle to 0-360 range
        while (expectedAngle < 0) expectedAngle += 360;
        while (expectedAngle >= 360) expectedAngle -= 360;
        assertThat(newPos.getAngle()).isCloseTo(expectedAngle, org.assertj.core.data.Offset.offset(0.1));
    }
    
    @Test
    @DisplayName("Should rotate active sequence with shapes using Ctrl+Minus by 1 degree")
    public void testRotateSequenceWithShapesUsingCtrlMinus() throws IOException {
        waitForUI();
        
        SequenceManager manager = new SequenceManager();
        SequenceManager.LoadResult result = loadTestSequence(manager);
        
        ShapeSequence seq = result.getSequences().get(0);
        seq.setActive(true);
        mainWindow.getAllSequences().clear();
        mainWindow.getAllSequences().add(seq);
        mainWindow.getShapeSequencePanel().setSequences(mainWindow.getAllSequences());
        mainWindow.getDrawingPanel().setSequences(mainWindow.getAllSequences());
        
        // Trigger a repaint to ensure sequences are drawn and positions are initialized
        mainWindow.getDrawingPanel().repaint();
        waitForUI();
        
        // Verify the sequence is active in the panel
        assertThat(mainWindow.getShapeSequencePanel().getActiveSequence()).isNotNull();
        
        AlignPosition initialPos = seq.getInitialAlignmentAsPosition();
        double initialAngle = initialPos.getAngle();
        
        // Call adjustAlignmentPosition directly (mocking Ctrl+Minus - 1 degree rotation)
        AlignmentKeyboardController keyboardController = mainWindow.getKeyboardController();
        keyboardController.adjustAlignmentPosition(0, 0, 1, true); // Rotate counter-clockwise by 1 degree
        waitForUI();
        
        AlignPosition newPos = seq.getInitialAlignmentAsPosition();
        double expectedAngle = initialAngle + 1;
        // Normalize angle to 0-360 range
        while (expectedAngle < 0) expectedAngle += 360;
        while (expectedAngle >= 360) expectedAngle -= 360;
        assertThat(newPos.getAngle()).isCloseTo(expectedAngle, org.assertj.core.data.Offset.offset(0.1));
    }
    
    @Test
    @DisplayName("Should not rotate linked sequences")
    public void testLinkedSequenceNotRotatable() throws IOException {
        waitForUI();
        
        SequenceManager manager = new SequenceManager();
        SequenceManager.LoadResult result = loadTestSequence(manager);
        
        ShapeSequence parentSeq = result.getSequences().get(0);
        parentSeq.setActive(false);
        
        // Create a linked sequence
        ShapeSequence linkedSeq = new ShapeSequence("Linked");
        if (!parentSeq.getShapes().isEmpty()) {
            linkedSeq.setInitialAlignment(parentSeq.getShapes().get(0));
            // Add a shape to make it non-empty (create new instance from template)
            ShapeInstance template = parentSeq.getShapes().get(0);
            ShapeInstance newShape = createShapeInstance(template);
            newShape.setOrientation(-template.getOrientation());
            linkedSeq.insertShape(0, newShape);
        }
        linkedSeq.setActive(true);
        
        mainWindow.getAllSequences().clear();
        mainWindow.getAllSequences().add(parentSeq);
        mainWindow.getAllSequences().add(linkedSeq);
        mainWindow.getShapeSequencePanel().setSequences(mainWindow.getAllSequences());
        mainWindow.getDrawingPanel().setSequences(mainWindow.getAllSequences());
        
        // Draw all sequences to initialize positions
        mainWindow.getDrawingPanel().drawAll();
        waitForUI();
        
        // Linked sequences get their position from the linked shape, not from initialAlignmentAsPosition
        // So we need to get the effective position instead
        AlignPosition initialPos = linkedSeq.getEffectiveInitialAlignment();
        assertThat(initialPos).isNotNull(); // Linked sequence should have effective alignment
        double initialAngle = initialPos.getAngle();
        
        // Try to rotate linked sequence (should not work)
        AlignmentKeyboardController keyboardController = mainWindow.getKeyboardController();
        keyboardController.adjustAlignmentPosition(0, 0, -10, true); // Try to rotate clockwise by 10 degrees
        waitForUI();
        
        // Angle should not change (linked sequences cannot be rotated)
        AlignPosition newPos = linkedSeq.getEffectiveInitialAlignment();
        assertThat(newPos.getAngle()).isCloseTo(initialAngle, org.assertj.core.data.Offset.offset(0.1));
    }
    
    /**
     * Loads test sequence from test resources.
     */
    private SequenceManager.LoadResult loadTestSequence(SequenceManager manager) throws IOException {
        // Try to load from test resources first
        java.io.InputStream inputStream = getClass().getClassLoader().getResourceAsStream("test_sequence_with_shapes.json");
        if (inputStream != null) {
            // Copy to temp file and load
            File tempFile = File.createTempFile("test_sequence", ".json");
            tempFile.deleteOnExit();
            try (java.io.FileOutputStream out = new java.io.FileOutputStream(tempFile)) {
                inputStream.transferTo(out);
            }
            return manager.loadSequences(tempFile.getAbsolutePath());
        }
        
        // Fallback to file system path
        File testFile = new File(TEST_FILE);
        if (!testFile.exists()) {
            throw new IOException("Test file not found: " + TEST_FILE);
        }
        return manager.loadSequences(testFile.getAbsolutePath());
    }
    
    /**
     * Creates a new ShapeInstance from a template (similar to ShapeSequenceController).
     */
    private ShapeInstance createShapeInstance(ShapeInstance template) {
        return template.copy();
    }
}

