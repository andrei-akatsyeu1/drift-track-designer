package com.trackdraw.view;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

/**
 * Panel for controlling the background image scale factor.
 */
public class BackgroundImageScalePanel extends JPanel {
    private ScaleControl scaleControl;
    private Consumer<Double> scaleChangeHandler;
    
    public BackgroundImageScalePanel() {
        // Default scale is 1.0 (100%)
        scaleControl = new ScaleControl("Image Scale:", 1.0, 0.1, 10.0);
        
        scaleControl.setScaleChangeHandler(scale -> {
            if (scaleChangeHandler != null) {
                scaleChangeHandler.accept(scale);
            }
        });
        
        setupLayout();
    }
    
    private void setupLayout() {
        setLayout(new FlowLayout(FlowLayout.CENTER));
        add(scaleControl);
    }
    
    /**
     * Gets the current scale value.
     * @return Current scale factor
     */
    public double getScale() {
        return scaleControl.getScale();
    }
    
    /**
     * Sets the scale value.
     * @param scale Scale factor
     */
    public void setScale(double scale) {
        scaleControl.setScale(scale);
    }
    
    /**
     * Sets the handler to be called when scale changes.
     * @param handler Handler that receives the new scale value
     */
    public void setScaleChangeHandler(Consumer<Double> handler) {
        this.scaleChangeHandler = handler;
    }
    
    /**
     * Gets the underlying ScaleControl component.
     * @return The ScaleControl instance
     */
    public ScaleControl getScaleControl() {
        return scaleControl;
    }
}

