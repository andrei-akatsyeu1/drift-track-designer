package com.trackdraw.view;

import com.trackdraw.config.ShapeConfig;
import com.trackdraw.model.ShapeInstance;
import com.trackdraw.model.AnnularSector;
import com.trackdraw.model.Rectangle;
import com.trackdraw.model.ShapeSequence;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Main application window with drawing panel and shape input field.
 */
public class MainWindow extends JFrame {
    private DrawingPanel drawingPanel;
    private ScaleControlPanel scaleControlPanel;
    private ShapePalettePanel shapePalettePanel;
    private ShapeSequencePanel shapeSequencePanel;
    private JList<String> shapeSequenceList;
    private DefaultListModel<String> shapeSequenceModel;
    private ShapeConfig shapeConfig;
    private List<ShapeSequence> allSequences;
    private ShapeSequence activeSequence;
    
    public MainWindow() {
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        loadShapes();
        createShapePalettes();
    }
    
    private void initializeComponents() {
        setTitle("Track Draw - Shape Drawing Application");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        drawingPanel = new DrawingPanel();
        shapeConfig = new ShapeConfig();
        
        // Create UI component panels
        scaleControlPanel = new ScaleControlPanel();
        shapePalettePanel = new ShapePalettePanel(shapeConfig);
        shapeSequencePanel = new ShapeSequencePanel();
        
        shapeSequenceModel = new DefaultListModel<>();
        shapeSequenceList = new JList<>(shapeSequenceModel);
        shapeSequenceList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Store the listener so we can temporarily disable it
        javax.swing.event.ListSelectionListener shapeSelectionListener = e -> {
            if (!e.getValueIsAdjusting()) {
                onShapeSelectionChanged();
            }
        };
        shapeSequenceList.addListSelectionListener(shapeSelectionListener);
        
        allSequences = new ArrayList<>();
        
        // Create default "Main" sequence
        ShapeSequence mainSequence = new ShapeSequence("Main");
        mainSequence.setActive(true);
        allSequences.add(mainSequence);
        activeSequence = mainSequence;
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Top panel with controls
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        topPanel.add(scaleControlPanel);
        
        add(topPanel, BorderLayout.NORTH);
        
        // Create split pane: left side for sequence management and shape list, right side for drawing
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        
        // Left side: sequence management panel and shape list
        JPanel leftPanel = new JPanel(new BorderLayout());
        
        // Top: ShapeSequence management panel
        leftPanel.add(shapeSequencePanel, BorderLayout.NORTH);
        
        // Center: shape list for active sequence
        JPanel sequencePanel = new JPanel(new BorderLayout());
        sequencePanel.add(new JLabel("Shape Sequence:"), BorderLayout.NORTH);
        
        // Clear button
        JButton clearButton = new JButton("Clear");
        clearButton.addActionListener(e -> clearSequence());
        sequencePanel.add(clearButton, BorderLayout.SOUTH);
        
        JScrollPane listScrollPane = new JScrollPane(shapeSequenceList);
        listScrollPane.setPreferredSize(new Dimension(250, 0));
        sequencePanel.add(listScrollPane, BorderLayout.CENTER);
        
        leftPanel.add(sequencePanel, BorderLayout.CENTER);
        
        splitPane.setLeftComponent(leftPanel);
        splitPane.setRightComponent(drawingPanel);
        splitPane.setDividerLocation(300);
        splitPane.setResizeWeight(0.0); // Don't resize left panel
        
        // Center panel: palette above canvas
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(shapePalettePanel, BorderLayout.NORTH);
        centerPanel.add(splitPane, BorderLayout.CENTER);
        
        add(centerPanel, BorderLayout.CENTER);
        
        pack();
        setLocationRelativeTo(null);
    }
    
    private void setupEventHandlers() {
        // Window closing handler
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                // Note: shapes.json is managed manually, not saved by the application
                // Other data (drawing results) will be saved to a different file later
                System.exit(0);
            }
        });
        
        // Setup handlers for UI components
        scaleControlPanel.setScaleChangeHandler(scale -> {
            drawAll();
        });
        
        shapePalettePanel.setShapeSelectionHandler((key, orientation) -> {
            addShapeToSequence(key, orientation);
        });
        
        // Set remove handler for the palette panel
        shapePalettePanel.setRemoveHandler(() -> removeSelectedOrLastShape());
        
        // Setup handler for sequence panel
        shapeSequencePanel.setSequenceChangeHandler(seq -> {
            // Sync sequences from panel to MainWindow
            allSequences = shapeSequencePanel.getSequences();
            
            // Store the previous active sequence's active shape before switching
            // (The active shape state is already preserved in each shape's isActive() flag)
            
            activeSequence = seq;
            
            // Don't deactivate shapes - preserve active state for each sequence
            // Just update the UI to reflect the current active sequence's active shape
            
            updateShapeList();
            
            // Select the active shape in the shape list if there is one
            if (activeSequence != null) {
                for (int i = 0; i < activeSequence.size(); i++) {
                    ShapeInstance shape = activeSequence.getShape(i);
                    if (shape != null && shape.isActive()) {
                        shapeSequenceList.setSelectedIndex(i);
                        break;
                    }
                }
            }
            
            drawAll();
        });
        
        // Setup suppliers so panel can access active sequence and all sequences
        shapeSequencePanel.setSequenceSuppliers(
            () -> activeSequence,
            () -> allSequences
        );
        
        // Initialize sequences list with default "Main" sequence
        shapeSequencePanel.setSequences(allSequences);
        updateShapeList();
    }
    
    private void loadShapes() {
        try {
            shapeConfig.loadShapes();
        } catch (IOException e) {
            System.err.println("Error loading shapes: " + e.getMessage());
        }
    }
    
    /**
     * Creates shape palettes with buttons for each shape key.
     */
    private void createShapePalettes() {
        shapePalettePanel.createPalettes();
    }
    
    /**
     * Adds a shape to the active sequence following workflow:
     * 3.1) Add new shape button pressed
     * 3.2) New object created and added to list (AlignPosition is null)
     * 3.3) canvas cleared
     * 3.4) start draw all shapes from the list by sequence
     * 3.5) for first shape set default AlignPosition (center of canvas or initialX/Y, angle from rotation field)
     * 3.6) draw first shape, set returned AlignPosition to next shape
     * 3.7) loop till the end of shape list
     */
    private void addShapeToSequence(String key, int orientation) {
        if (activeSequence == null) {
            JOptionPane.showMessageDialog(this,
                "No active sequence. Please create a sequence first.",
                "No Active Sequence",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        ShapeInstance shapeTemplate = shapeConfig.getShape(key);

        // 3.2) New object created and added to list (AlignPosition is null)
        ShapeInstance newShapeInstance = createShapeInstance(shapeTemplate);
        
        // Set orientation (only for annular sectors, rectangles always have orientation = 1)
        if (newShapeInstance instanceof AnnularSector) {
            newShapeInstance.setOrientation(orientation);
        } else {
            newShapeInstance.setOrientation(1); // Rectangles always have orientation = 1
        }
        
        // Check if a shape is selected in the list
        int selectedIndex = shapeSequenceList.getSelectedIndex();
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
        activeSequence.insertShape(insertIndex, newShapeInstance);
        
        // Update list model
        updateShapeList();
        
        // Keep selection on the newly added shape
        shapeSequenceList.setSelectedIndex(insertIndex);
        
        // Redraw (colors will be recalculated in drawAll)
        drawAll();
    }
    
    /**
     * Removes the selected shape from the active sequence, or the last one if none is selected.
     */
    private void removeSelectedOrLastShape() {
        if (activeSequence == null || activeSequence.isEmpty()) {
            return;
        }
        
        int selectedIndex = shapeSequenceList.getSelectedIndex();
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
        
        updateShapeList();
        
        // Clear selection
        shapeSequenceList.clearSelection();
        
        // Redraw (colors will be recalculated in drawAll)
        drawAll();
    }
    
    /**
     * Called when shape selection changes in the list.
     */
    private void onShapeSelectionChanged() {
        if (activeSequence == null) {
            return;
        }
        
        int selectedIndex = shapeSequenceList.getSelectedIndex();
        
        // Deactivate all shapes
        for (ShapeInstance shape : activeSequence.getShapes()) {
            shape.setActive(false);
        }
        
        // Activate selected shape
        if (selectedIndex >= 0 && selectedIndex < activeSequence.size()) {
            ShapeInstance selectedShape = activeSequence.getShape(selectedIndex);
            if (selectedShape != null) {
                selectedShape.setActive(true);
            }
        }
        
        // Update list display (without triggering selection events)
        updateShapeList();
        
        // Ensure the active shape is selected in the list
        if (selectedIndex >= 0 && selectedIndex < shapeSequenceModel.getSize()) {
            shapeSequenceList.setSelectedIndex(selectedIndex);
        }
        
        // Redraw to update colors (colors will be recalculated in drawAll)
        drawAll();
    }
    
    /**
     * Updates the shape list model to reflect the active sequence.
     * Temporarily disables selection listener to prevent circular calls.
     */
    private void updateShapeList() {
        // Temporarily disable selection listener to prevent circular calls
        javax.swing.event.ListSelectionListener[] listeners = shapeSequenceList.getListSelectionListeners();
        for (javax.swing.event.ListSelectionListener listener : listeners) {
            shapeSequenceList.removeListSelectionListener(listener);
        }
        
        try {
            shapeSequenceModel.clear();
            
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
                    shapeSequenceModel.addElement(displayKey);
                }
            }
            
            // Select the active shape if there is one
            if (activeShapeIndex >= 0 && activeShapeIndex < shapeSequenceModel.getSize()) {
                shapeSequenceList.setSelectedIndex(activeShapeIndex);
            }
        } finally {
            // Re-enable selection listeners
            for (javax.swing.event.ListSelectionListener listener : listeners) {
                shapeSequenceList.addListSelectionListener(listener);
            }
        }
    }
    
    /**
     * Creates a new ShapeInstance from a template.
     */
    private ShapeInstance createShapeInstance(ShapeInstance template) {
        if (template instanceof AnnularSector) {
            AnnularSector sector = (AnnularSector) template;
            return new AnnularSector(sector.getKey(), sector.getExternalDiameter(), 
                sector.getAngleDegrees(), sector.getWidth());
        } else if (template instanceof Rectangle) {
            Rectangle rect = (Rectangle) template;
            return new Rectangle(rect.getKey(), rect.getLength(), rect.getWidth());
        }
        throw new IllegalArgumentException("Unknown shape type: " + template.getType());
    }
    
    /**
     * Clears the active shape sequence.
     */
    private void clearSequence() {
        if (activeSequence != null) {
            activeSequence.clear();
            updateShapeList();
            drawAll();
        }
    }
    
    /**
     * Updates the sequence panel with current sequences.
     */
    private void updateSequencePanel() {
        shapeSequencePanel.setSequences(allSequences);
    }
    
    /**
     * Recalculates colors for all sequences.
     * Processes sequences in order - since sequences can only depend on sequences above them,
     * we can safely recalculate them in list order without additional dependency checks.
     */
    private void recalculateAllColors() {
        // Sync sequences from panel to MainWindow
        allSequences = shapeSequencePanel.getSequences();
        
        // Process sequences in order - each sequence can only depend on sequences above it
        // The linked shape's color is already calculated because it's from a sequence processed earlier
        for (ShapeSequence sequence : allSequences) {
            sequence.recalculateColors();
        }
    }
    
    /**
     * Draws all shapes following workflow:
     * 3.3) canvas cleared
     * 3.4) start draw all shapes from the list by sequence
     * 3.5) for first shape set default AlignPosition (center of canvas, angle 0)
     * 3.6) draw first shape, set returned AlignPosition to next shape
     * 3.7) loop till the end of shape list
     */
    private void drawAll() {
        // Sync sequences from panel to MainWindow
        allSequences = shapeSequencePanel.getSequences();
        activeSequence = shapeSequencePanel.getActiveSequence();
        
        // Recalculate colors before drawing
        recalculateAllColors();
        
        // Update shape list for active sequence
        updateShapeList();
        
        // Update drawing panel with all sequences
        drawingPanel.setSequences(allSequences);
        
        // Trigger repaint
        drawingPanel.drawAll();
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            MainWindow window = new MainWindow();
            window.setVisible(true);
        });
    }
}
