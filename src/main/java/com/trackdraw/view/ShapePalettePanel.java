package com.trackdraw.view;

import com.trackdraw.config.ShapeConfig;
import com.trackdraw.model.ShapeInstance;
import com.trackdraw.model.AnnularSector;
import com.trackdraw.model.Rectangle;
import com.trackdraw.model.HalfCircle;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * Panel containing shape palettes for selecting shapes with different orientations.
 * Two palettes: one for orientation = 1, one for orientation = -1.
 */
public class ShapePalettePanel extends JPanel {
    private static final int BUTTON_WIDTH = 70; // Fixed width for all buttons (increased to fit "-05", "-10", etc.)
    private static final int BUTTON_HEIGHT = 30; // Fixed height for all buttons
    
    private JPanel palettePanel1; // Palette for orientation = 1
    private JPanel palettePanel2; // Palette for orientation = -1
    private ShapeConfig shapeConfig;
    private BiConsumer<String, Integer> shapeSelectionHandler; // Handler for shape selection (key, orientation)
    private Runnable removeHandler; // Handler for remove button
    private Runnable invertColorHandler; // Handler for invert color button
    
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
        
        // Set preferred height to fit 2 rows of buttons plus some padding
        // Increased height to provide more vertical space and prevent clipping
        setPreferredSize(new Dimension(0, 120)); // Height for 2 rows of buttons with extra padding
        add(palettesPanel, BorderLayout.CENTER);
        
        // Add Remove and Invert Color buttons at the bottom, wrapped in a FlowLayout panel to prevent full width
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        Font buttonFont = new Font(Font.SANS_SERIF, Font.PLAIN, 20);
        
        JButton removeButton = new JButton("🗑");
        removeButton.setFont(buttonFont);
        removeButton.setToolTipText("Remove selected or last shape");
        removeButton.addActionListener(e -> {
            if (removeHandler != null) {
                removeHandler.run();
            }
        });
        buttonPanel.add(removeButton);
        
        JButton invertColorButton = new JButton("↻🎨");
        invertColorButton.setFont(buttonFont);
        invertColorButton.setToolTipText("Invert color of selected shape");
        invertColorButton.addActionListener(e -> {
            if (invertColorHandler != null) {
                invertColorHandler.run();
            }
        });
        buttonPanel.add(invertColorButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Sets the handler to be called when a shape is selected.
     * @param handler Handler that receives (shapeKey, orientation)
     */
    public void setShapeSelectionHandler(BiConsumer<String, Integer> handler) {
        this.shapeSelectionHandler = handler;
    }
    
    /**
     * Sets the handler to be called when the Remove button is clicked.
     * @param handler Handler to remove selected or last shape
     */
    public void setRemoveHandler(Runnable handler) {
        this.removeHandler = handler;
    }
    
    /**
     * Sets the handler to be called when the Invert Color button is clicked.
     * @param handler Handler to invert color of selected shape
     */
    public void setInvertColorHandler(Runnable handler) {
        this.invertColorHandler = handler;
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
        // Set fixed size to ensure consistent button dimensions
        button.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        button.setMinimumSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        button.setMaximumSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        // Set font for shape key buttons (smaller than other buttons)
        Font shapeButtonFont = new Font(Font.SANS_SERIF, Font.PLAIN, 16);
        button.setFont(shapeButtonFont);
        // Ensure text is not clipped - set horizontal and vertical text position
        button.setHorizontalTextPosition(SwingConstants.CENTER);
        button.setVerticalTextPosition(SwingConstants.CENTER);
        String tooltip = "Add shape: " + displayText + (orientation == -1 ? " (orientation: -1)" : "");
        button.setToolTipText(tooltip);
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
            } else if (shape instanceof HalfCircle) {
                // Add half-circles to palette 1 only (orientation = 1)
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

