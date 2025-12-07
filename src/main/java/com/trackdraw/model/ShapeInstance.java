package com.trackdraw.model;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.util.UUID;

/**
 * Abstract base class for all shape instances.
 * Contains instance-specific properties like alignment position, colors, and orientation.
 */
public abstract class ShapeInstance {
    private UUID id;
    protected AlignPosition alignPosition; // Position where this shape should be aligned to (null for first shape)
    protected Color contourColor;
    protected Color infillColor;
    protected int orientation; // 1 or -1 for AnnularSector (convenient for math operations), always 1 for Rectangle
    protected boolean active; // Whether this shape is active
    protected boolean isRed; // Color flag: true = red, false = white (default)
    
    public ShapeInstance() {
        this.id = UUID.randomUUID();
        this.contourColor = Color.BLACK;
        this.infillColor = Color.WHITE;
        this.orientation = 1; // Default orientation
        this.active = false; // Default to not active
        this.isRed = false; // Default to white
    }
    
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public AlignPosition getAlignPosition() {
        return alignPosition;
    }
    
    public void setAlignPosition(AlignPosition alignPosition) {
        this.alignPosition = alignPosition;
    }
    
    public Color getContourColor() {
        return contourColor;
    }
    
    public void setContourColor(Color contourColor) {
        this.contourColor = contourColor;
    }
    
    public Color getInfillColor() {
        return infillColor;
    }
    
    public void setInfillColor(Color infillColor) {
        this.infillColor = infillColor;
    }
    
    public int getOrientation() {
        return orientation;
    }
    
    public void setOrientation(int orientation) {
        // Ensure orientation is either 1 or -1
        if (orientation != 1 && orientation != -1) {
            throw new IllegalArgumentException("Orientation must be 1 or -1");
        }
        this.orientation = orientation;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }
    
    public boolean isRed() {
        return isRed;
    }
    
    public void setRed(boolean isRed) {
        this.isRed = isRed;
    }
    
    /**
     * Gets the unique key/identifier for this shape.
     * @return shape key
     */
    public abstract String getKey();
    
    /**
     * Gets the type of shape (e.g., "annular_sector", "rectangle").
     * @return shape type
     */
    public abstract String getType();
    
    /**
     * Sets up the graphics transform and colors for drawing.
     * Applies rotation around the center point and sets the contour color.
     * 
     * @param g2d Graphics2D context to draw on
     * @return The original transform (to be restored after drawing)
     */
    protected AffineTransform setupDrawing(Graphics2D g2d) {
        // alignPosition must be set before calling draw
        // alignPosition represents the first short side center
        // Calculate the actual shape center from it
        double[] center = calculateCenterFromAlignPosition();
        double centerX = center[0];
        double centerY = center[1];
        double rotationAngle = alignPosition.getAngle();
        
        // Save original transform
        AffineTransform originalTransform = g2d.getTransform();
        
        // Apply rotation around center point
        g2d.translate(centerX, centerY);
        g2d.rotate(Math.toRadians(-rotationAngle));
        g2d.translate(-centerX, -centerY);

        // Set infill color (will be used for filling)
        g2d.setColor(infillColor);
        
        return originalTransform;
    }
    
    /**
     * Calculates the actual shape center from alignPosition.
     * alignPosition represents the first short side center, and this method
     * calculates where the shape center should be.
     * 
     * @return array [centerX, centerY]
     */
    protected double[] calculateCenterFromAlignPosition() {
        //calculation hack: the diff between method is what we need to substract offset instead of adding.
        // Adding 180 to angle change sign of sin and cos, what change sign of offset
        return calculateAlignPositionFromCenter(alignPosition.getX(), alignPosition.getY(), alignPosition.getAngle() + (orientation == 1 ? 180 : 0));
    }
    
    /**
     * Calculates the first short side center position from a desired shape center.
     * Used for positioning the first shape in the sequence.
     * 
     * @param desiredCenterX Desired shape center X coordinate
     * @param desiredCenterY Desired shape center Y coordinate
     * @param rotationAngle Rotation angle in degrees
     * @return array [firstSideCenterX, firstSideCenterY]
     */
    public abstract double[] calculateAlignPositionFromCenter(double desiredCenterX, double desiredCenterY, double rotationAngle);
    
    /**
     * Gets the center X coordinate.
     * Calculated from alignPosition (first short side center).
     */
    protected double getCenterX() {
        return calculateCenterFromAlignPosition()[0];
    }
    
    /**
     * Gets the center Y coordinate.
     * Calculated from alignPosition (first short side center).
     */
    protected double getCenterY() {
        return calculateCenterFromAlignPosition()[1];
    }
    
    /**
     * Gets the rotation angle from alignPosition.
     */
    protected double getRotationAngle() {
        return alignPosition.getAngle();
    }
    
    /**
     * Draws this shape instance on the given Graphics2D context.
     * Uses the alignPosition to determine where and how to draw the shape.
     * This method contains all common logic and delegates to doDraw() and getAlignPosition().
     * 
     * @param g2d Graphics2D context to draw on
     * @return AlignPosition for the next shape in the sequence
     */
    public AlignPosition draw(Graphics2D g2d) {
        // Setup transform and colors
        AffineTransform originalTransform = setupDrawing(g2d);
        
        try {
            // Delegate actual drawing to subclass
            doDraw(g2d);
        } finally {
            // Always restore original transform
            g2d.setTransform(originalTransform);
        }
        
        // Calculate and return next align position
        return calculateNextAlignPosition();
    }
    
    /**
     * Performs the actual drawing of the shape.
     * Called after transform and colors are set up.
     * 
     * @param g2d Graphics2D context to draw on (transform already applied)
     */
    protected abstract void doDraw(Graphics2D g2d);
    
    /**
     * Calculates and returns the AlignPosition for the next shape in the sequence.
     * This position represents where the next shape's first short side should connect.
     * 
     * @return AlignPosition for the next shape
     */
    protected abstract AlignPosition calculateNextAlignPosition();
}

