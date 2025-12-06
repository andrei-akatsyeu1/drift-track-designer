package com.trackdraw.model;

/**
 * Represents the alignment position for a shape.
 * Contains the position (x, y) and rotation angle where a shape should be drawn.
 */
public class AlignPosition {
    private double x;
    private double y;
    private double angle; // Rotation angle in degrees
    
    public AlignPosition() {
        this(0, 0, 0);
    }
    
    public AlignPosition(double x, double y, double angle) {
        this.x = x;
        this.y = y;
        this.angle = angle;
    }
    
    public double getX() {
        return x;
    }
    
    public void setX(double x) {
        this.x = x;
    }
    
    public double getY() {
        return y;
    }
    
    public void setY(double y) {
        this.y = y;
    }
    
    public double getAngle() {
        return angle;
    }
    
    public void setAngle(double angle) {
        this.angle = angle;
    }
    
    @Override
    public String toString() {
        return String.format("AlignPosition(x=%.2f, y=%.2f, angle=%.2f°)", x, y, angle);
    }
}

