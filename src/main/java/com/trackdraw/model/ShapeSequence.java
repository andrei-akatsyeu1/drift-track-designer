package com.trackdraw.model;

import org.apache.commons.lang3.StringUtils;

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
        this.name = StringUtils.defaultString(name);
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
        this.name = StringUtils.defaultString(name);
    }

    /**
     * Gets the list of shapes in the sequence.
     *
     * @return List of ShapeInstance objects
     */
    public List<ShapeInstance> getShapes() {
        return new ArrayList<>(shapes);
    }

    /**
     * Sets the list of shapes in the sequence.
     *
     * @param shapes List of ShapeInstance objects
     */
    public void setShapes(List<ShapeInstance> shapes) {
        this.shapes = shapes != null ? new ArrayList<>(shapes) : new ArrayList<>();
    }

    /**
     * Adds a shape to the sequence.
     *
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
     *
     * @param index Index at which to insert the shape
     * @param shape ShapeInstance to insert
     * @throws IllegalArgumentException if validation fails
     */
    public void insertShape(int index, ShapeInstance shape) {
        if (shape != null && index >= 0 && index <= shapes.size()) {
            // Validate before inserting (pass the insertion index)
            com.trackdraw.validation.SequenceValidator.ValidationResult result =
                    com.trackdraw.validation.SequenceValidator.validateAddShape(this, shape, index);
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
        return original.copy();
    }

    /**
     * Removes a shape at the specified index.
     *
     * @param index Index of the shape to remove
     */
    public void removeShape(int index) {
        if (index >= 0 && index < shapes.size()) {
            shapes.remove(index);
        }
    }

    /**
     * Gets the shape at the specified index.
     *
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
     *
     * @return Number of shapes
     */
    public int size() {
        return shapes.size();
    }

    /**
     * Checks if the sequence is empty.
     *
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

    /**
     * Deactivates all shapes in the sequence.
     */
    public void deactivateAllShapes() {
        for (ShapeInstance shape : shapes) {
            shape.setActive(false);
        }
    }

    /**
     * Activates the shape at the specified index and deactivates all others.
     * @param index Index of the shape to activate
     */
    public void activateShape(int index) {
        deactivateAllShapes();
        if (index >= 0 && index < shapes.size()) {
            ShapeInstance shape = shapes.get(index);
            if (shape != null) {
                shape.setActive(true);
            }
        }
    }

    /**
     * Gets the active shape in the sequence.
     * @return The first active ShapeInstance, or null if none is active
     */
    public ShapeInstance getActiveShape() {
        for (ShapeInstance shape : shapes) {
            if (shape.isActive()) {
                return shape;
            }
        }
        return null;
    }

    /**
     * Gets the index of the active shape in the sequence.
     * @return Index of the first active shape, or -1 if none is active
     */
    public int getActiveShapeIndex() {
        for (int i = 0; i < shapes.size(); i++) {
            if (shapes.get(i).isActive()) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Checks if any shape in the sequence is active.
     * @return true if at least one shape is active, false otherwise
     */
    public boolean hasActiveShape() {
        return getActiveShape() != null;
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
     *
     * @param alignPosition AlignPosition for the first shape
     */
    public void setInitialAlignment(AlignPosition alignPosition) {
        this.initialAlignment = alignPosition;
    }

    /**
     * Sets the initial alignment position as a ShapeInstance.
     * The align position of this shape will be used for the first shape in the sequence.
     *
     * @param shape ShapeInstance whose align position will be used
     */
    public void setInitialAlignment(ShapeInstance shape) {
        this.initialAlignment = shape;
    }

    /**
     * Gets the initial alignment position as an AlignPosition.
     *
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
     *
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
     *
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
     * - When invertAlignment is true: first shape should be opposite to linked shape
     * - When invertAlignment is false: first shape has same color as linked shape
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
     *
     * @param g2d                  Graphics2D context to draw on
     * @param initialAlignPosition The initial alignment position for the first shape
     * @param showKeys             Whether to show shape keys on the canvas
     * @return The final alignment position after drawing all shapes
     */
    public AlignPosition drawAll(Graphics2D g2d, AlignPosition initialAlignPosition, boolean showKeys) {
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

            // Draw the shape's key text if enabled
            shape.drawText(g2d, showKeys);
        }

        return currentAlignPosition;
    }

    /**
     * Gets all align positions for shapes in this sequence.
     * Traverses the sequence starting from the initial position and collects all align positions.
     * 
     * @param initialPos Initial alignment position for the first shape
     * @param g2d Graphics2D context for calculating positions (can be a temporary/dummy context)
     * @return List of align positions, one for each shape
     */
    public List<AlignPosition> getAllAlignPositions(AlignPosition initialPos, Graphics2D g2d) {
        List<AlignPosition> positions = new ArrayList<>();
        if (shapes.isEmpty() || initialPos == null) {
            return positions;
        }

        AlignPosition currentPos = initialPos;
        for (ShapeInstance shape : shapes) {
            positions.add(new AlignPosition(currentPos.getX(), currentPos.getY(), currentPos.getAngle()));
            shape.setAlignPosition(currentPos);
            currentPos = shape.draw(g2d);
        }
        return positions;
    }

    /**
     * Calculates the bounding box of this sequence.
     * Traverses all shapes and collects their align positions to determine min/max bounds.
     * 
     * @param initialPos Initial alignment position for the first shape
     * @param g2d Graphics2D context for calculating positions (can be a temporary/dummy context)
     * @param padding Padding to add around the bounds (in current scale units)
     * @return Rectangle2D.Double representing the bounds, or null if sequence is empty
     */
    public java.awt.geom.Rectangle2D.Double calculateBounds(AlignPosition initialPos, Graphics2D g2d, double padding) {
        if (shapes.isEmpty() || initialPos == null) {
            return null;
        }

        List<AlignPosition> positions = getAllAlignPositions(initialPos, g2d);
        if (positions.isEmpty()) {
            return null;
        }

        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE;
        double maxY = Double.MIN_VALUE;

        for (AlignPosition pos : positions) {
            minX = Math.min(minX, pos.getX());
            minY = Math.min(minY, pos.getY());
            maxX = Math.max(maxX, pos.getX());
            maxY = Math.max(maxY, pos.getY());
        }

        if (minX == Double.MAX_VALUE) {
            return null;
        }

        // Add padding
        minX = minX - padding;
        minY = minY - padding;
        maxX = maxX + padding;
        maxY = maxY + padding;

        return new java.awt.geom.Rectangle2D.Double(minX, minY, maxX - minX, maxY - minY);
    }
}

