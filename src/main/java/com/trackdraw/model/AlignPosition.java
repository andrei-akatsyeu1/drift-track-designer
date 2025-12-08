package com.trackdraw.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents the alignment position for a shape.
 * Contains the position (x, y) and rotation angle where a shape should be drawn.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlignPosition {
    private double x;
    private double y;
    private double angle; // Rotation angle in degrees
    
    @Override
    public String toString() {
        return String.format("AlignPosition(x=%.2f, y=%.2f, angle=%.2f°)", x, y, angle);
    }
}

