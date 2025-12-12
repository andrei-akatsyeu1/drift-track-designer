package com.trackdraw.model;

import com.trackdraw.config.GlobalScale;
import lombok.Getter;
import lombok.Setter;

import java.awt.*;
import java.awt.geom.Arc2D;
import java.util.UUID;

/**
 * Represents a half-circle shape.
 * Defined by diameter (which equals width).
 * This is a closing shape that can be aligned by diameter.
 */
@Getter
@Setter
public class HalfCircle extends ShapeInstance {
    private double diameter; // diameter = width = 6
    
    public HalfCircle() {
    }
    
    public HalfCircle(String key, double diameter) {
        this.key = key;
        this.diameter = diameter;
    }
    
    @Override
    public String getType() {
        return "half_circle";
    }
    
    public double getWidth() {
        return diameter; // width equals diameter
    }
    
    @Override
    public ShapeInstance copy() {
        HalfCircle copy = new HalfCircle(key, diameter);
        copy.setId(UUID.randomUUID()); // New UUID
        copy.setOrientation(orientation);
        copy.setRed(isRed);
        copy.setForceInvertColor(forceInvertColor);
        copy.setContourColor(contourColor);
        copy.setInfillColor(infillColor);
        copy.setActive(active);
        copy.setAlignPosition(alignPosition != null ? new AlignPosition(alignPosition.getX(), alignPosition.getY(), alignPosition.getAngle()) : null);
        return copy;
    }
    
    @Override
    public double[] calculateAlignPositionFromCenter(double centerX, double centerY, double rotationAngle) {
        // For half-circle, alignPosition represents the center of the diameter
        // The diameter is perpendicular to the rotation angle
        // The shape center is at the center of the diameter
        // So alignPosition IS the shape center
        return new double[]{centerX, centerY};
    }
    
    @Override
    protected void doDraw(Graphics2D g2d) {
        // Orientation is always 1 for half-circle
        orientation = 1;
        
        // Get center coordinates (which equals alignPosition for half-circle)
        // Transform is already applied in setupDrawing, so we draw in local coordinates
        double centerX = getCenterX();
        double centerY = getCenterY();
        
        // Draw the half-circle
        double scale = GlobalScale.getScale();
        double radius = (diameter * scale) / 2.0;
        
        // Create a path for the half-circle
        // The half-circle is drawn as a semicircle (180 degrees)
        // The diameter is horizontal (perpendicular to rotation angle after transform)
        // In local coordinates, diameter is horizontal, arc is above it
        
        java.awt.geom.Path2D.Double path = new java.awt.geom.Path2D.Double();
        
        // Start point of diameter (left side)
        double startX = centerX - radius;
        double startY = centerY;
        
        // Create arc for the semicircle (180 degrees, starting from right, going counterclockwise)
        Arc2D.Double arc = new Arc2D.Double(
            centerX - radius, centerY - radius,
            radius * 2, radius * 2,
            0.0, 180.0, // Start at 0 degrees (right), sweep 180 degrees counterclockwise
            Arc2D.OPEN
        );
        
        // Build the path: start at left end of diameter, draw arc, close with diameter
        path.moveTo(startX, startY);
        path.append(arc, true);
        path.closePath(); // Close the path back to start
        
        // Fill the shape with infill color, then draw outline with contour color
        g2d.fill(path);
        g2d.setColor(contourColor);
        g2d.draw(path);
    }
    
    @Override
    protected AlignPosition calculateNextAlignPosition() {
        // This is a closing shape - return the same alignPosition
        // The next shape (if any) will align to the same point
        return new AlignPosition(alignPosition.getX(), alignPosition.getY(), alignPosition.getAngle());
    }
    
    @Override
    public void drawText(Graphics2D g2d, boolean showKeys) {
        // Do not show text on HalfCircle
        // Override to do nothing
    }
}

