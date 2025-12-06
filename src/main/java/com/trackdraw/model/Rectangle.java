package com.trackdraw.model;

import com.trackdraw.config.GlobalScale;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

/**
 * Represents a rectangle shape.
 * Defined by length and width.
 */
public class Rectangle extends ShapeInstance {
    private String key;
    private double length;
    private double width;
    
    public Rectangle() {
    }
    
    public Rectangle(String key, double length, double width) {
        this.key = key;
        this.length = length;
        this.width = width;
    }
    
    @Override
    public String getKey() {
        return key;
    }
    
    @Override
    public String getType() {
        return "rectangle";
    }
    
    public double getLength() {
        return length;
    }
    
    public void setLength(double length) {
        this.length = length;
    }
    
    public double getWidth() {
        return width;
    }
    
    public void setWidth(double width) {
        this.width = width;
    }

    @Override
    public double[] calculateAlignPositionFromCenter(double centerX, double centerY, double rotationAngle) {
        // Calculate first short side center from desired shape center
        // In local coordinates, first short side center is at (0, -width/2)
        // After rotation: firstSideCenter = center + rotate(0, -width/2)
        
        double scale = GlobalScale.getScale();
        double scaledLength = length * scale;
        
        // Rotate (0, -width/2) by rotation angle
        // rotate(0, -width/2) = (0*cos - (-width/2)*sin, 0*sin + (-width/2)*cos)
        // = (width/2 * sin, -width/2 * cos)
        double rotationRad = Math.toRadians(rotationAngle);
        double offsetX = (scaledLength / 2.0) * Math.sin(rotationRad);
        double offsetY = (scaledLength / 2.0) * Math.cos(rotationRad);
        
        // First side center = shape center + offset
        double firstSideX = centerX + offsetX;
        double firstSideY = centerY + offsetY;
        
        return new double[]{firstSideX, firstSideY};
    }
    
    @Override
    protected void doDraw(Graphics2D g2d) {
        // Orientation is always 1 for rectangles
        orientation = 1;
        
        // Get center coordinates
        double centerX = getCenterX();
        double centerY = getCenterY();
        
        // Draw the rectangle
        double scale = GlobalScale.getScale();
        double scaledWidth = width * scale;
        double scaledLength = length * scale;
        
        double x = centerX - scaledWidth / 2;
        double y = centerY - scaledLength / 2;
        
        Rectangle2D.Double rectangle = new Rectangle2D.Double(x, y, scaledWidth, scaledLength);
        g2d.draw(rectangle);
    }
    
    @Override
    protected AlignPosition calculateNextAlignPosition() {
        double scale = GlobalScale.getScale();
        double scaledLength = length * scale;

        // Rotate (0, -width/2) by rotation angle
        // rotate(0, -width/2) = (0*cos - (-width/2)*sin, 0*sin + (-width/2)*cos)
        // = (width/2 * sin, -width/2 * cos)
        double rotationRad = Math.toRadians(alignPosition.getAngle());
        double offsetX = scaledLength * Math.sin(rotationRad);
        double offsetY = scaledLength * Math.cos(rotationRad);

        // Return the second short side center position and angle
        // The next shape will use this to position its first short side center
        return new AlignPosition(alignPosition.getX() - offsetX, alignPosition.getY() - offsetY, alignPosition.getAngle());
    }
}

