package com.trackdraw.view;

import com.trackdraw.model.AlignPosition;
import com.trackdraw.model.ShapeInstance;
import com.trackdraw.model.ShapeSequence;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

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
        
        // Get input map and action map for key bindings
        InputMap inputMap = drawingPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = drawingPanel.getActionMap();
        
        // Arrow keys: move by 10 pixels
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "moveUp10");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "moveDown10");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "moveLeft10");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "moveRight10");
        
        // Arrow keys + Ctrl: move by 1 pixel
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, KeyEvent.CTRL_DOWN_MASK), "moveUp1");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, KeyEvent.CTRL_DOWN_MASK), "moveDown1");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, KeyEvent.CTRL_DOWN_MASK), "moveLeft1");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, KeyEvent.CTRL_DOWN_MASK), "moveRight1");
        
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
        
        // Define actions
        actionMap.put("moveUp10", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                adjustAlignmentPosition(0, -10, 0);
            }
        });
        
        actionMap.put("moveDown10", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                adjustAlignmentPosition(0, 10, 0);
            }
        });
        
        actionMap.put("moveLeft10", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                adjustAlignmentPosition(-10, 0, 0);
            }
        });
        
        actionMap.put("moveRight10", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                adjustAlignmentPosition(10, 0, 0);
            }
        });
        
        actionMap.put("moveUp1", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                adjustAlignmentPosition(0, -1, 0);
            }
        });
        
        actionMap.put("moveDown1", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                adjustAlignmentPosition(0, 1, 0);
            }
        });
        
        actionMap.put("moveLeft1", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                adjustAlignmentPosition(-1, 0, 0);
            }
        });
        
        actionMap.put("moveRight1", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                adjustAlignmentPosition(1, 0, 0);
            }
        });
        
        actionMap.put("rotateCCW10", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                adjustAlignmentPosition(0, 0, 10);
            }
        });
        
        actionMap.put("rotateCW10", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                adjustAlignmentPosition(0, 0, -10);
            }
        });
        
        actionMap.put("rotateCCW1", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                adjustAlignmentPosition(0, 0, 1);
            }
        });
        
        actionMap.put("rotateCW1", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                adjustAlignmentPosition(0, 0, -1);
            }
        });
    }
    
    /**
     * Adjusts the alignment position of the active sequence's first shape.
     * Only works if the sequence is not linked to another shape.
     * @param deltaX Change in X position
     * @param deltaY Change in Y position
     * @param deltaAngle Change in rotation angle (degrees, positive = CCW)
     */
    private void adjustAlignmentPosition(double deltaX, double deltaY, double deltaAngle) {
        ShapeSequence activeSequence = shapeSequencePanel.getActiveSequence();
        
        // Check if we have an active sequence
        if (activeSequence == null || activeSequence.isEmpty()) {
            return;
        }
        
        // Check if sequence is linked to another shape - if so, don't adjust
        if (activeSequence.getInitialAlignmentAsShape() != null) {
            return;
        }
        
        // Get or create the initial alignment position
        AlignPosition alignPos = activeSequence.getInitialAlignmentAsPosition();
        
        if (alignPos == null) {
            // If no alignment position exists, create one at center of canvas
            // Store it normalized to scale 1.0 (relative to canvas center)
            double canvasCenterX = drawingPanel.getWidth() / 2.0;
            double canvasCenterY = drawingPanel.getHeight() / 2.0;
            double initialRotationAngle = 0.0;
            
            ShapeInstance firstShape = activeSequence.getShape(0);
            if (firstShape == null) {
                return;
            }
            
            // Calculate the first short side center from the desired shape center
            double[] firstSideCenter = firstShape.calculateAlignPositionFromCenter(canvasCenterX, canvasCenterY, initialRotationAngle);
            // Store position normalized to scale 1.0 (relative to canvas center)
            alignPos = new AlignPosition(firstSideCenter[0], firstSideCenter[1], initialRotationAngle);
            activeSequence.setInitialAlignment(alignPos);
        }
        
        // Get current scale and canvas center for normalization
        double currentScale = com.trackdraw.config.GlobalScale.getScale();
        double canvasCenterX = drawingPanel.getWidth() / 2.0;
        double canvasCenterY = drawingPanel.getHeight() / 2.0;
        
        // Convert stored position (normalized to scale 1.0) to current scale for adjustment
        double currentX = canvasCenterX + (alignPos.getX() - canvasCenterX) * currentScale;
        double currentY = canvasCenterY + (alignPos.getY() - canvasCenterY) * currentScale;
        
        // Adjust the position at current scale
        currentX += deltaX;
        currentY += deltaY;
        
        // Normalize back to scale 1.0 for storage
        alignPos.setX(canvasCenterX + (currentX - canvasCenterX) / currentScale);
        alignPos.setY(canvasCenterY + (currentY - canvasCenterY) / currentScale);
        alignPos.setAngle(alignPos.getAngle() + deltaAngle);
        
        // Normalize angle to 0-360 range
        double angle = alignPos.getAngle();
        while (angle < 0) angle += 360;
        while (angle >= 360) angle -= 360;
        alignPos.setAngle(angle);
        
        // Redraw
        drawingCoordinator.drawAll();
    }
}

