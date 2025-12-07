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
    
    public ShapeSequence() {
        this.name = "";
        this.shapes = new ArrayList<>();
        this.initialAlignment = null;
        this.active = true; // Default to active
    }
    
    public ShapeSequence(String name) {
        this.name = name != null ? name : "";
        this.shapes = new ArrayList<>();
        this.initialAlignment = null;
        this.active = true; // Default to active
    }
    
    public ShapeSequence(List<ShapeInstance> shapes) {
        this.name = "";
        this.shapes = shapes != null ? new ArrayList<>(shapes) : new ArrayList<>();
        this.initialAlignment = null;
        this.active = true; // Default to active
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
     */
    public void insertShape(int index, ShapeInstance shape) {
        if (shape != null && index >= 0 && index <= shapes.size()) {
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
     * If set as ShapeInstance, returns its align position.
     * @return AlignPosition for the first shape, or null if not set
     */
    public AlignPosition getEffectiveInitialAlignment() {
        if (initialAlignment instanceof AlignPosition) {
            return (AlignPosition) initialAlignment;
        } else if (initialAlignment instanceof ShapeInstance) {
            return ((ShapeInstance) initialAlignment).calculateNextAlignPosition();
        }
        return null;
    }
    
    /**
     * Recalculates color flags (isRed) for all shapes in this sequence.
     * If this sequence is linked to another shape, starts from opposite color of linked shape.
     * Otherwise, starts from white (false) and alternates.
     */
    public void recalculateColors() {
        if (shapes.isEmpty()) {
            return;
        }
        
        // Get the linked shape if this sequence is linked to another shape
        ShapeInstance linkedShape = getInitialAlignmentAsShape();
        
        if (linkedShape != null) {
            // Sequence with link: start from opposite color of linked shape
            boolean startRed = !linkedShape.isRed();
            for (int i = 0; i < shapes.size(); i++) {
                ShapeInstance shape = shapes.get(i);
                if (shape != null) {
                    // Alternate starting from startRed
                    shape.setRed((i % 2 == 0) ? startRed : !startRed);
                }
            }
        } else {
            // Sequence without link: alternate starting from white (false)
            for (int i = 0; i < shapes.size(); i++) {
                ShapeInstance shape = shapes.get(i);
                if (shape != null) {
                    shape.setRed(i % 2 == 1); // Even indices (0, 2, 4...) = white, odd (1, 3, 5...) = red
                }
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
        boolean isRed = shape.isRed();
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

