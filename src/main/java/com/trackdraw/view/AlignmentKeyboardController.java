package com.trackdraw.view;

import com.trackdraw.model.AlignPosition;
import com.trackdraw.model.ShapeInstance;
import com.trackdraw.model.ShapeSequence;

import javax.swing.*;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.List;

/**
 * Controller for keyboard controls to adjust alignment position of sequences.
 */
public class AlignmentKeyboardController {
    private DrawingPanel drawingPanel;
    private ShapeSequencePanel shapeSequencePanel;
    private DrawingCoordinator drawingCoordinator;
    
    public AlignmentKeyboardController(DrawingPanel drawingPanel, ShapeSequencePanel shapeSequencePanel,
                                      DrawingCoordinator drawingCoordinator) {
        this.drawingPanel = drawingPanel;
        this.shapeSequencePanel = shapeSequencePanel;
        this.drawingCoordinator = drawingCoordinator;
    }
    
    /**
     * Sets up keyboard controls for adjusting the active sequence's alignment position.
     * Only works if the active sequence is not linked to another shape.
     */
    public void setupKeyboardControls() {
        // Make drawing panel focusable to receive keyboard events
        drawingPanel.setFocusable(true);
        
        // Use KeyEventDispatcher to intercept arrow keys globally before components consume them
        // This ensures arrow keys work even when JList or JTextField have focus
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {
            @Override
            public boolean dispatchKeyEvent(KeyEvent e) {
                // Only handle key pressed events for arrow keys
                if (e.getID() != KeyEvent.KEY_PRESSED) {
                    return false; // Let other events pass through
                }
                
                int keyCode = e.getKeyCode();
                boolean isArrowKey = (keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_DOWN || 
                                     keyCode == KeyEvent.VK_LEFT || keyCode == KeyEvent.VK_RIGHT);
                
                if (!isArrowKey) {
                    return false; // Not an arrow key, let it pass through
                }
                
                // Check modifiers
                boolean shiftPressed = (e.getModifiersEx() & KeyEvent.SHIFT_DOWN_MASK) != 0;
                boolean ctrlPressed = (e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0;
                int delta = (shiftPressed && ctrlPressed) ? 1 : (ctrlPressed ? 1 : 10);
                
                // Handle arrow key movement
                if (shiftPressed) {
                    // Shift + Arrow: move all sequences
                    switch (keyCode) {
                        case KeyEvent.VK_UP:
                            adjustAlignmentPosition(0, -delta, 0, false);
                            e.consume();
                            return true;
                        case KeyEvent.VK_DOWN:
                            adjustAlignmentPosition(0, delta, 0, false);
                            e.consume();
                            return true;
                        case KeyEvent.VK_LEFT:
                            adjustAlignmentPosition(-delta, 0, 0, false);
                            e.consume();
                            return true;
                        case KeyEvent.VK_RIGHT:
                            adjustAlignmentPosition(delta, 0, 0, false);
                            e.consume();
                            return true;
                    }
                } else {
                    // Regular Arrow: move active sequence only
                    // Check if we have an active sequence that's not linked
                    ShapeSequence activeSequence = shapeSequencePanel.getActiveSequence();
                    if (activeSequence == null || activeSequence.isEmpty() || 
                        activeSequence.getInitialAlignmentAsShape() != null) {
                        return false; // No active sequence or sequence is linked, let default behavior happen
                    }
                    
                    switch (keyCode) {
                        case KeyEvent.VK_UP:
                            adjustAlignmentPosition(0, -delta, 0, true);
                            e.consume();
                            return true;
                        case KeyEvent.VK_DOWN:
                            adjustAlignmentPosition(0, delta, 0, true);
                            e.consume();
                            return true;
                        case KeyEvent.VK_LEFT:
                            adjustAlignmentPosition(-delta, 0, 0, true);
                            e.consume();
                            return true;
                        case KeyEvent.VK_RIGHT:
                            adjustAlignmentPosition(delta, 0, 0, true);
                            e.consume();
                            return true;
                    }
                }
                
                return false;
            }
        });
        
        // Also set up key bindings for rotation (plus/minus keys) using root pane
        JRootPane rootPane = SwingUtilities.getRootPane(drawingPanel);
        InputMap inputMap = rootPane != null 
            ? rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            : drawingPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = rootPane != null 
            ? rootPane.getActionMap()
            : drawingPanel.getActionMap();
        
        // Plus and Minus: rotate by 10 degrees
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, 0), "rotateCW10");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, 0), "rotateCW10"); // Also handle = key
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, 0), "rotateCCW10");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT, 0), "rotateCCW10"); // Numpad minus
        
        // Plus and Minus + Ctrl: rotate by 1 degree
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, KeyEvent.CTRL_DOWN_MASK), "rotateCW1");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, KeyEvent.CTRL_DOWN_MASK), "rotateCW1");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, KeyEvent.CTRL_DOWN_MASK), "rotateCCW1");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT, KeyEvent.CTRL_DOWN_MASK), "rotateCCW1");
        
        // Define rotation actions
        actionMap.put("rotateCCW10", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                adjustAlignmentPosition(0, 0, 10, true);
            }
        });
        
        actionMap.put("rotateCW10", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                adjustAlignmentPosition(0, 0, -10, true);
            }
        });
        
        actionMap.put("rotateCCW1", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                adjustAlignmentPosition(0, 0, 1, true);
            }
        });
        
        actionMap.put("rotateCW1", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                adjustAlignmentPosition(0, 0, -1, true);
            }
        });
    }
    
    /**
     * Adjusts the alignment position of sequences.
     * Only works if sequences are not linked to another shape.
     * @param deltaX Change in X position
     * @param deltaY Change in Y position
     * @param deltaAngle Change in rotation angle (degrees, positive = CCW)
     * @param onlyActive If true, only adjust the active sequence; if false, adjust all unlinked sequences
     */
    private void adjustAlignmentPosition(double deltaX, double deltaY, double deltaAngle, boolean onlyActive) {
        List<ShapeSequence> sequencesToProcess;
        
        if (onlyActive) {
            // Only process the active sequence
            ShapeSequence activeSequence = shapeSequencePanel.getActiveSequence();
            if (activeSequence == null || activeSequence.isEmpty()) {
                return;
            }
            // Check if sequence is linked to another shape - if so, don't adjust
            if (activeSequence.getInitialAlignmentAsShape() != null) {
                return;
            }
            sequencesToProcess = List.of(activeSequence);
        } else {
            // Process all unlinked sequences
            List<ShapeSequence> allSequences = shapeSequencePanel.getSequences();
            if (allSequences.isEmpty()) {
                return;
            }
            sequencesToProcess = allSequences;
        }
        
        // Get current scale - positions are stored relative to (0,0) at scale 1.0
        double currentScale = com.trackdraw.config.GlobalScale.getScale();
        
        // Process each sequence
        for (ShapeSequence sequence : sequencesToProcess) {
            // Skip sequences that are linked to another shape (only relevant when processing all)
            if (!onlyActive && sequence.getInitialAlignmentAsShape() != null) {
                continue;
            }
            
            // Skip empty sequences
            if (sequence.isEmpty()) {
                continue;
            }
            
            // Get or create the initial alignment position
            AlignPosition alignPos = sequence.getInitialAlignmentAsPosition();
            
            if (alignPos == null) {
                // If no alignment position exists, we need to get the current visual position
                // by checking where the sequence is actually drawn, then store it relative to (0,0)
                ShapeInstance firstShape = sequence.getShape(0);
                if (firstShape == null) {
                    continue;
                }
                
                // Check if first shape has alignPosition set (from previous drawing)
                AlignPosition firstShapeAlignPos = firstShape.getAlignPosition();
                if (firstShapeAlignPos != null) {
                    // Use the actual drawn position, normalize to scale 1.0 relative to (0,0)
                    double currentX = firstShapeAlignPos.getX();
                    double currentY = firstShapeAlignPos.getY();
                    // Normalize to scale 1.0 (relative to 0,0)
                    double normalizedX = currentX / currentScale;
                    double normalizedY = currentY / currentScale;
                    alignPos = new AlignPosition(normalizedX, normalizedY, firstShapeAlignPos.getAngle());
                } else {
                    // No drawn position yet - create one at center, normalized to scale 1.0 relative to (0,0)
                    double canvasCenterX = drawingPanel.getWidth() / 2.0;
                    double canvasCenterY = drawingPanel.getHeight() / 2.0;
                    double initialRotationAngle = 0.0;
                    // Calculate the first short side center from the desired shape center
                    double[] firstSideCenter = firstShape.calculateAlignPositionFromCenter(canvasCenterX, canvasCenterY, initialRotationAngle);
                    // Normalize to scale 1.0 (relative to 0,0) - store the actual position, not offset from center
                    double normalizedX = firstSideCenter[0] / currentScale;
                    double normalizedY = firstSideCenter[1] / currentScale;
                    alignPos = new AlignPosition(normalizedX, normalizedY, initialRotationAngle);
                }
                sequence.setInitialAlignment(alignPos);
            }
            
            // Convert stored position (relative to 0,0 at scale 1.0) to current scale
            double currentX = alignPos.getX() * currentScale;
            double currentY = alignPos.getY() * currentScale;
            
            // Adjust the position at current scale
            currentX += deltaX;
            currentY += deltaY;
            
            // Normalize back to scale 1.0 relative to (0,0) for storage
            alignPos.setX(currentX / currentScale);
            alignPos.setY(currentY / currentScale);
            alignPos.setAngle(alignPos.getAngle() + deltaAngle);
            
            // Normalize angle to 0-360 range
            double angle = alignPos.getAngle();
            while (angle < 0) angle += 360;
            while (angle >= 360) angle -= 360;
            alignPos.setAngle(angle);
        }
        
        // Redraw
        drawingCoordinator.drawAll();
    }
}

