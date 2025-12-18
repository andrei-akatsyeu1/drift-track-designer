package com.trackdraw.view;

import com.trackdraw.config.ShapeConfig;
import com.trackdraw.model.AnnularSector;
import com.trackdraw.model.ShapeInstance;
import com.trackdraw.model.ShapeSequence;
import java.util.function.Consumer;

/**
 * Controller for managing shape sequence operations (add, remove, clear, selection).
 */
public class ShapeSequenceController {
    private ShapeConfig shapeConfig;
    private ShapeSequence activeSequence;
    private ShapeListPanel shapeListPanel;
    private DrawingCoordinator drawingCoordinator;
    private Consumer<String> statusMessageHandler;
    
    public ShapeSequenceController(ShapeConfig shapeConfig, ShapeListPanel shapeListPanel, 
                                   DrawingCoordinator drawingCoordinator) {
        this.shapeConfig = shapeConfig;
        this.shapeListPanel = shapeListPanel;
        this.drawingCoordinator = drawingCoordinator;
    }
    
    /**
     * Sets the status message handler for displaying validation errors.
     */
    public void setStatusMessageHandler(Consumer<String> handler) {
        this.statusMessageHandler = handler;
    }
    
    /**
     * Sets the active sequence.
     */
    public void setActiveSequence(ShapeSequence sequence) {
        this.activeSequence = sequence;
        shapeListPanel.setActiveSequence(sequence);
    }
    
    /**
     * Adds a shape to the active sequence.
     */
    public void addShapeToSequence(String key, int orientation) {
        if (activeSequence == null) {
            return;
        }
        
        ShapeInstance shapeTemplate = shapeConfig.getShape(key);
        if (shapeTemplate == null) {
            return;
        }
        
        // Create new shape instance
        ShapeInstance newShapeInstance = createShapeInstance(shapeTemplate);
        
        // Set orientation (only for annular sectors, rectangles always have orientation = 1)
        if (newShapeInstance instanceof AnnularSector) {
            newShapeInstance.setOrientation(orientation);
        } else {
            newShapeInstance.setOrientation(1); // Rectangles always have orientation = 1
        }
        
        // Check if a shape is selected in the list
        int selectedIndex = shapeListPanel.getSelectedIndex();
        int insertIndex;
        if (selectedIndex >= 0 && selectedIndex < activeSequence.size()) {
            // Insert after selected shape
            insertIndex = selectedIndex + 1;
        } else {
            // Add at the end
            insertIndex = activeSequence.size();
        }
        
        // Insert the shape at the calculated index
        // (insertShape handles color flag, deactivation, and activation)
        try {
            activeSequence.insertShape(insertIndex, newShapeInstance);
            
            // Update list model
            shapeListPanel.updateShapeList();
            
            // Keep selection on the newly added shape
            shapeListPanel.setSelectedIndex(insertIndex);
            
            // Redraw (colors will be recalculated in drawAll)
            drawingCoordinator.drawAll();
        } catch (IllegalArgumentException e) {
            // Validation failed - show error message
            if (statusMessageHandler != null) {
                statusMessageHandler.accept(e.getMessage());
            }
        }
    }
    
    /**
     * Removes the selected shape from the active sequence, or the last one if none is selected.
     * After removal, activates the previous shape (or first if first was removed).
     */
    public void removeSelectedOrLastShape() {
        if (activeSequence == null || activeSequence.isEmpty()) {
            return;
        }
        
        int selectedIndex = shapeListPanel.getSelectedIndex();
        int removeIndex;
        
        if (selectedIndex >= 0 && selectedIndex < activeSequence.size()) {
            // Remove selected shape
            removeIndex = selectedIndex;
        } else {
            // Remove last shape if nothing is selected
            removeIndex = activeSequence.size() - 1;
        }
        
        // Deactivate the shape before removing
        ShapeInstance shapeToRemove = activeSequence.getShape(removeIndex);
        if (shapeToRemove != null) {
            shapeToRemove.setActive(false);
        }
        
        activeSequence.removeShape(removeIndex);
        
        // Determine which shape to activate (previous shape)
        int nextActiveIndex = -1;
        if (!activeSequence.isEmpty()) {
            if (removeIndex > 0) {
                // If we removed a shape that wasn't the first, activate the previous shape
                nextActiveIndex = removeIndex - 1;
            } else {
                // If we removed the first shape (index 0), activate the new first shape (index 0)
                nextActiveIndex = 0;
            }
        }
        
        // Activate the previous shape if available
        if (nextActiveIndex >= 0) {
            activeSequence.activateShape(nextActiveIndex);
        }
        
        shapeListPanel.updateShapeList();
        
        // Select the active shape if available
        if (nextActiveIndex >= 0) {
            shapeListPanel.setSelectedIndex(nextActiveIndex);
        }
        
        // Redraw (colors will be recalculated in drawAll)
        drawingCoordinator.drawAll();
    }
    
    /**
     * Clears the active shape sequence.
     */
    public void clearSequence() {
        if (activeSequence != null) {
            activeSequence.clear();
            shapeListPanel.updateShapeList();
            drawingCoordinator.drawAll();
        }
    }
    
    /**
     * Called when shape selection changes in the list.
     */
    public void onShapeSelectionChanged() {
        if (activeSequence == null) {
            return;
        }
        
        int selectedIndex = shapeListPanel.getSelectedIndex();
        
        // Deactivate all shapes and activate selected shape
        activeSequence.activateShape(selectedIndex);
        
        // Update list display (without triggering selection events)
        shapeListPanel.updateShapeList();
        
        // Ensure the active shape is selected in the list
        if (selectedIndex >= 0 && selectedIndex < shapeListPanel.getModel().getSize()) {
            shapeListPanel.setSelectedIndex(selectedIndex);
        }
        
        // Redraw to update colors (colors will be recalculated in drawAll)
        drawingCoordinator.drawAll();
    }
    
    /**
     * Toggles the force invert color flag for the selected shape.
     */
    public void toggleInvertColor() {
        if (activeSequence == null || activeSequence.isEmpty()) {
            return;
        }
        
        int selectedIndex = shapeListPanel.getSelectedIndex();
        if (selectedIndex < 0 || selectedIndex >= activeSequence.size()) {
            return;
        }
        
        ShapeInstance selectedShape = activeSequence.getShape(selectedIndex);
        if (selectedShape != null) {
            selectedShape.setForceInvertColor(!selectedShape.isForceInvertColor());
            // Redraw (colors will be recalculated in drawAll)
            drawingCoordinator.drawAll();
        }
    }
    
    /**
     * Creates a new ShapeInstance from a template.
     */
    private ShapeInstance createShapeInstance(ShapeInstance template) {
        return template.copy();
    }
}

