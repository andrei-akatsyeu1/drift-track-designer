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
 * UI tests for sequence movement with actual shapes loaded from test file.
 * Tests REQ-008: Sequence Movement with real sequences.
 */
@DisplayName("Sequence Movement With Shapes UI Tests")
public class SequenceMovementWithShapesUITest extends BaseUITest {

    private static final String TEST_FILE = "src/test/resources/test_sequence_with_shapes.json";

    @Test
    @DisplayName("Should move active sequence with shapes using arrow keys by 10 pixels")
    public void testMoveSequenceWithShapesUsingArrowKeys() throws IOException {
        waitForUI();

        // Load test sequence with shapes from test resources
        SequenceManager manager = new SequenceManager();
        SequenceManager.LoadResult result = loadTestSequence(manager);

        assertThat(result.getSequences()).isNotEmpty();
        ShapeSequence seq = result.getSequences().get(0);
        assertThat(seq.getShapes().size()).isGreaterThan(0); // Has shapes

        // Set up the sequence in the main window
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
        assertThat(mainWindow.getShapeSequencePanel().getActiveSequence()).isEqualTo(seq);

        // Get initial position (positions are stored normalized to scale 1.0)
        AlignPosition initialPos = seq.getInitialAlignmentAsPosition();
        assertThat(initialPos).isNotNull();
        double currentScale = com.trackdraw.config.GlobalScale.getScale();
        // Convert normalized position to screen coordinates for comparison
        double initialX = initialPos.getX() * currentScale;
        double initialY = initialPos.getY() * currentScale;

        // Call adjustAlignmentPosition directly (mocking keyboard event)
        AlignmentKeyboardController keyboardController = mainWindow.getKeyboardController();
        keyboardController.adjustAlignmentPosition(10, 0, 0, true); // Move right by 10 pixels
        waitForUI();

        // Verify position changed by 10 pixels (movement is independent of scale)
        AlignPosition newPos = seq.getInitialAlignmentAsPosition();
        double newX = newPos.getX() * currentScale;
        double newY = newPos.getY() * currentScale;
        assertThat(newX).isCloseTo(initialX + 10, org.assertj.core.data.Offset.offset(0.1));
        assertThat(newY).isCloseTo(initialY, org.assertj.core.data.Offset.offset(0.1));
    }

    @Test
    @DisplayName("Should move active sequence with shapes using Ctrl+Arrow by 1 pixel")
    public void testMoveSequenceWithShapesUsingCtrlArrow() throws IOException {
        waitForUI();

        // Load test sequence
        SequenceManager manager = new SequenceManager();
        SequenceManager.LoadResult result = loadTestSequence(manager);

        ShapeSequence seq = result.getSequences().get(0);
        seq.setActive(true);
        mainWindow.getAllSequences().clear();
        mainWindow.getAllSequences().add(seq);
        mainWindow.getShapeSequencePanel().setSequences(mainWindow.getAllSequences());
        mainWindow.getDrawingPanel().setSequences(mainWindow.getAllSequences());

        mainWindow.getDrawingPanel().drawAll();
        waitForUI();

        AlignPosition initialPos = seq.getInitialAlignmentAsPosition();
        double currentScale = com.trackdraw.config.GlobalScale.getScale();
        double initialX = initialPos.getX() * currentScale;

        // Call adjustAlignmentPosition directly (mocking Ctrl+Right Arrow - 1 pixel)
        AlignmentKeyboardController keyboardController = mainWindow.getKeyboardController();
        keyboardController.adjustAlignmentPosition(1, 0, 0, true); // Move right by 1 pixel
        waitForUI();

        AlignPosition newPos = seq.getInitialAlignmentAsPosition();
        double newX = newPos.getX() * currentScale;
        assertThat(newX).isCloseTo(initialX + 1, org.assertj.core.data.Offset.offset(0.1));
    }

    @Test
    @DisplayName("Should move all unlinked sequences with shapes using Shift+Arrow")
    public void testMoveAllSequencesWithShapesUsingShiftArrow() throws IOException {
        waitForUI();

        // Load test sequence
        SequenceManager manager = new SequenceManager();
        SequenceManager.LoadResult result = loadTestSequence(manager);

        ShapeSequence seq1 = result.getSequences().get(0);
        seq1.setActive(true);

        // Create a second sequence
        ShapeSequence seq2 = new ShapeSequence("Seq2");
        seq2.setInitialAlignment(new AlignPosition(500, 400, 0));
        // Add a shape to seq2 so it's not empty (create new instance from template)
        if (!seq1.getShapes().isEmpty()) {
            ShapeInstance template = seq1.getShapes().get(0);
            ShapeInstance newShape = createShapeInstance(template);
            seq2.insertShape(0, newShape);
        }

        mainWindow.getAllSequences().clear();
        mainWindow.getAllSequences().add(seq1);
        mainWindow.getAllSequences().add(seq2);
        mainWindow.getShapeSequencePanel().setSequences(mainWindow.getAllSequences());
        mainWindow.getDrawingPanel().setSequences(mainWindow.getAllSequences());

        mainWindow.getDrawingPanel().drawAll();
        waitForUI();

        AlignPosition initialPos1 = seq1.getInitialAlignmentAsPosition();
        AlignPosition initialPos2 = seq2.getInitialAlignmentAsPosition();
        double currentScale = com.trackdraw.config.GlobalScale.getScale();
        double initialX1 = initialPos1.getX() * currentScale;
        double initialX2 = initialPos2.getX() * currentScale;

        // Call adjustAlignmentPosition directly (mocking Shift+Right Arrow - move all sequences)
        AlignmentKeyboardController keyboardController = mainWindow.getKeyboardController();
        keyboardController.adjustAlignmentPosition(10, 0, 0, false); // Move all sequences right by 10 pixels
        waitForUI();

        // Both sequences should move
        AlignPosition newPos1 = seq1.getInitialAlignmentAsPosition();
        AlignPosition newPos2 = seq2.getInitialAlignmentAsPosition();
        double newX1 = newPos1.getX() * currentScale;
        double newX2 = newPos2.getX() * currentScale;
        assertThat(newX1).isCloseTo(initialX1 + 10, org.assertj.core.data.Offset.offset(0.1));
        assertThat(newX2).isCloseTo(initialX2 + 10, org.assertj.core.data.Offset.offset(0.1));
    }

    @Test
    @DisplayName("Should move sequence in all directions")
    public void testMoveSequenceInAllDirections() throws IOException {
        waitForUI();

        SequenceManager manager = new SequenceManager();
        SequenceManager.LoadResult result = loadTestSequence(manager);

        ShapeSequence seq = result.getSequences().get(0);
        seq.setActive(true);
        mainWindow.getAllSequences().clear();
        mainWindow.getAllSequences().add(seq);
        mainWindow.getShapeSequencePanel().setSequences(mainWindow.getAllSequences());
        mainWindow.getDrawingPanel().setSequences(mainWindow.getAllSequences());

        mainWindow.getDrawingPanel().drawAll();
        waitForUI();

        AlignPosition initialPos = seq.getInitialAlignmentAsPosition();
        double currentScale = com.trackdraw.config.GlobalScale.getScale();
        double initialX = initialPos.getX() * currentScale;
        double initialY = initialPos.getY() * currentScale;

        // Call adjustAlignmentPosition directly for each direction
        AlignmentKeyboardController keyboardController = mainWindow.getKeyboardController();

        // Move right
        keyboardController.adjustAlignmentPosition(10, 0, 0, true);
        waitForUI();

        AlignPosition pos = seq.getInitialAlignmentAsPosition();
        double posX = pos.getX() * currentScale;
        double posY = pos.getY() * currentScale;
        assertThat(posX).isCloseTo(initialX + 10, org.assertj.core.data.Offset.offset(0.1));
        assertThat(posY).isCloseTo(initialY, org.assertj.core.data.Offset.offset(0.1));

        // Move down
        keyboardController.adjustAlignmentPosition(0, 10, 0, true);
        waitForUI();

        pos = seq.getInitialAlignmentAsPosition();
        posX = pos.getX() * currentScale;
        posY = pos.getY() * currentScale;
        assertThat(posX).isCloseTo(initialX + 10, org.assertj.core.data.Offset.offset(0.1));
        assertThat(posY).isCloseTo(initialY + 10, org.assertj.core.data.Offset.offset(0.1));

        // Move left
        keyboardController.adjustAlignmentPosition(-10, 0, 0, true);
        waitForUI();

        pos = seq.getInitialAlignmentAsPosition();
        posX = pos.getX() * currentScale;
        posY = pos.getY() * currentScale;
        assertThat(posX).isCloseTo(initialX, org.assertj.core.data.Offset.offset(0.1));
        assertThat(posY).isCloseTo(initialY + 10, org.assertj.core.data.Offset.offset(0.1));

        // Move up
        keyboardController.adjustAlignmentPosition(0, -10, 0, true);
        waitForUI();

        pos = seq.getInitialAlignmentAsPosition();
        posX = pos.getX() * currentScale;
        posY = pos.getY() * currentScale;
        assertThat(posX).isCloseTo(initialX, org.assertj.core.data.Offset.offset(0.1));
        assertThat(posY).isCloseTo(initialY, org.assertj.core.data.Offset.offset(0.1));
    }

    /**
     * Loads test sequence from test resources.
     */
    private SequenceManager.LoadResult loadTestSequence(SequenceManager manager) throws IOException {
        // Try to load from test resources first
        try(java.io.InputStream inputStream = getClass().getClassLoader().getResourceAsStream("test_sequence_with_shapes.json")) {
            if (inputStream != null) {
                // Copy to temp file and load
                File tempFile = File.createTempFile("test_sequence", ".json");
                tempFile.deleteOnExit();
                try (java.io.FileOutputStream out = new java.io.FileOutputStream(tempFile)) {
                    inputStream.transferTo(out);
                }
                return manager.loadSequences(tempFile.getAbsolutePath());
            }
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

