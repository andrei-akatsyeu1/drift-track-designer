package com.trackdraw.view;

import com.trackdraw.config.ShapeConfig;
import com.trackdraw.model.ShapeInstance;
import com.trackdraw.model.AnnularSector;
import com.trackdraw.model.Rectangle;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * Panel containing shape palettes for selecting shapes with different orientations.
 * Two palettes: one for orientation = 1, one for orientation = -1.
 */
public class ShapePalettePanel extends JPanel {
    private static final int BUTTON_WIDTH = 50; // Fixed width for all buttons
    private static final int BUTTON_HEIGHT = 30; // Fixed height for all buttons
    
    private JPanel palettePanel1; // Palette for orientation = 1
    private JPanel palettePanel2; // Palette for orientation = -1
    private ShapeConfig shapeConfig;
    private BiConsumer<String, Integer> shapeSelectionHandler; // Handler for shape selection (key, orientation)
    
    public ShapePalettePanel(ShapeConfig shapeConfig) {
        this.shapeConfig = shapeConfig;
        initializeComponents();
        setupLayout();
    }
    
    private void initializeComponents() {
        palettePanel1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        palettePanel2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Combine both palettes in a single panel with 2 rows
        JPanel palettesPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        palettesPanel.add(palettePanel1);
        palettesPanel.add(palettePanel2);
        
        // Set preferred height to fit 2 rows of buttons
        setPreferredSize(new Dimension(0, 80)); // Height for 2 rows of buttons
        add(palettesPanel, BorderLayout.CENTER);
    }
    
    /**
     * Sets the handler to be called when a shape is selected.
     * @param handler Handler that receives (shapeKey, orientation)
     */
    public void setShapeSelectionHandler(BiConsumer<String, Integer> handler) {
        this.shapeSelectionHandler = handler;
    }
    
    /**
     * Creates a button with fixed size.
     * @param displayText Text to display on the button
     * @param shapeKey The actual shape key (for the handler)
     * @param orientation The orientation (1 or -1)
     * @return A button with fixed size
     */
    private JButton createFixedSizeButton(String displayText, String shapeKey, int orientation) {
        JButton button = new JButton(displayText);
        button.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        button.setMinimumSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        button.setMaximumSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        button.addActionListener(e -> {
            if (shapeSelectionHandler != null) {
                shapeSelectionHandler.accept(shapeKey, orientation);
            }
        });
        return button;
    }
    
    /**
     * Creates shape palettes with buttons for each shape key.
     * Two palettes: one for orientation = 1, one for orientation = -1.
     * Rectangles are placed at the end, and no buttons for rectangle orientation = -1.
     * Buttons are sorted in the same order as in JSON.
     */
    public void createPalettes() {
        // Get shapes in order from JSON
        List<ShapeInstance> shapesInOrder = shapeConfig.getShapesInOrder();
        
        // Clear existing buttons
        palettePanel1.removeAll();
        palettePanel2.removeAll();
        
        // Add shapes in JSON order
        for (ShapeInstance shape : shapesInOrder) {
            String key = shape.getKey();
            
            if (shape instanceof AnnularSector) {
                // Palette 1: orientation = 1
                JButton button1 = createFixedSizeButton(key, key, 1);
                palettePanel1.add(button1);
                
                // Palette 2: orientation = -1 (with "-" prefix)
                JButton button2 = createFixedSizeButton("-" + key, key, -1);
                palettePanel2.add(button2);
            } else if (shape instanceof Rectangle) {
                // Add rectangles to palette 1 only (orientation = 1)
                JButton button = createFixedSizeButton(key, key, 1);
                palettePanel1.add(button);
            }
        }
        
        // Refresh panels
        palettePanel1.revalidate();
        palettePanel1.repaint();
        palettePanel2.revalidate();
        palettePanel2.repaint();
    }
}

