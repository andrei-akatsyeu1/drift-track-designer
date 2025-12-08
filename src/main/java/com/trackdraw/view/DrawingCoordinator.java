package com.trackdraw.view;

import com.trackdraw.model.ShapeSequence;

import java.util.List;

/**
 * Coordinates drawing operations and color recalculation for all sequences.
 */
public class DrawingCoordinator {
    private DrawingPanel drawingPanel;
    private ShapeSequencePanel shapeSequencePanel;
    private ShapeListPanel shapeListPanel;
    
    public DrawingCoordinator(DrawingPanel drawingPanel, ShapeSequencePanel shapeSequencePanel,
                              ShapeListPanel shapeListPanel) {
        this.drawingPanel = drawingPanel;
        this.shapeSequencePanel = shapeSequencePanel;
        this.shapeListPanel = shapeListPanel;
    }
    
    /**
     * Recalculates colors for all sequences.
     * Processes sequences in order - since sequences can only depend on sequences above them,
     * we can safely recalculate them in list order without additional dependency checks.
     */
    public void recalculateAllColors() {
        List<ShapeSequence> allSequences = shapeSequencePanel.getSequences();
        
        // Process sequences in order - each sequence can only depend on sequences above it
        // The linked shape's color is already calculated because it's from a sequence processed earlier
        for (ShapeSequence sequence : allSequences) {
            sequence.recalculateColors();
        }
    }
    
    /**
     * Draws all shapes following workflow:
     * 3.3) canvas cleared
     * 3.4) start draw all shapes from the list by sequence
     * 3.5) for first shape set default AlignPosition (center of canvas, angle 0)
     * 3.6) draw first shape, set returned AlignPosition to next shape
     * 3.7) loop till the end of shape list
     */
    public void drawAll() {
        List<ShapeSequence> allSequences = shapeSequencePanel.getSequences();
        ShapeSequence activeSequence = shapeSequencePanel.getActiveSequence();
        
        // Recalculate colors before drawing
        recalculateAllColors();
        
        // Update shape list for active sequence
        shapeListPanel.setActiveSequence(activeSequence);
        
        // Update drawing panel with all sequences
        drawingPanel.setSequences(allSequences);
        
        // Trigger repaint
        drawingPanel.drawAll();
    }
}

