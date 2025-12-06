package com.trackdraw.view;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

/**
 * Panel for controlling the first shape's position and rotation.
 */
public class FirstShapePositionPanel extends JPanel {
    private JTextField xField;
    private JTextField yField;
    private JTextField rotationField;
    private double rotationAngle = 0.0;
    private Consumer<Double> rotationChangeHandler; // Handler for rotation changes
    
    public FirstShapePositionPanel() {
        initializeComponents();
        setupLayout();
    }
    
    private void initializeComponents() {
        // X, Y fields for first shape position
        xField = new JTextField(8);
        xField.setText("");
        xField.setToolTipText("X coordinate for first shape (click on canvas to set)");
        xField.setEditable(false);
        
        yField = new JTextField(8);
        yField.setText("");
        yField.setToolTipText("Y coordinate for first shape (click on canvas to set)");
        yField.setEditable(false);
        
        rotationField = new JTextField(8);
        rotationField.setText("0");
        rotationField.setToolTipText("Rotation angle in degrees (for first shape only)");
        rotationField.addActionListener(e -> updateRotation());
    }
    
    private void setupLayout() {
        setLayout(new FlowLayout(FlowLayout.CENTER));
        
        // First shape position controls
        add(new JLabel("First Shape X:"));
        add(xField);
        add(new JLabel("Y:"));
        add(yField);
        add(new JLabel("  (click canvas to set)"));
        
        // Rotation controls
        add(new JLabel("  Rotation:"));
        add(rotationField);
        add(new JLabel("°"));
    }
    
    /**
     * Sets the X, Y position values.
     * @param x X coordinate
     * @param y Y coordinate
     */
    public void setPosition(double x, double y) {
        xField.setText(String.valueOf((int)x));
        yField.setText(String.valueOf((int)y));
    }
    
    /**
     * Gets the current rotation angle.
     * @return Rotation angle in degrees
     */
    public double getRotationAngle() {
        return rotationAngle;
    }
    
    /**
     * Sets the rotation angle.
     * @param angle Rotation angle in degrees
     */
    public void setRotationAngle(double angle) {
        this.rotationAngle = angle;
        rotationField.setText(String.valueOf((int)angle));
    }
    
    /**
     * Sets the handler to be called when rotation changes.
     * @param handler Handler that receives the new rotation angle
     */
    public void setRotationChangeHandler(Consumer<Double> handler) {
        this.rotationChangeHandler = handler;
    }
    
    /**
     * Updates the rotation angle from the input field.
     */
    private void updateRotation() {
        try {
            rotationAngle = Double.parseDouble(rotationField.getText().trim());
            if (rotationChangeHandler != null) {
                rotationChangeHandler.accept(rotationAngle);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, 
                "Invalid rotation angle. Please enter a number.", 
                "Invalid Input", 
                JOptionPane.WARNING_MESSAGE);
            rotationField.setText(String.valueOf((int)rotationAngle));
        }
    }
}

