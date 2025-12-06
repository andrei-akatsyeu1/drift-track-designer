package com.trackdraw.view;

import com.trackdraw.config.GlobalScale;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

/**
 * Panel for controlling the scale factor.
 */
public class ScaleControlPanel extends JPanel {
    private JTextField scaleField;
    private double currentScale = 3.0;
    private Consumer<Double> scaleChangeHandler; // Handler for scale changes
    
    public ScaleControlPanel() {
        initializeComponents();
        setupLayout();
        // Initialize with global scale value
        currentScale = GlobalScale.getScale();
        updateScale();
    }
    
    private void initializeComponents() {
        scaleField = new JTextField(6);
        scaleField.setText("300%");
        scaleField.setEditable(false);
    }
    
    private void setupLayout() {
        setLayout(new FlowLayout(FlowLayout.CENTER));
        
        // Scale controls
        add(new JLabel("  Scale:"));
        JButton scaleMinusButton = new JButton("-");
        scaleMinusButton.addActionListener(e -> changeScale(-0.1));
        add(scaleMinusButton);
        
        add(scaleField);
        
        JButton scalePlusButton = new JButton("+");
        scalePlusButton.addActionListener(e -> changeScale(0.1));
        add(scalePlusButton);
    }
    
    /**
     * Changes the scale by the specified delta (e.g., 0.1 for 10%).
     * @param delta Scale change delta
     */
    private void changeScale(double delta) {
        currentScale += delta;
        // Ensure scale doesn't go below 0.1 (10%)
        if (currentScale < 0.1) {
            currentScale = 0.1;
        }
        
        // Set new scale to global property
        GlobalScale.setScale(currentScale);
        updateScale();
        
        // Notify handler
        if (scaleChangeHandler != null) {
            scaleChangeHandler.accept(currentScale);
        }
    }
    
    /**
     * Updates the scale display.
     */
    private void updateScale() {
        int scalePercent = (int)(currentScale * 100);
        scaleField.setText(scalePercent + "%");
        // Update global scale
        GlobalScale.setScale(currentScale);
    }
    
    /**
     * Gets the current scale value.
     * @return Current scale factor
     */
    public double getScale() {
        return currentScale;
    }
    
    /**
     * Sets the scale value.
     * @param scale Scale factor
     */
    public void setScale(double scale) {
        this.currentScale = scale;
        updateScale();
    }
    
    /**
     * Sets the handler to be called when scale changes.
     * @param handler Handler that receives the new scale value
     */
    public void setScaleChangeHandler(Consumer<Double> handler) {
        this.scaleChangeHandler = handler;
    }
}

