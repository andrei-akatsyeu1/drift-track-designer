package com.trackdraw.view;

import com.trackdraw.config.SequenceManager;
import com.trackdraw.config.ShapeConfig;
import com.trackdraw.model.ShapeSequence;
import com.trackdraw.report.ShapeReportGenerator;
import java.awt.image.BufferedImage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Main application window - orchestrates UI layout and delegates to controllers.
 */
public class MainWindow extends JFrame {
    // UI Components
    private DrawingPanel drawingPanel;
    private ScaleControlPanel scaleControlPanel;
    private BackgroundImageScalePanel backgroundImageScalePanel;
    private JCheckBox measureCheckBox;
    private JCheckBox showKeysCheckBox;
    private ShapePalettePanel shapePalettePanel;
    private ShapeSequencePanel shapeSequencePanel;
    private ShapeListPanel shapeListPanel;
    private StatusBar statusBar;
    
    // Controllers
    private ShapeConfig shapeConfig;
    private SequenceManager sequenceManager;
    private ShapeSequenceController sequenceController;
    private DrawingCoordinator drawingCoordinator;
    private AlignmentKeyboardController keyboardController;
    
    // State
    private List<ShapeSequence> allSequences;
    private ShapeSequence activeSequence;
    private String loadedJsonFileName; // Name of loaded JSON file (without extension)
    
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
        
        // Initialize UI components
        drawingPanel = new DrawingPanel();
        drawingPanel.setName("drawingPanel");
        shapeConfig = new ShapeConfig();
        sequenceManager = new SequenceManager();
        scaleControlPanel = new ScaleControlPanel();
        backgroundImageScalePanel = new BackgroundImageScalePanel();
        shapePalettePanel = new ShapePalettePanel(shapeConfig);
        shapeSequencePanel = new ShapeSequencePanel();
        shapeListPanel = new ShapeListPanel();
        statusBar = new StatusBar();
        
        // Initialize controllers
        drawingCoordinator = new DrawingCoordinator(drawingPanel, shapeSequencePanel, shapeListPanel);
        sequenceController = new ShapeSequenceController(shapeConfig, shapeListPanel, drawingCoordinator);
        keyboardController = new AlignmentKeyboardController(drawingPanel, shapeSequencePanel, drawingCoordinator);
        
        // Initialize state
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
        topPanel.add(backgroundImageScalePanel);
        
        // Measurement tool checkbox
        measureCheckBox = new JCheckBox("📏");
        Font measureFont = new Font(Font.SANS_SERIF, Font.PLAIN, 20);
        measureCheckBox.setFont(measureFont);
        measureCheckBox.setToolTipText("Measurement Tool");
        measureCheckBox.addActionListener(e -> {
            boolean active = measureCheckBox.isSelected();
            drawingPanel.getMeasurementTool().setActive(active);
            drawingPanel.repaint();
        });
        topPanel.add(measureCheckBox);
        
        // Show keys checkbox
        showKeysCheckBox = new JCheckBox("Show Keys");
        showKeysCheckBox.setToolTipText("Show shape keys on canvas");
        showKeysCheckBox.addActionListener(e -> {
            boolean showKeys = showKeysCheckBox.isSelected();
            drawingPanel.setShowKeys(showKeys);
            drawingPanel.repaint();
        });
        topPanel.add(showKeysCheckBox);
        
        // Help button with keyboard shortcuts tooltip
        JButton helpButton = new JButton("?");
        helpButton.setFont(measureFont);
            String tooltipText = "<html><b>Keyboard Shortcuts:</b><br>" +
            "<b>Canvas Panning:</b><br>" +
            "Left Mouse Drag: Pan canvas<br><br>" +
            "<b>Sequence Alignment (when sequence not linked):</b><br>" +
            "Arrow Keys: Move by 10px<br>" +
            "Arrow + Ctrl: Move by 1px<br>" +
            "Shift + Arrow Keys: Move all sequences by 10px<br>" +
            "Shift + Ctrl + Arrow Keys: Move all sequences by 1px<br>" +
            "+ / -: Rotate by 10°<br>" +
            "+ / - + Ctrl: Rotate by 1°<br><br>" +
            "<b>Global Scale:</b><br>" +
            "Mouse Wheel: Scale by 10%<br>" +
            "Mouse Wheel + Ctrl: Scale by 1%<br><br>" +
            "<b>Image Scale:</b><br>" +
            "Page Up/Down: Scale by 10%<br>" +
            "Page Up/Down + Ctrl: Scale by 1%<br><br>" +
            "<b>Measurement Tool:</b><br>" +
            "Esc: Disable measurement tool</html>";
        helpButton.setToolTipText(tooltipText);
        topPanel.add(helpButton);
        
        add(topPanel, BorderLayout.NORTH);
        
        // Create split pane: left side for sequence management and shape list, right side for drawing
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        
        // Left side: sequence management panel and shape list
        JPanel leftPanel = new JPanel(new BorderLayout());
        
        // Top: ShapeSequence management panel
        leftPanel.add(shapeSequencePanel, BorderLayout.NORTH);
        
        // Center: shape list panel with clear button
        JPanel sequencePanel = new JPanel(new BorderLayout());
        
        // Clear button
        JButton clearButton = new JButton("🗑");
        // Use a font that supports Unicode well, with larger size
        Font largerFont = new Font(Font.SANS_SERIF, Font.PLAIN, 20);
        clearButton.setFont(largerFont);
        clearButton.setToolTipText("Clear active sequence");
        clearButton.addActionListener(e -> sequenceController.clearSequence());
        sequencePanel.add(clearButton, BorderLayout.SOUTH);
        
        sequencePanel.add(shapeListPanel, BorderLayout.CENTER);
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
        add(statusBar, BorderLayout.SOUTH);
        
        pack();
        setLocationRelativeTo(null);
    }
    
    private void setupEventHandlers() {
        // Window closing handler
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                System.exit(0);
            }
        });
        
        // Setup handlers for UI components
        scaleControlPanel.setScaleChangeHandler(scale -> {
            drawingCoordinator.drawAll();
        });
        
        // Update scale control panel when scale changes via mouse wheel
        drawingPanel.setGlobalScaleChangeHandler(scale -> {
            scaleControlPanel.setScale(scale);
        });
        
        backgroundImageScalePanel.setScaleChangeHandler(scale -> {
            drawingPanel.setBackgroundImageScale(scale);
        });
        
        // Setup keyboard bindings for scale controls
        setupScaleKeyboardBindings();
        
        // Setup callback to sync measurement tool checkbox when Esc is pressed
        drawingPanel.setMeasurementDeactivatedCallback(() -> {
            measureCheckBox.setSelected(false);
        });
        
        shapePalettePanel.setShapeSelectionHandler((key, orientation) -> {
            sequenceController.addShapeToSequence(key, orientation);
        });
        
        // Set remove handler for the palette panel
        shapePalettePanel.setRemoveHandler(() -> sequenceController.removeSelectedOrLastShape());
        
        // Set invert color handler for the palette panel
        shapePalettePanel.setInvertColorHandler(() -> sequenceController.toggleInvertColor());
        
        // Set status message handler for sequence controller
        sequenceController.setStatusMessageHandler(message -> statusBar.setStatus(message, 5000));
        
        // Setup handler for sequence panel
        shapeSequencePanel.setSequenceChangeHandler(seq -> {
            // Sync sequences from panel to MainWindow
            allSequences = shapeSequencePanel.getSequences();
            activeSequence = seq;
            
            // Update sequence controller
            sequenceController.setActiveSequence(activeSequence);
            
            // Select the active shape in the shape list if there is one
            if (activeSequence != null) {
                for (int i = 0; i < activeSequence.size(); i++) {
                    if (activeSequence.getShape(i) != null && activeSequence.getShape(i).isActive()) {
                        shapeListPanel.setSelectedIndex(i);
                        break;
                    }
                }
            }
            
            drawingCoordinator.drawAll();
        });
        
        // Setup suppliers so panel can access active sequence and all sequences
        shapeSequencePanel.setSequenceSuppliers(
            () -> activeSequence,
            () -> allSequences
        );
        
        // Setup status message handler for shape sequence panel
        shapeSequencePanel.setStatusMessageHandler(message -> {
            statusBar.setStatus(message, 3000);
        });
        
        // Initialize sequences list with default "Main" sequence
        shapeSequencePanel.setSequences(allSequences);
        sequenceController.setActiveSequence(activeSequence);
        
        // Setup keyboard controls for adjusting alignment position
        keyboardController.setupKeyboardControls();
        
        // Setup shape list selection listener
        shapeListPanel.addSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                sequenceController.onShapeSelectionChanged();
            }
        });
        
        // Add menu bar with report option
        setupMenuBar();
    }
    
    /**
     * Sets up the menu bar with report and file options.
     */
    private void setupMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        
        // File menu
        JMenu fileMenu = new JMenu("File");
        
        JMenuItem loadItem = new JMenuItem("Load Sequences...");
        loadItem.addActionListener(e -> loadSequences());
        fileMenu.add(loadItem);
        
        JMenuItem saveItem = new JMenuItem("Save Sequences...");
        saveItem.addActionListener(e -> saveSequences());
        fileMenu.add(saveItem);
        
        fileMenu.addSeparator();
        
        JMenuItem loadImageItem = new JMenuItem("Load Background Image...");
        loadImageItem.addActionListener(e -> loadBackgroundImage());
        fileMenu.add(loadImageItem);
        
        JMenuItem clearImageItem = new JMenuItem("Clear Background Image");
        clearImageItem.addActionListener(e -> clearBackgroundImage());
        fileMenu.add(clearImageItem);
        
        fileMenu.addSeparator();
        
        JMenuItem exportItem = new JMenuItem("Export Image...");
        exportItem.addActionListener(e -> exportImage());
        fileMenu.add(exportItem);
        
        fileMenu.addSeparator();
        
        // Measurement tool toggle
        JCheckBoxMenuItem measureItem = new JCheckBoxMenuItem("Measurement Tool");
        measureItem.addActionListener(e -> {
            boolean active = measureItem.isSelected();
            drawingPanel.getMeasurementTool().setActive(active);
            drawingPanel.repaint();
        });
        fileMenu.add(measureItem);
        
        fileMenu.addSeparator();
        
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitItem);
        
        menuBar.add(fileMenu);
        
        // Report menu
        JMenu reportMenu = new JMenu("Report");
        JMenuItem generateReportItem = new JMenuItem("Generate Shape Report");
        generateReportItem.addActionListener(e -> showShapeReport());
        reportMenu.add(generateReportItem);
        
        menuBar.add(reportMenu);
        setJMenuBar(menuBar);
    }
    
    /**
     * Loads a background image.
     * If image is outside the images directory, copies it to images directory.
     */
    private void loadBackgroundImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(com.trackdraw.config.FileManager.getImagesDirectory());
        fileChooser.setDialogTitle("Load Background Image");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
            "Image Files", "jpg", "jpeg", "png", "gif", "bmp"));
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                File selectedFile = fileChooser.getSelectedFile();
                File imagesDir = com.trackdraw.config.FileManager.getImagesDirectory();
                
                // If file is outside images directory, copy it
                if (!selectedFile.getParentFile().equals(imagesDir)) {
                    File copiedFile = com.trackdraw.config.FileManager.copyImageToImagesDirectory(selectedFile);
                    drawingPanel.setBackgroundImage(copiedFile.getAbsolutePath());
                } else {
                    // File is already in images directory
                    drawingPanel.setBackgroundImage(selectedFile.getAbsolutePath());
                }
                
                // Reset scale to 100% when loading new image
                backgroundImageScalePanel.setScale(1.0);
                statusBar.setStatus("Background image loaded successfully", 3000);
            } catch (IOException e) {
                statusBar.setStatus("Error copying image: " + e.getMessage(), 5000);
            }
        }
    }
    
    /**
     * Clears the background image.
     */
    private void clearBackgroundImage() {
        drawingPanel.setBackgroundImage(null);
        backgroundImageScalePanel.setScale(1.0);
    }
    
    /**
     * Loads sequences from a file.
     */
    private void loadSequences() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(com.trackdraw.config.FileManager.getSavesDirectory());
        fileChooser.setDialogTitle("Load Sequences");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("JSON Files", "json"));
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                File selectedFile = fileChooser.getSelectedFile();
                SequenceManager.LoadResult loadResult = sequenceManager.loadSequences(selectedFile.getAbsolutePath());
                List<ShapeSequence> loadedSequences = loadResult.getSequences();
                
                if (loadedSequences.isEmpty()) {
                    statusBar.setStatus("No sequences found in file", 3000);
                    return;
                }
                
                // Store the loaded JSON file name (without extension) for export
                String fileName = selectedFile.getName();
                int lastDot = fileName.lastIndexOf('.');
                if (lastDot > 0) {
                    loadedJsonFileName = fileName.substring(0, lastDot);
                } else {
                    loadedJsonFileName = fileName;
                }
                
                // Replace current sequences
                allSequences = loadedSequences;
                shapeSequencePanel.setSequences(allSequences);
                
                // Load background image if present (convert relative path to absolute)
                if (loadResult.getBackgroundImagePath() != null) {
                    String absoluteImagePath = com.trackdraw.config.FileManager.toAbsoluteImagePath(loadResult.getBackgroundImagePath());
                    if (absoluteImagePath != null) {
                        drawingPanel.setBackgroundImage(absoluteImagePath);
                        drawingPanel.setBackgroundImageScale(loadResult.getBackgroundImageScale());
                        backgroundImageScalePanel.setScale(loadResult.getBackgroundImageScale());
                    } else {
                        // Image file not found, clear it
                        drawingPanel.setBackgroundImage(null);
                        backgroundImageScalePanel.setScale(1.0);
                        statusBar.setStatus("Background image not found: " + loadResult.getBackgroundImagePath(), 5000);
                    }
                } else {
                    // Clear background image if not in file
                    drawingPanel.setBackgroundImage(null);
                    backgroundImageScalePanel.setScale(1.0);
                }
                
                // Set first sequence as active if available
                if (!allSequences.isEmpty()) {
                    ShapeSequence firstSeq = allSequences.get(0);
                    firstSeq.setActive(true);
                    for (int i = 1; i < allSequences.size(); i++) {
                        allSequences.get(i).setActive(false);
                    }
                    activeSequence = firstSeq;
                    sequenceController.setActiveSequence(activeSequence);
                }
                
                drawingCoordinator.drawAll();
                
                statusBar.setStatus(String.format("Loaded %d sequence(s) successfully", loadedSequences.size()), 3000);
            } catch (IOException e) {
                statusBar.setStatus("Error loading sequences: " + e.getMessage(), 5000);
            }
        }
    }
    
    /**
     * Saves sequences to a file.
     */
    private void saveSequences() {
        // Sync sequences from panel
        allSequences = shapeSequencePanel.getSequences();
        
        if (allSequences.isEmpty()) {
            statusBar.setStatus("No sequences to save", 3000);
            return;
        }
        
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(com.trackdraw.config.FileManager.getSavesDirectory());
        fileChooser.setDialogTitle("Save Sequences");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("JSON Files", "json"));
        fileChooser.setSelectedFile(new File("sequences.json"));
        
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                String filePath = fileChooser.getSelectedFile().getAbsolutePath();
                if (!filePath.toLowerCase().endsWith(".json")) {
                    filePath += ".json";
                }
                
                // Get background image path and convert to relative path
                String bgImagePath = drawingPanel.getBackgroundImagePath();
                String relativeImagePath = null;
                if (bgImagePath != null && !bgImagePath.isEmpty()) {
                    relativeImagePath = com.trackdraw.config.FileManager.toRelativeImagePath(bgImagePath);
                }
                double bgImageScale = drawingPanel.getBackgroundImageScale();
                
                sequenceManager.saveSequences(allSequences, relativeImagePath, bgImageScale, filePath);
                
                statusBar.setStatus(String.format("Saved %d sequence(s) successfully", allSequences.size()), 3000);
            } catch (IOException e) {
                statusBar.setStatus("Error saving sequences: " + e.getMessage(), 5000);
            }
        }
    }
    
    /**
     * Sets up keyboard bindings for scale controls.
     * Global scale: Mouse wheel (handled in DrawingPanel)
     * Image scale: Page Up/Down (10%), Page Up/Down + Ctrl (1%)
     */
    private void setupScaleKeyboardBindings() {
        // Set keyboard target to drawing panel for image scale only
        backgroundImageScalePanel.getScaleControl().setKeyboardTarget(drawingPanel);
        
        // Image scale: Page Up/Down (10%), Page Up/Down + Ctrl (1%)
        backgroundImageScalePanel.getScaleControl().bindKeyboardKeys(KeyEvent.VK_PAGE_UP, false, 0.1);
        backgroundImageScalePanel.getScaleControl().bindKeyboardKeys(KeyEvent.VK_PAGE_DOWN, false, -0.1);
    }
    
    /**
     * Exports the canvas to a PNG image file.
     */
    private void exportImage() {
        ExportDialog dialog = new ExportDialog(this);
        dialog.setVisible(true);
        
        if (!dialog.isConfirmed()) {
            return;
        }
        
        // Show file chooser
        javax.swing.JFileChooser fileChooser = new javax.swing.JFileChooser();
        fileChooser.setDialogTitle("Export Image");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
            "PNG Images", "png"));
        
        // Set default directory to export folder
        fileChooser.setCurrentDirectory(com.trackdraw.config.FileManager.getExportDirectory());
        
        // Set default file name: use loaded JSON file name if available, otherwise use active sequence name
        String defaultName = "export";
        if (loadedJsonFileName != null && !loadedJsonFileName.isEmpty()) {
            defaultName = loadedJsonFileName;
        } else if (activeSequence != null && activeSequence.getName() != null && !activeSequence.getName().isEmpty()) {
            defaultName = activeSequence.getName();
        }
        fileChooser.setSelectedFile(new File(defaultName + ".png"));
        
        int result = fileChooser.showSaveDialog(this);
        if (result != javax.swing.JFileChooser.APPROVE_OPTION) {
            return;
        }
        
        File file = fileChooser.getSelectedFile();
        if (file == null) {
            return;
        }
        
        // Ensure .png extension
        if (!file.getName().toLowerCase().endsWith(".png")) {
            file = new File(file.getParent(), file.getName() + ".png");
        }
        
        try {
            // Get sequences and background image
            List<ShapeSequence> sequences = shapeSequencePanel.getSequences();
            BufferedImage backgroundImage = drawingPanel.getBackgroundImage();
            double backgroundImageScale = drawingPanel.getBackgroundImageScale();
            
            // Export
            ImageExporter.exportToPNG(
                sequences,
                backgroundImage,
                backgroundImageScale,
                dialog.isShowBackgroundImage(),
                dialog.isShowKey(),
                dialog.isShapesReport(),
                dialog.getScale(),
                file
            );
            
            statusBar.setStatus("Image exported successfully: " + file.getName(), 3000);
        } catch (Exception e) {
            statusBar.setStatus("Export failed: " + e.getMessage(), 5000);
            e.printStackTrace();
        }
    }
    
    /**
     * Generates and displays the shape usage report.
     */
    private void showShapeReport() {
        // Get all sequences
        List<ShapeSequence> allSequences = shapeSequencePanel.getSequences();
        
        // Generate report
        ShapeReportGenerator generator = new ShapeReportGenerator();
        ShapeReportGenerator.Report report = generator.generateReport(allSequences);
        
        // Display report in a dialog
        String reportText = report.formatReport();
        
        JTextArea textArea = new JTextArea(reportText);
        textArea.setEditable(false);
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 16));
        textArea.setBackground(Color.WHITE);
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(500, 400));
        
        JOptionPane.showMessageDialog(this, scrollPane, "Shape Usage Report", JOptionPane.INFORMATION_MESSAGE);
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
    
    // ========== Getters for Testing ==========
    
    /**
     * Gets the drawing panel (for testing purposes).
     * @return Drawing panel instance
     */
    public DrawingPanel getDrawingPanel() {
        return drawingPanel;
    }
    
    /**
     * Gets the scale control panel (for testing purposes).
     * @return Scale control panel instance
     */
    public ScaleControlPanel getScaleControlPanel() {
        return scaleControlPanel;
    }
    
    /**
     * Gets the measurement tool checkbox (for testing purposes).
     * @return Measurement tool checkbox
     */
    public JCheckBox getMeasureCheckBox() {
        return measureCheckBox;
    }
    
    /**
     * Gets the shape sequence panel (for testing purposes).
     * @return Shape sequence panel instance
     */
    public ShapeSequencePanel getShapeSequencePanel() {
        return shapeSequencePanel;
    }
    
    /**
     * Gets all sequences (for testing purposes).
     * @return List of all sequences
     */
    public List<ShapeSequence> getAllSequences() {
        return allSequences;
    }
    
    /**
     * Gets the keyboard controller (for testing purposes).
     * @return Keyboard controller instance
     */
    public AlignmentKeyboardController getKeyboardController() {
        return keyboardController;
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                
                // Set larger default fonts for UI components
                Font defaultFont = new Font(Font.SANS_SERIF, Font.PLAIN, 16);
                UIManager.put("Label.font", defaultFont);
                UIManager.put("Button.font", defaultFont);
                UIManager.put("TextField.font", defaultFont);
                UIManager.put("TextArea.font", defaultFont);
                UIManager.put("List.font", defaultFont);
                UIManager.put("CheckBox.font", defaultFont);
                UIManager.put("Menu.font", defaultFont);
                UIManager.put("MenuItem.font", defaultFont);
                UIManager.put("CheckBoxMenuItem.font", defaultFont);
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            MainWindow window = new MainWindow();
            window.setVisible(true);
        });
    }
}
