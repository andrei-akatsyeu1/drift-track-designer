package com.trackdraw.model;

import com.trackdraw.config.GlobalScale;
import lombok.Getter;
import lombok.Setter;

import java.awt.*;
import java.awt.geom.Arc2D;
import java.util.UUID;

/**
 * Represents an annular sector (ring segment) shape.
 * Defined by external diameter, angle in degrees, and width.
 */
@Getter
@Setter
public class AnnularSector extends ShapeInstance {
    private double externalDiameter;
    private double angleDegrees;
    private double width;
    
    public AnnularSector() {
    }
    
    public AnnularSector(String key, double externalDiameter, double angleDegrees, double width) {
        this.key = key;
        this.externalDiameter = externalDiameter;
        this.angleDegrees = angleDegrees;
        this.width = width;
    }
    
    @Override
    public String getType() {
        return "annular_sector";
    }
    
    /**
     * Calculates the internal diameter.
     * @return internal diameter
     */
    public double getInternalDiameter() {
        return externalDiameter - 2 * width;
    }
    
    @Override
    public ShapeInstance copy() {
        AnnularSector copy = new AnnularSector(key, externalDiameter, angleDegrees, width);
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
        // Calculate first short side center from desired shape center
        // In local coordinates, first short side center is at (midRadius, 0) at angle 0
        // After rotation: firstSideCenter = center + rotate(midRadius, 0)
        
        double scale = GlobalScale.getScale();
        double externalRadius = (externalDiameter * scale) / 2.0;
        double internalRadius = (getInternalDiameter() * scale) / 2.0;
        double midRadius = (externalRadius + internalRadius) / 2.0;
        
        // Rotate (midRadius, 0) by rotation angle
        double rotationRad = Math.toRadians(rotationAngle);
        double offsetX = midRadius * Math.cos(rotationRad);
        double offsetY = -midRadius * Math.sin(rotationRad);
        
        // First side center = shape center + offset
        double firstSideX = centerX + offsetX;
        double firstSideY = centerY + offsetY;
        
        return new double[]{firstSideX, firstSideY};
    }
    
    @Override
    protected void doDraw(Graphics2D g2d) {
        // Get center coordinates
        double centerX = getCenterX();
        double centerY = getCenterY();
        
        // Draw the annular sector
        double scale = GlobalScale.getScale();
        double externalRadius = (externalDiameter * scale) / 2.0;
        double internalRadius = (getInternalDiameter() * scale) / 2.0;
        double angleDeg = angleDegrees; // Orientation removed from calculation
        double angleRad = Math.toRadians(angleDeg);
        
        // Create path for the annular sector
        java.awt.geom.Path2D.Double path = new java.awt.geom.Path2D.Double();
        
        // Calculate key points
        double startOuterX = centerX + externalRadius * orientation;
        double startOuterY = centerY;
        double endInnerX = centerX + internalRadius * Math.cos(angleRad) * orientation;
        double endInnerY = centerY - internalRadius * Math.sin(angleRad);
        
        path.moveTo(startOuterX, startOuterY);
        
        // Ensure arc always sweeps counterclockwise (positive angle)
        double arcSweep = Math.abs(angleDeg);
        
        Arc2D.Double outerArc = new Arc2D.Double(
            centerX - externalRadius, centerY - externalRadius,
            externalRadius * 2, externalRadius * 2,
                (orientation == 1 ? 0 : 180), arcSweep * orientation,
            Arc2D.OPEN
        );
        path.append(outerArc, true);
        path.lineTo(endInnerX, endInnerY);
        
        Arc2D.Double innerArc = new Arc2D.Double(
            centerX - internalRadius, centerY - internalRadius,
            internalRadius * 2, internalRadius * 2,
            orientation == 1 ? arcSweep : 180 - arcSweep, -arcSweep * orientation,
            Arc2D.OPEN
        );
        
        java.awt.geom.Path2D.Double innerPath = new java.awt.geom.Path2D.Double();
        innerPath.moveTo(endInnerX, endInnerY);
        innerPath.append(innerArc, true);
        
        java.awt.geom.PathIterator pi = innerPath.getPathIterator(null);
        double[] coords = new double[6];
        boolean skipFirst = true;
        while (!pi.isDone()) {
            int type = pi.currentSegment(coords);
            if (skipFirst && type == java.awt.geom.PathIterator.SEG_MOVETO) {
                skipFirst = false;
                pi.next();
                continue;
            }
            if (type == java.awt.geom.PathIterator.SEG_LINETO) {
                path.lineTo(coords[0], coords[1]);
            } else if (type == java.awt.geom.PathIterator.SEG_QUADTO) {
                path.quadTo(coords[0], coords[1], coords[2], coords[3]);
            } else if (type == java.awt.geom.PathIterator.SEG_CUBICTO) {
                path.curveTo(coords[0], coords[1], coords[2], coords[3], coords[4], coords[5]);
            }
            pi.next();
        }
        
        path.closePath();
        
        // Fill the shape with infill color, then draw outline with contour color
        g2d.fill(path);
        g2d.setColor(contourColor);
        g2d.draw(path);
    }
    
    @Override
    protected AlignPosition calculateNextAlignPosition() {
        // Get actual shape center (calculated from alignPosition)
        double[] center = calculateCenterFromAlignPosition();

        double secondSideAngle = alignPosition.getAngle() + angleDegrees * orientation; // Orientation removed from calculation

        double[] nextAlignPosition = calculateAlignPositionFromCenter(center[0], center[1], secondSideAngle + (orientation == 1 ? 0 : 180));

        return new AlignPosition(nextAlignPosition[0], nextAlignPosition[1], secondSideAngle);
    }
}

