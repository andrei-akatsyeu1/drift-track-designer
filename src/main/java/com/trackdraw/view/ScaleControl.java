package com.trackdraw.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.function.Consumer;

/**
 * Reusable scale control component with label, minus button, value display, and plus button.
 * Supports keyboard bindings for scale adjustment.
 */
public class ScaleControl extends JPanel {
    private JTextField scaleField;
    private double currentScale;
    private Consumer<Double> scaleChangeHandler;
    private String labelText;
    private double minScale;
    private double maxScale;
    private JComponent keyboardTarget; // Component to bind keyboard shortcuts to
    
    /**
     * Creates a scale control component.
     * @param labelText Label text (e.g., "Scale:", "Image Scale:")
     * @param initialScale Initial scale value
     * @param minScale Minimum scale value
     * @param maxScale Maximum scale value
     */
    public ScaleControl(String labelText, double initialScale, double minScale, double maxScale) {
        this.labelText = labelText;
        this.currentScale = initialScale;
        this.minScale = minScale;
        this.maxScale = maxScale;
        initializeComponents();
        setupLayout();
        updateScale();
    }
    
    private void initializeComponents() {
        scaleField = new JTextField(6);
        scaleField.setEditable(false);
        scaleField.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
    }
    
    private void setupLayout() {
        setLayout(new FlowLayout(FlowLayout.CENTER));
        
        // Label
        add(new JLabel("  " + labelText));
        
        Font buttonFont = new Font(Font.SANS_SERIF, Font.PLAIN, 20);
        
        // Minus button
        JButton scaleMinusButton = new JButton("-");
        scaleMinusButton.setFont(buttonFont);
        scaleMinusButton.setToolTipText("Decrease " + labelText.toLowerCase() + " by 10%");
        scaleMinusButton.addActionListener(e -> changeScale(-0.1));
        add(scaleMinusButton);
        
        // Scale display
        add(scaleField);
        
        // Plus button
        JButton scalePlusButton = new JButton("+");
        scalePlusButton.setFont(buttonFont);
        scalePlusButton.setToolTipText("Increase " + labelText.toLowerCase() + " by 10%");
        scalePlusButton.addActionListener(e -> changeScale(0.1));
        add(scalePlusButton);
    }
    
    /**
     * Changes the scale by the specified delta (e.g., 0.1 for 10%).
     * Normalizes the scale to ensure clean increments (rounds to nearest 0.1 for 10% changes, 0.01 for 1% changes).
     * @param delta Scale change delta
     */
    public void changeScale(double delta) {
        currentScale += delta;
        
        // Normalize scale to clean increments to avoid floating point precision issues
        // If delta is 0.1 (10%), round to nearest 0.1; if delta is 0.01 (1%), round to nearest 0.01
        if (Math.abs(delta) >= 0.05) {
            // 10% change: round to nearest 0.1
            currentScale = Math.round(currentScale * 10.0) / 10.0;
        } else {
            // 1% change: round to nearest 0.01
            currentScale = Math.round(currentScale * 100.0) / 100.0;
        }
        
        // Clamp to min/max range
        if (currentScale < minScale) {
            currentScale = minScale;
        }
        if (currentScale > maxScale) {
            currentScale = maxScale;
        }
        
        updateScale();
        
        // Notify handler
        if (scaleChangeHandler != null) {
            scaleChangeHandler.accept(currentScale);
        }
    }
    
    /**
     * Updates the scale display.
     * Rounds the percentage instead of truncating to ensure accurate display.
     */
    private void updateScale() {
        int scalePercent = (int)Math.round(currentScale * 100);
        scaleField.setText(scalePercent + "%");
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
        // Clamp to min/max range
        if (scale < minScale) {
            scale = minScale;
        }
        if (scale > maxScale) {
            scale = maxScale;
        }
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
    
    /**
     * Sets the component to bind keyboard shortcuts to.
     * @param target Component to bind keys to (typically DrawingPanel)
     */
    public void setKeyboardTarget(JComponent target) {
        this.keyboardTarget = target;
    }
    
    /**
     * Binds keyboard shortcuts for scale adjustment.
     * @param keyCode Key code (e.g., KeyEvent.VK_PAGE_UP)
     * @param useShift If true, requires Shift modifier
     * @param delta10Percent Delta for 10% change (e.g., 0.1 for increase, -0.1 for decrease)
     */
    public void bindKeyboardKeys(int keyCode, boolean useShift, double delta10Percent) {
        if (keyboardTarget == null) {
            return;
        }
        
        InputMap inputMap = keyboardTarget.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = keyboardTarget.getActionMap();
        
        // Calculate action names
        String actionName10 = String.format("scale_%s_%d_10", useShift ? "shift" : "normal", keyCode);
        String actionName1 = String.format("scale_%s_%d_1", useShift ? "shift" : "normal", keyCode);
        
        // Calculate deltas
        double delta1Percent = delta10Percent * 0.1; // 1% is 10% of 10%
        
        // Bind 10% change (without Ctrl)
        int modifiers = useShift ? KeyEvent.SHIFT_DOWN_MASK : 0;
        inputMap.put(KeyStroke.getKeyStroke(keyCode, modifiers), actionName10);
        actionMap.put(actionName10, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                changeScale(delta10Percent);
            }
        });
        
        // Bind 1% change (with Ctrl) - Ctrl always means 1%
        int modifiersWithCtrl = modifiers | KeyEvent.CTRL_DOWN_MASK;
        inputMap.put(KeyStroke.getKeyStroke(keyCode, modifiersWithCtrl), actionName1);
        actionMap.put(actionName1, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                changeScale(delta1Percent);
            }
        });
    }
}

