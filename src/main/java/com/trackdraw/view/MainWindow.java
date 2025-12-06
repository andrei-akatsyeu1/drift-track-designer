package com.trackdraw.view;

import com.trackdraw.config.ShapeConfig;
import com.trackdraw.model.ShapeInstance;
import com.trackdraw.model.AnnularSector;
import com.trackdraw.model.Rectangle;

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
    private FirstShapePositionPanel firstShapePositionPanel;
    private ScaleControlPanel scaleControlPanel;
    private ShapePalettePanel shapePalettePanel;
    private JList<String> shapeSequenceList;
    private DefaultListModel<String> shapeSequenceModel;
    private ShapeConfig shapeConfig;
    private double rotationAngle = 0.0; // Rotation angle in degrees (for first shape only)
    private double initialX = -1; // Initial X position (-1 means use center)
    private double initialY = -1; // Initial Y position (-1 means use center)
    private List<ShapeInstance> shapeSequence = new ArrayList<>();
    
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
        firstShapePositionPanel = new FirstShapePositionPanel();
        scaleControlPanel = new ScaleControlPanel();
        shapePalettePanel = new ShapePalettePanel(shapeConfig);
        
        shapeSequenceModel = new DefaultListModel<>();
        shapeSequenceList = new JList<>(shapeSequenceModel);
        shapeSequenceList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Top panel with controls
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        topPanel.add(firstShapePositionPanel);
        topPanel.add(scaleControlPanel);
        
        add(topPanel, BorderLayout.NORTH);
        
        // Create split pane: left side for sequence list, right side for drawing
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        
        // Left side: sequence list
        JPanel leftPanel = new JPanel(new BorderLayout());
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
        splitPane.setDividerLocation(250);
        splitPane.setResizeWeight(0.0); // Don't resize left panel
        
        // Center panel: palette above canvas
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(shapePalettePanel, BorderLayout.NORTH);
        centerPanel.add(splitPane, BorderLayout.CENTER);
        
        add(centerPanel, BorderLayout.CENTER);
        
        // Setup click handler for canvas
        drawingPanel.setClickHandler(point -> {
            initialX = point.x;
            initialY = point.y;
            firstShapePositionPanel.setPosition(initialX, initialY);
            drawingPanel.setInitialPosition(initialX, initialY);
            drawAll();
        });
        
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
        firstShapePositionPanel.setRotationChangeHandler(angle -> {
            rotationAngle = angle;
            drawAll();
        });
        
        scaleControlPanel.setScaleChangeHandler(scale -> {
            drawAll();
        });
        
        shapePalettePanel.setShapeSelectionHandler((key, orientation) -> {
            addShapeToSequence(key, orientation);
        });
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
     * Adds a shape to the sequence following workflow:
     * 3.1) Add new shape button pressed
     * 3.2) New object created and added to list (AlignPosition is null)
     * 3.3) canvas cleared
     * 3.4) start draw all shapes from the list by sequence
     * 3.5) for first shape set default AlignPosition (center of canvas or initialX/Y, angle from rotation field)
     * 3.6) draw first shape, set returned AlignPosition to next shape
     * 3.7) loop till the end of shape list
     */
    private void addShapeToSequence(String key, int orientation) {
        ShapeInstance shapeTemplate = shapeConfig.getShape(key);
        
        if (shapeTemplate == null) {
            JOptionPane.showMessageDialog(this, 
                "Shape with key '" + key + "' not found.", 
                "Shape Not Found", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // 3.2) New object created and added to list (AlignPosition is null)
        ShapeInstance newShapeInstance = createShapeInstance(shapeTemplate);
        
        // Set orientation (only for annular sectors, rectangles always have orientation = 1)
        if (newShapeInstance instanceof AnnularSector) {
            newShapeInstance.setOrientation(orientation);
        } else {
            newShapeInstance.setOrientation(1); // Rectangles always have orientation = 1
        }
        
        shapeSequence.add(newShapeInstance);
        
        // Update list model
        String displayKey = (orientation == -1 && newShapeInstance instanceof AnnularSector) ? "-" + key : key;
        shapeSequenceModel.addElement(displayKey);
        
        // 3.3-3.7) Draw all shapes
        drawAll();
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
     * Clears the shape sequence.
     */
    private void clearSequence() {
        shapeSequence.clear();
        shapeSequenceModel.clear();
        drawAll();
    }
    
    /**
     * Draws all shapes following workflow:
     * 3.3) canvas cleared
     * 3.4) start draw all shapes from the list by sequence
     * 3.5) for first shape set default AlignPosition (center of canvas or initialX/Y, angle from rotation field)
     * 3.6) draw first shape, set returned AlignPosition to next shape
     * 3.7) loop till the end of shape list
     */
    private void drawAll() {
        // Get rotation angle from panel
        rotationAngle = firstShapePositionPanel.getRotationAngle();
        
        // Set initial rotation angle for first shape
        drawingPanel.setInitialRotationAngle(rotationAngle);
        
        // Set initial position
        drawingPanel.setInitialPosition(initialX, initialY);
        
        // Update drawing panel with current sequence
        drawingPanel.setShapeSequence(shapeSequence);
        
        // Trigger repaint (which will call drawAll internally)
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
