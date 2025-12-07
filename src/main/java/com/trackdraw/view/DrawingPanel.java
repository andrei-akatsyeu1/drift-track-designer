package com.trackdraw.view;

import com.trackdraw.model.AlignPosition;
import com.trackdraw.model.ShapeSequence;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel for drawing geometric shapes.
 */
public class DrawingPanel extends JPanel {
    private List<ShapeSequence> sequences = new ArrayList<>();
    
    public DrawingPanel() {
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(800, 600));
    }
    
    /**
     * Sets the list of sequences to draw.
     * @param sequences List of ShapeSequence objects
     */
    public void setSequences(List<ShapeSequence> sequences) {
        this.sequences = sequences != null ? new ArrayList<>(sequences) : new ArrayList<>();
        repaint();
    }
    
    /**
     * Gets the list of sequences.
     * @return List of ShapeSequence objects
     */
    public List<ShapeSequence> getSequences() {
        return new ArrayList<>(sequences);
    }
    
    /**
     * Draws all shapes in the sequence following the workflow:
     * 3.3) Canvas cleared
     * 3.4) Start drawing all shapes from the list by sequence
     * 3.5) For the first shape, set default AlignPosition (center of canvas, angle 0)
     * 3.6) Draw the first shape, set the returned AlignPosition to the next shape
     * 3.7) Loop till the end of the shape list
     */
    public void drawAll() {
        repaint();
    }
    
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2d = (Graphics2D) g;
        
        // Enable anti-aliasing for smoother shapes
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2.0f));
        
        // 3.3) Canvas cleared (already done by super.paint())
        
        // Draw all sequences (not just active ones)
        for (ShapeSequence sequence : sequences) {
            if (sequence.isEmpty()) {
                continue;
            }
            
            // Determine initial alignment position
            AlignPosition initialAlignPosition = sequence.getEffectiveInitialAlignment();

            if (initialAlignPosition == null) {
                // If no initial alignment set, use center of canvas
                double desiredCenterX = getWidth() / 2.0;
                double desiredCenterY = getHeight() / 2.0;
                double initialRotationAngle = 0.0;

                initialAlignPosition = new AlignPosition(desiredCenterX, desiredCenterY, initialRotationAngle);
            }
            
            // Draw the sequence
            sequence.drawAll(g2d, initialAlignPosition);
        }
    }
}

