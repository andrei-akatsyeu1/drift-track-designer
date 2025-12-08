package com.trackdraw.view;

import com.trackdraw.model.AnnularSector;
import com.trackdraw.model.ShapeInstance;
import com.trackdraw.model.ShapeSequence;

import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import java.awt.*;

/**
 * Panel component for displaying and managing the shape list for the active sequence.
 */
public class ShapeListPanel extends JPanel {
    private JList<String> shapeList;
    private DefaultListModel<String> shapeListModel;
    private ShapeSequence activeSequence;
    
    public ShapeListPanel() {
        initializeComponents();
        setupLayout();
    }
    
    private void initializeComponents() {
        shapeListModel = new DefaultListModel<>();
        shapeList = new JList<>(shapeListModel);
        shapeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        JLabel label = new JLabel("Shape Sequence:");
        add(label, BorderLayout.NORTH);
        
        JScrollPane scrollPane = new JScrollPane(shapeList);
        scrollPane.setPreferredSize(new Dimension(250, 0));
        add(scrollPane, BorderLayout.CENTER);
    }
    
    /**
     * Sets the active sequence and updates the list display.
     */
    public void setActiveSequence(ShapeSequence sequence) {
        this.activeSequence = sequence;
        updateShapeList();
    }
    
    /**
     * Updates the shape list model to reflect the active sequence.
     * Temporarily disables selection listener to prevent circular calls.
     */
    public void updateShapeList() {
        // Temporarily disable selection listener to prevent circular calls
        ListSelectionListener[] listeners = shapeList.getListSelectionListeners();
        for (ListSelectionListener listener : listeners) {
            shapeList.removeListSelectionListener(listener);
        }
        
        try {
            shapeListModel.clear();
            
            int activeShapeIndex = -1;
            if (activeSequence != null) {
                for (int i = 0; i < activeSequence.size(); i++) {
                    ShapeInstance shape = activeSequence.getShape(i);
                    String displayKey = shape.getKey();
                    if (shape instanceof AnnularSector && shape.getOrientation() == -1) {
                        displayKey = "-" + displayKey;
                    }
                    if (shape.isActive()) {
                        displayKey += " (active)";
                        activeShapeIndex = i; // Remember the active shape index
                    }
                    shapeListModel.addElement(displayKey);
                }
            }
            
            // Select the active shape if there is one
            if (activeShapeIndex >= 0 && activeShapeIndex < shapeListModel.getSize()) {
                shapeList.setSelectedIndex(activeShapeIndex);
            }
        } finally {
            // Re-enable selection listeners
            for (ListSelectionListener listener : listeners) {
                shapeList.addListSelectionListener(listener);
            }
        }
    }
    
    /**
     * Gets the selected index in the shape list.
     */
    public int getSelectedIndex() {
        return shapeList.getSelectedIndex();
    }
    
    /**
     * Sets the selected index in the shape list.
     */
    public void setSelectedIndex(int index) {
        shapeList.setSelectedIndex(index);
    }
    
    /**
     * Clears the selection in the shape list.
     */
    public void clearSelection() {
        shapeList.clearSelection();
    }
    
    /**
     * Adds a selection listener to the shape list.
     */
    public void addSelectionListener(ListSelectionListener listener) {
        shapeList.addListSelectionListener(listener);
    }
    
    /**
     * Gets the JList component (for advanced operations if needed).
     */
    public JList<String> getList() {
        return shapeList;
    }
    
    /**
     * Gets the list model (for advanced operations if needed).
     */
    public DefaultListModel<String> getModel() {
        return shapeListModel;
    }
}

