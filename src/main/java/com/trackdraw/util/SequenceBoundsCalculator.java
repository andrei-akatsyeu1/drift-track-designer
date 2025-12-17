package com.trackdraw.util;

import com.trackdraw.config.GlobalScale;
import com.trackdraw.model.AlignPosition;
import com.trackdraw.model.ShapeSequence;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Utility class for calculating bounds of sequences without background image.
 * Used for reporting width and length of sequences.
 */
public class SequenceBoundsCalculator {
    
    /**
     * Calculates the bounding box of all sequences (without background image).
     * Rule: Calculate min/max x/y of all align positions, add 20px padding.
     * Returns bounds at current GlobalScale (not normalized to scale 1.0).
     * 
     * This method does NOT modify GlobalScale to avoid interfering with export operations.
     * 
     * @param sequences List of sequences to calculate bounds for
     * @return Rectangle2D.Double representing the bounds, or null if no sequences
     */
    public static Rectangle2D.Double calculateSequenceBounds(List<ShapeSequence> sequences) {
        if (sequences == null || sequences.isEmpty()) {
            return null;
        }
        
        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE;
        double maxY = Double.MIN_VALUE;
        
        // Collect all align positions from sequences at current scale
        BufferedImage tempImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        Graphics2D tempG2d = tempImage.createGraphics();
        tempG2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        try {
            for (ShapeSequence sequence : sequences) {
                if (sequence.isEmpty()) {
                    continue;
                }
                
                // Get initial position at current scale
                AlignPosition initialPos = getInitialAlignPosition(sequence);
                if (initialPos == null) {
                    continue;
                }
                
                // Calculate all positions at current scale
                List<AlignPosition> positions = sequence.getAllAlignPositions(initialPos, tempG2d);
                for (AlignPosition pos : positions) {
                    minX = Math.min(minX, pos.getX());
                    minY = Math.min(minY, pos.getY());
                    maxX = Math.max(maxX, pos.getX());
                    maxY = Math.max(maxY, pos.getY());
                }
            }
        } finally {
            tempG2d.dispose();
        }
        
        if (minX == Double.MAX_VALUE) {
            // No bounds found
            return null;
        }
        
        // Add 20px padding for sequences (at current scale)
        double currentScale = GlobalScale.getScale();
        double padding = 20.0 * currentScale;
        minX = minX - padding;
        minY = minY - padding;
        maxX = maxX + padding;
        maxY = maxY + padding;
        
        // Return bounds at current scale
        return new Rectangle2D.Double(minX, minY, maxX - minX, maxY - minY);
    }
    
    /**
     * Gets the initial align position for a sequence at the current GlobalScale.
     * 
     * @param sequence The sequence to get initial position for
     * @return AlignPosition at current scale, or null if not available
     */
    private static AlignPosition getInitialAlignPosition(ShapeSequence sequence) {
        if (sequence.getInitialAlignmentAsShape() != null) {
            // For linked sequences, get effective alignment at current scale
            return sequence.getEffectiveInitialAlignment();
        } else {
            AlignPosition storedPosition = sequence.getInitialAlignmentAsPosition();
            if (storedPosition != null) {
                // Stored position is relative to (0,0) at scale 1.0
                // Scale it to current scale
                double currentScale = GlobalScale.getScale();
                return new AlignPosition(
                    storedPosition.getX() * currentScale,
                    storedPosition.getY() * currentScale,
                    storedPosition.getAngle()
                );
            }
        }
        return null;
    }
}

