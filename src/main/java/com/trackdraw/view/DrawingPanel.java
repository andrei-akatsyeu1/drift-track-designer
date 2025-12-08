package com.trackdraw.view;

import com.trackdraw.config.GlobalScale;
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
        setFocusable(true);
        
        // Request focus when clicked so keyboard controls work
        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                requestFocusInWindow();
            }
        });
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
        // Get canvas center for scaling reference
        double canvasCenterX = getWidth() / 2.0;
        double canvasCenterY = getHeight() / 2.0;
        double currentScale = GlobalScale.getScale();
        
        for (ShapeSequence sequence : sequences) {
            if (sequence.isEmpty()) {
                continue;
            }
            
            // Determine initial alignment position
            AlignPosition initialAlignPosition = null;
            
            // Check if sequence is linked to a shape (linked sequences don't need scaling)
            if (sequence.getInitialAlignmentAsShape() != null) {
                // Linked sequence - use effective alignment directly (already calculated correctly)
                initialAlignPosition = sequence.getEffectiveInitialAlignment();
            } else {
                // Independent sequence - scale the stored position
                AlignPosition storedPosition = sequence.getInitialAlignmentAsPosition();
                
                if (storedPosition != null) {
                    // Scale position relative to canvas center
                    // Position is stored at scale 1.0, so scale by current scale
                    double offsetX = storedPosition.getX() - canvasCenterX;
                    double offsetY = storedPosition.getY() - canvasCenterY;
                    
                    double scaledX = canvasCenterX + offsetX * currentScale;
                    double scaledY = canvasCenterY + offsetY * currentScale;
                    
                    initialAlignPosition = new AlignPosition(scaledX, scaledY, storedPosition.getAngle());
                } else {
                    // No stored position - use center of canvas
                    initialAlignPosition = new AlignPosition(canvasCenterX, canvasCenterY, 0.0);
                }
            }

            if (initialAlignPosition == null) {
                // Fallback: use center of canvas
                initialAlignPosition = new AlignPosition(canvasCenterX, canvasCenterY, 0.0);
            }
            
            // Draw the sequence
            sequence.drawAll(g2d, initialAlignPosition);
        }
    }
}

