package com.trackdraw.view;

import com.trackdraw.model.ShapeInstance;
import com.trackdraw.model.AlignPosition;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Panel for drawing geometric shapes.
 */
public class DrawingPanel extends JPanel {
    private List<ShapeInstance> shapeSequence = new ArrayList<>();
    private double initialRotationAngle = 0.0; // Rotation angle for the first shape only
    private double initialX = -1; // Initial X position for first shape (-1 means use center)
    private double initialY = -1; // Initial Y position for first shape (-1 means use center)
    private Consumer<Point> clickHandler; // Handler for mouse clicks to set initial position
    
    public DrawingPanel() {
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(800, 600));
        
        // Add mouse listener for clicking to set initial position
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (clickHandler != null) {
                    clickHandler.accept(e.getPoint());
                }
            }
        });
    }
    
    /**
     * Sets the click handler for setting initial position.
     * @param handler Handler that receives the clicked point
     */
    public void setClickHandler(Consumer<Point> handler) {
        this.clickHandler = handler;
    }
    
    /**
     * Sets the shape sequence to draw.
     * @param sequence List of ShapeInstance objects to draw
     */
    public void setShapeSequence(List<ShapeInstance> sequence) {
        this.shapeSequence = sequence != null ? new ArrayList<>(sequence) : new ArrayList<>();
        repaint();
    }
    
    /**
     * Gets the current shape sequence.
     * @return List of ShapeInstance objects
     */
    public List<ShapeInstance> getShapeSequence() {
        return new ArrayList<>(shapeSequence);
    }
    
    /**
     * Sets the initial rotation angle for the first shape.
     * @param angleDegrees Rotation angle in degrees
     */
    public void setInitialRotationAngle(double angleDegrees) {
        this.initialRotationAngle = angleDegrees;
    }
    
    /**
     * Sets the initial X,Y position for the first shape.
     * @param x X coordinate (-1 to use center)
     * @param y Y coordinate (-1 to use center)
     */
    public void setInitialPosition(double x, double y) {
        this.initialX = x;
        this.initialY = y;
    }
    
    /**
     * Draws all shapes in the sequence following the workflow:
     * 3.3) Canvas cleared
     * 3.4) Start drawing all shapes from the list by sequence
     * 3.5) For the first shape, set default AlignPosition (center of canvas, angle from initialRotationAngle)
     * 3.6) Draw the first shape, set the returned AlignPosition to the next shape
     * 3.7) Loop till the end of the shape list
     */
    public void drawAll() {
        repaint();
    }
    
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2d = (Graphics2D) g;
        
        // Enable anti-aliasing for smoother shapes
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2.0f));
        
        // 3.3) Canvas cleared (already done by super.paint())
        
        // 3.4) Start drawing all shapes from the list by sequence
        if (shapeSequence.isEmpty()) {
            return;
        }
        
        // 3.5) For the first shape, set default AlignPosition (center of canvas or initialX/Y, angle from initialRotationAngle)
        // For the first shape, we want to position it at a specific center, so we calculate
        // what the first short side center should be from that desired center
        double desiredCenterX = (initialX >= 0) ? initialX : (getWidth() / 2.0);
        double desiredCenterY = (initialY >= 0) ? initialY : (getHeight() / 2.0);
        
        ShapeInstance firstShape = shapeSequence.get(0);
        double[] firstSideCenter = firstShape.calculateAlignPositionFromCenter(desiredCenterX, desiredCenterY, initialRotationAngle);
        AlignPosition currentAlignPosition = new AlignPosition(firstSideCenter[0], firstSideCenter[1], initialRotationAngle);
        
        // 3.6) Draw the first shape, set the returned AlignPosition to the next shape
        // 3.7) Loop till the end of the shape list
        for (ShapeInstance shape : shapeSequence) {
            // Set align position for current shape
            shape.setAlignPosition(currentAlignPosition);
            
            // Draw the shape and get the next align position
            currentAlignPosition = shape.draw(g2d);
        }
    }
}

