package com.trackdraw.model;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a sequence of shapes with their initial alignment position.
 * The initial alignment position can be specified as either an AlignPosition
 * or a ShapeInstance (whose align position will be used).
 */
public class ShapeSequence {
    private String name;
    private List<ShapeInstance> shapes;
    private Object initialAlignment; // Can be AlignPosition or ShapeInstance
    private boolean active; // Whether this sequence is active
    private boolean invertAlignment; // Whether to invert alignment (default false)
    
    public ShapeSequence() {
        this.name = "";
        this.shapes = new ArrayList<>();
        this.initialAlignment = null;
        this.active = true; // Default to active
        this.invertAlignment = false; // Default to no inversion
    }
    
    public ShapeSequence(String name) {
        this.name = name != null ? name : "";
        this.shapes = new ArrayList<>();
        this.initialAlignment = null;
        this.active = true; // Default to active
        this.invertAlignment = false; // Default to no inversion
    }
    
    public ShapeSequence(List<ShapeInstance> shapes) {
        this.name = "";
        this.shapes = shapes != null ? new ArrayList<>(shapes) : new ArrayList<>();
        this.initialAlignment = null;
        this.active = true; // Default to active
        this.invertAlignment = false; // Default to no inversion
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name != null ? name : "";
    }
    
    /**
     * Gets the list of shapes in the sequence.
     * @return List of ShapeInstance objects
     */
    public List<ShapeInstance> getShapes() {
        return new ArrayList<>(shapes);
    }
    
    /**
     * Sets the list of shapes in the sequence.
     * @param shapes List of ShapeInstance objects
     */
    public void setShapes(List<ShapeInstance> shapes) {
        this.shapes = shapes != null ? new ArrayList<>(shapes) : new ArrayList<>();
    }
    
    /**
     * Adds a shape to the sequence.
     * @param shape ShapeInstance to add
     */
    public void addShape(ShapeInstance shape) {
        if (shape != null) {
            shapes.add(shape);
        }
    }
    
    /**
     * Inserts a shape at the specified index.
     * Sets default color flag (white), deactivates all existing shapes,
     * inserts the shape, and activates the newly inserted shape.
     * @param index Index at which to insert the shape
     * @param shape ShapeInstance to insert
     * @throws IllegalArgumentException if validation fails
     */
    public void insertShape(int index, ShapeInstance shape) {
        if (shape != null && index >= 0 && index <= shapes.size()) {
            // Validate before inserting
            com.trackdraw.validation.SequenceValidator.ValidationResult result = 
                com.trackdraw.validation.SequenceValidator.validateAddShape(this, shape);
            if (!result.isValid()) {
                throw new IllegalArgumentException(result.getErrorMessage());
            }
            
            // If this is the first shape being added to a linked sequence, validate the combination
            if (shapes.isEmpty() && index == 0) {
                ShapeInstance linkedShape = getInitialAlignmentAsShape();
                if (linkedShape != null) {
                    // Create a temporary sequence with just this shape to validate
                    ShapeSequence tempSeq = new ShapeSequence("temp");
                    tempSeq.setInvertAlignment(this.invertAlignment);
                    // Temporarily add the shape to validate (we'll add it properly below)
                    ShapeInstance tempShape = createTempShapeCopy(shape);
                    tempSeq.shapes.add(tempShape);
                    
                    com.trackdraw.validation.SequenceValidator.ValidationResult linkResult = 
                        com.trackdraw.validation.SequenceValidator.validateLinkedSequence(linkedShape, tempSeq);
                    if (!linkResult.isValid()) {
                        throw new IllegalArgumentException(linkResult.getErrorMessage());
                    }
                }
            }
            
            // Set default color flag (white)
            shape.setRed(false);
            
            // Deactivate all shapes in the sequence
            for (ShapeInstance existingShape : shapes) {
                existingShape.setActive(false);
            }
            
            // Insert the shape
            shapes.add(index, shape);
            
            // Activate the newly inserted shape
            shape.setActive(true);
        }
    }
    
    /**
     * Creates a temporary copy of a shape for validation purposes.
     * This is needed to avoid modifying the original shape during validation.
     */
    private ShapeInstance createTempShapeCopy(ShapeInstance original) {
        // Create a copy with the same key and color properties for validation
        if (original instanceof com.trackdraw.model.AnnularSector) {
            com.trackdraw.model.AnnularSector sector = (com.trackdraw.model.AnnularSector) original;
            com.trackdraw.model.AnnularSector copy = new com.trackdraw.model.AnnularSector(
                sector.getKey(), sector.getExternalDiameter(), 
                sector.getAngleDegrees(), sector.getWidth());
            copy.setRed(sector.isRed());
            copy.setForceInvertColor(sector.isForceInvertColor());
            return copy;
        } else if (original instanceof com.trackdraw.model.Rectangle) {
            com.trackdraw.model.Rectangle rect = (com.trackdraw.model.Rectangle) original;
            com.trackdraw.model.Rectangle copy = new com.trackdraw.model.Rectangle(
                rect.getKey(), rect.getLength(), rect.getWidth());
            copy.setRed(rect.isRed());
            copy.setForceInvertColor(rect.isForceInvertColor());
            return copy;
        } else if (original instanceof com.trackdraw.model.HalfCircle) {
            com.trackdraw.model.HalfCircle halfCircle = (com.trackdraw.model.HalfCircle) original;
            com.trackdraw.model.HalfCircle copy = new com.trackdraw.model.HalfCircle(
                halfCircle.getKey(), halfCircle.getDiameter());
            copy.setRed(halfCircle.isRed());
            copy.setForceInvertColor(halfCircle.isForceInvertColor());
            return copy;
        }
        return original; // Fallback
    }
    
    /**
     * Removes a shape at the specified index.
     * @param index Index of the shape to remove
     */
    public void removeShape(int index) {
        if (index >= 0 && index < shapes.size()) {
            shapes.remove(index);
        }
    }
    
    /**
     * Gets the shape at the specified index.
     * @param index Index of the shape
     * @return ShapeInstance at the index, or null if index is invalid
     */
    public ShapeInstance getShape(int index) {
        if (index >= 0 && index < shapes.size()) {
            return shapes.get(index);
        }
        return null;
    }
    
    /**
     * Gets the number of shapes in the sequence.
     * @return Number of shapes
     */
    public int size() {
        return shapes.size();
    }
    
    /**
     * Checks if the sequence is empty.
     * @return true if empty, false otherwise
     */
    public boolean isEmpty() {
        return shapes.isEmpty();
    }
    
    /**
     * Clears all shapes from the sequence.
     */
    public void clear() {
        shapes.clear();
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }
    
    public boolean isInvertAlignment() {
        return invertAlignment;
    }
    
    public void setInvertAlignment(boolean invertAlignment) {
        this.invertAlignment = invertAlignment;
    }
    
    /**
     * Sets the initial alignment position as an AlignPosition.
     * @param alignPosition AlignPosition for the first shape
     */
    public void setInitialAlignment(AlignPosition alignPosition) {
        this.initialAlignment = alignPosition;
    }
    
    /**
     * Sets the initial alignment position as a ShapeInstance.
     * The align position of this shape will be used for the first shape in the sequence.
     * @param shape ShapeInstance whose align position will be used
     */
    public void setInitialAlignment(ShapeInstance shape) {
        this.initialAlignment = shape;
    }
    
    /**
     * Gets the initial alignment position as an AlignPosition.
     * @return AlignPosition if set as AlignPosition, null otherwise
     */
    public AlignPosition getInitialAlignmentAsPosition() {
        if (initialAlignment instanceof AlignPosition) {
            return (AlignPosition) initialAlignment;
        }
        return null;
    }
    
    /**
     * Gets the initial alignment position as a ShapeInstance.
     * @return ShapeInstance if set as ShapeInstance, null otherwise
     */
    public ShapeInstance getInitialAlignmentAsShape() {
        if (initialAlignment instanceof ShapeInstance) {
            return (ShapeInstance) initialAlignment;
        }
        return null;
    }
    
    /**
     * Gets the effective initial alignment position.
     * If set as AlignPosition, returns it directly.
     * If set as ShapeInstance, returns its align position (not calculateNextAlignPosition).
     * If invertAlignment is true and linked to a shape, inverts the angle by 180 degrees.
     * @return AlignPosition for the first shape, or null if not set
     */
    public AlignPosition getEffectiveInitialAlignment() {
        AlignPosition alignPos = null;
        
        if (initialAlignment instanceof AlignPosition) {
            alignPos = (AlignPosition) initialAlignment;
        } else if (initialAlignment instanceof ShapeInstance) {
            // Use the shape's AlignPosition directly, not calculateNextAlignPosition()
            // The linked shape's alignPosition should already be set because sequences are drawn
            // in dependency order (parent sequences before child sequences)
            ShapeInstance linkedShape = (ShapeInstance) initialAlignment;
            alignPos = linkedShape.getAlignPosition();
            
            // Assert that alignPos is not null - if it is, there's a bug in sequence ordering
            assert alignPos != null : "Linked shape's alignPosition is null. " +
                "This should not happen if sequences are drawn in dependency order. " +
                "Linked shape: " + linkedShape.getKey() + " (ID: " + linkedShape.getId() + ")";
            
            // Create a copy to avoid modifying the original
            alignPos = new AlignPosition(alignPos.getX(), alignPos.getY(), alignPos.getAngle());
            
            // If invertAlignment is true, invert the angle by 180 degrees
            if (invertAlignment) {
                double invertedAngle = alignPos.getAngle() + 180.0;
                // Normalize angle to 0-360 range
                while (invertedAngle < 0) invertedAngle += 360;
                while (invertedAngle >= 360) invertedAngle -= 360;
                alignPos = new AlignPosition(alignPos.getX(), alignPos.getY(), invertedAngle);
            }
        }
        
        return alignPos;
    }
    
    /**
     * Recalculates color flags (isRed) for all shapes in this sequence.
     * If this sequence is linked to another shape:
     *   - When invertAlignment is true: first shape should be opposite to linked shape
     *   - When invertAlignment is false: first shape has same color as linked shape
     * Otherwise, starts from white (false) and alternates.
     * Each shape's color is calculated based on the previous shape's effective color (after inversion).
     */
    public void recalculateColors() {
        if (shapes.isEmpty()) {
            return;
        }
        
        // Get the linked shape if this sequence is linked to another shape
        ShapeInstance linkedShape = getInitialAlignmentAsShape();
        
        // Determine starting color based on linked shape or default
        boolean previousEffectiveRed;
        if (linkedShape != null) {
            // Sequence with link: start from opposite color of linked shape's effective color
            previousEffectiveRed = !linkedShape.getEffectiveIsRed();
        } else {
            // Sequence without link: start from white (false)
            previousEffectiveRed = false;
        }
        
        // Calculate each shape's color based on the previous shape's effective color
        for (int i = 0; i < shapes.size(); i++) {
            ShapeInstance shape = shapes.get(i);
            if (shape != null) {
                boolean baseRed;
                if (i == 0 && linkedShape != null) {
                    // First shape of linked sequence
                    if (invertAlignment) {
                        // When invertAlignment is true: first shape should be opposite to linked shape
                        // previousEffectiveRed is already opposite to linked shape, so use it directly
                        baseRed = previousEffectiveRed;
                    } else {
                        // When invertAlignment is false: first shape has same color as linked shape
                        // previousEffectiveRed is opposite, so invert it to get same as linked shape
                        baseRed = !previousEffectiveRed;
                    }
                } else {
                    // Subsequent shapes: alternate from previous effective color
                    baseRed = !previousEffectiveRed;
                }
                shape.setRed(baseRed);
                
                // Update previous effective color for next iteration
                // Note: forceInvertColor is preserved, so the effective color will be inverted if needed
                previousEffectiveRed = shape.getEffectiveIsRed();
            }
        }
    }
    
    /**
     * Calculates colors for a shape based on color schema:
     * - Base color flag (isRed) determines if it's black/white or red
     * - Active sequence vs not active sequence changes intensity
     * - Active shape: green contour for black/white, dark red for red
     * 
     * @param shape The shape to calculate colors for
     * @return Array with [contourColor, infillColor]
     */
    private Color[] calculateColors(ShapeInstance shape) {
        // Use effective color (after inversion) for display
        boolean isRed = shape.getEffectiveIsRed();
        boolean seqActive = this.active;
        boolean shapeActive = shape.isActive();
        
        Color contourColor;
        Color infillColor;
        
        // Active shape colors only apply if the sequence is also active
        // If sequence is not active, active shapes use "not active" colors
        if (isRed) {
            // Red base color
            if (shapeActive && seqActive) {
                // Active shape in active sequence: dark red (maroon) contour and infill
                contourColor = new Color(128, 0, 0); // Dark red / maroon
                infillColor = new Color(128, 0, 0); // Dark red / maroon
            } else if (seqActive) {
                // Active sequence: bright red
                contourColor = Color.RED;
                infillColor = Color.RED;
            } else {
                // Not active sequence (including active shapes in non-active sequences): light pink
                contourColor = new Color(255, 182, 193); // Light pink
                infillColor = new Color(255, 182, 193); // Light pink
            }
        } else {
            // White/black base color
            if (shapeActive && seqActive) {
                // Active shape in active sequence: green contour, white infill
                contourColor = Color.GREEN;
                infillColor = Color.WHITE;
            } else if (seqActive) {
                // Active sequence: dark gray contour, white infill
                contourColor = new Color(64, 64, 64); // Dark gray
                infillColor = Color.WHITE;
            } else {
                // Not active sequence (including active shapes in non-active sequences): light gray contour, white infill
                contourColor = new Color(192, 192, 192); // Light gray
                infillColor = Color.WHITE;
            }
        }
        
        return new Color[]{contourColor, infillColor};
    }
    
    /**
     * Draws all shapes in the sequence with proper color assignment and alignment.
     * Colors are calculated based on color flags, active sequence, and active shape.
     * @param g2d Graphics2D context to draw on
     * @param initialAlignPosition The initial alignment position for the first shape
     * @return The final alignment position after drawing all shapes
     */
    public AlignPosition drawAll(Graphics2D g2d, AlignPosition initialAlignPosition) {
        if (shapes.isEmpty()) {
            return initialAlignPosition;
        }
        
        AlignPosition currentAlignPosition = initialAlignPosition;
        
        for (int i = 0; i < shapes.size(); i++) {
            ShapeInstance shape = shapes.get(i);
            
            // Calculate colors based on schema
            Color[] colors = calculateColors(shape);
            shape.setContourColor(colors[0]);
            shape.setInfillColor(colors[1]);
            
            // Set align position for current shape
            shape.setAlignPosition(currentAlignPosition);
            
            // Draw the shape and get the next align position
            currentAlignPosition = shape.draw(g2d);
        }
        
        return currentAlignPosition;
    }
}

