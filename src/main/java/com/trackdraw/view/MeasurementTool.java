package com.trackdraw.view;

import com.trackdraw.config.GlobalScale;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

/**
 * Interactive measurement tool for measuring distances on the canvas.
 * Click two points to measure the distance between them.
 */
public class MeasurementTool {
    private Point2D.Double startPoint;
    private Point2D.Double endPoint;
    private boolean active;
    private boolean measuring; // true when first point is set, waiting for second
    
    public MeasurementTool() {
        this.active = false;
        this.measuring = false;
    }
    
    /**
     * Activates or deactivates the measurement tool.
     * @param active true to activate, false to deactivate
     */
    public void setActive(boolean active) {
        this.active = active;
        if (!active) {
            // Reset measurement when deactivating
            startPoint = null;
            endPoint = null;
            measuring = false;
        }
    }
    
    /**
     * Checks if the measurement tool is active.
     * @return true if active
     */
    public boolean isActive() {
        return active;
    }
    
    /**
     * Handles a mouse click for measurement.
     * @param x X coordinate
     * @param y Y coordinate
     * @return true if measurement is complete (both points set)
     */
    public boolean handleClick(double x, double y) {
        if (!active) {
            return false;
        }
        
        if (!measuring) {
            // First point
            startPoint = new Point2D.Double(x, y);
            endPoint = null;
            measuring = true;
            return false;
        } else {
            // Second point
            endPoint = new Point2D.Double(x, y);
            measuring = false;
            return true; // Measurement complete
        }
    }
    
    /**
     * Handles mouse movement to show preview line.
     * @param x X coordinate
     * @param y Y coordinate
     */
    public void handleMouseMove(double x, double y) {
        if (active && measuring && startPoint != null) {
            endPoint = new Point2D.Double(x, y);
        }
    }
    
    /**
     * Resets the current measurement.
     */
    public void reset() {
        startPoint = null;
        endPoint = null;
        measuring = false;
    }
    
    /**
     * Gets the measured distance in pixels.
     * @return Distance in pixels, or 0 if measurement is incomplete
     */
    public double getDistancePixels() {
        if (startPoint == null || endPoint == null) {
            return 0;
        }
        return startPoint.distance(endPoint);
    }
    
    /**
     * Gets the measured distance in scaled units (accounting for global scale).
     * @return Distance in scaled units, or 0 if measurement is incomplete
     */
    public double getDistanceScaled() {
        double pixelDistance = getDistancePixels();
        if (pixelDistance == 0) {
            return 0;
        }
        // Divide by global scale to get "real" units
        return pixelDistance / GlobalScale.getScale();
    }
    
    /**
     * Gets the start point of the measurement.
     * @return Start point, or null if not set
     */
    public Point2D.Double getStartPoint() {
        return startPoint;
    }
    
    /**
     * Gets the end point of the measurement.
     * @return End point, or null if not set
     */
    public Point2D.Double getEndPoint() {
        return endPoint;
    }
    
    /**
     * Checks if a measurement is in progress (first point set, waiting for second).
     * @return true if measuring
     */
    public boolean isMeasuring() {
        return measuring;
    }
    
    /**
     * Checks if a complete measurement exists (both points set).
     * @return true if measurement is complete
     */
    public boolean hasMeasurement() {
        return startPoint != null && endPoint != null && !measuring;
    }
}

