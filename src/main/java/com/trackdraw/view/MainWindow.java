package com.trackdraw.view;

import org.apache.commons.lang3.StringUtils;

import com.trackdraw.config.FileManager;
import com.trackdraw.config.SequenceManager;
import com.trackdraw.config.ShapeConfig;
import com.trackdraw.config.ShapeLibrary;
import com.trackdraw.model.ShapeSequence;
import com.trackdraw.report.ShapeReportGenerator;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
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
    
    // Status message timeout constants
    private static final int STATUS_SHORT = 3000;
    private static final int STATUS_LONG = 5000;
    
    public MainWindow() {
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        loadShapes();
        validateShapeLibrary();
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
        measureCheckBox.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 20));
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
        helpButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 20));
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
            "<b>Shape Management:</b><br>" +
            "Delete / Backspace: Remove active shape<br><br>" +
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
        clearButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 20));
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
                int activeShapeIndex = activeSequence.getActiveShapeIndex();
                if (activeShapeIndex >= 0) {
                    shapeListPanel.setSelectedIndex(activeShapeIndex);
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
        shapeSequencePanel.setStatusMessageHandler(message -> statusBar.setStatus(message, STATUS_SHORT));
        
        // Initialize sequences list with default "Main" sequence
        shapeSequencePanel.setSequences(allSequences);
        sequenceController.setActiveSequence(activeSequence);
        
        // Setup keyboard controls for adjusting alignment position
        keyboardController.setupKeyboardControls();
        
        // Setup Delete key to remove active shape
        setupDeleteKeyBinding();
        
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
    
    // ========== Utility Methods ==========
    
    /**
     * Shows a status message with default short timeout.
     * @param message Status message to display
     */
    private void showStatus(String message) {
        statusBar.setStatus(message, STATUS_SHORT);
    }
    
    /**
     * Shows an error status message with default long timeout.
     * @param message Error message to display
     */
    private void showError(String message) {
        statusBar.setStatus(message, STATUS_LONG);
    }
    
    /**
     * Creates and configures a file chooser dialog.
     * @param defaultDirectory Default directory to open
     * @param dialogTitle Title of the dialog
     * @param filter File filter (can be null)
     * @param defaultFile Default file to select (can be null)
     * @return Configured JFileChooser
     */
    private JFileChooser createFileChooser(File defaultDirectory, String dialogTitle, 
                                           FileNameExtensionFilter filter, File defaultFile) {
        JFileChooser fileChooser = new JFileChooser();
        if (defaultDirectory != null) {
            fileChooser.setCurrentDirectory(defaultDirectory);
        }
        fileChooser.setDialogTitle(dialogTitle);
        if (filter != null) {
            fileChooser.setFileFilter(filter);
        }
        if (defaultFile != null) {
            fileChooser.setSelectedFile(defaultFile);
        }
        return fileChooser;
    }
    
    /**
     * Shows a file chooser dialog and returns the selected file if approved, null otherwise.
     * @param fileChooser Configured file chooser
     * @param isSaveDialog true for save dialog, false for open dialog
     * @return Selected file if approved, null otherwise
     */
    private File showFileChooserAndGetFile(JFileChooser fileChooser, boolean isSaveDialog) {
        int result = isSaveDialog ? fileChooser.showSaveDialog(this) : fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile();
        }
        return null;
    }
    
    /**
     * Clears the background image and resets its scale.
     */
    private void clearBackgroundImageAndScale() {
        drawingPanel.setBackgroundImage(null);
        backgroundImageScalePanel.setScale(1.0);
    }
    
    /**
     * Loads a background image.
     * If image is outside the images directory, copies it to images directory.
     */
    private void loadBackgroundImage() {
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
            "Image Files", "jpg", "jpeg", "png", "gif", "bmp");
        JFileChooser fileChooser = createFileChooser(
            FileManager.getImagesDirectory(),
            "Load Background Image",
            filter,
            null
        );
        
        File selectedFile = showFileChooserAndGetFile(fileChooser, false);
        if (selectedFile != null) {
            try {
                File imagesDir = FileManager.getImagesDirectory();
                // If file is outside images directory, copy it; otherwise use it directly
                File imageFile = !selectedFile.getParentFile().equals(imagesDir) 
                    ? FileManager.copyImageToImagesDirectory(selectedFile)
                    : selectedFile;
                drawingPanel.setBackgroundImage(imageFile.getAbsolutePath());
                
                // Reset scale to 100% when loading new image
                backgroundImageScalePanel.setScale(1.0);
                showStatus("Background image loaded successfully");
            } catch (IOException e) {
                showError("Error copying image: " + e.getMessage());
            }
        }
    }
    
    /**
     * Clears the background image.
     */
    private void clearBackgroundImage() {
        clearBackgroundImageAndScale();
    }
    
    /**
     * Loads sequences from a file.
     */
    private void loadSequences() {
        FileNameExtensionFilter filter = new FileNameExtensionFilter("JSON Files", "json");
        JFileChooser fileChooser = createFileChooser(
            FileManager.getSavesDirectory(),
            "Load Sequences",
            filter,
            null
        );
        
        File selectedFile = showFileChooserAndGetFile(fileChooser, false);
        if (selectedFile != null) {
            try {
                SequenceManager.LoadResult loadResult = sequenceManager.loadSequences(selectedFile.getAbsolutePath());
                List<ShapeSequence> loadedSequences = loadResult.getSequences();
                
                if (loadedSequences.isEmpty()) {
                    showStatus("No sequences found in file");
                    return;
                }
                
                // Store the loaded JSON file name (without extension) for export
                loadedJsonFileName = FileManager.getBaseName(selectedFile);
                
                // Replace current sequences
                allSequences = loadedSequences;
                shapeSequencePanel.setSequences(allSequences);
                
                // Load background image if present (convert relative path to absolute)
                String bgImagePath = loadResult.getBackgroundImagePath();
                if (StringUtils.isNotEmpty(bgImagePath)) {
                    String absoluteImagePath = FileManager.toAbsoluteImagePath(bgImagePath);
                    if (absoluteImagePath != null) {
                        double scale = loadResult.getBackgroundImageScale();
                        drawingPanel.setBackgroundImage(absoluteImagePath);
                        drawingPanel.setBackgroundImageScale(scale);
                        backgroundImageScalePanel.setScale(scale);
                    } else {
                        clearBackgroundImageAndScale();
                        showError("Background image not found: " + bgImagePath);
                    }
                } else {
                    clearBackgroundImageAndScale();
                }
                
                // Set first sequence as active if available
                if (!allSequences.isEmpty()) {
                    ShapeSequence firstSeq = allSequences.get(0);
                    // Deactivate all sequences, then activate first
                    for (ShapeSequence seq : allSequences) {
                        seq.setActive(false);
                    }
                    firstSeq.setActive(true);
                    activeSequence = firstSeq;
                    sequenceController.setActiveSequence(activeSequence);
                }
                
                drawingCoordinator.drawAll();
                
                showStatus(String.format("Loaded %d sequence(s) successfully", loadedSequences.size()));
            } catch (IOException e) {
                showError("Error loading sequences: " + e.getMessage());
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
            showStatus("No sequences to save");
            return;
        }
        
        FileNameExtensionFilter filter = new FileNameExtensionFilter("JSON Files", "json");
        JFileChooser fileChooser = createFileChooser(
            FileManager.getSavesDirectory(),
            "Save Sequences",
            filter,
            new File("sequences.json")
        );
        
        File selectedFile = showFileChooserAndGetFile(fileChooser, true);
        if (selectedFile != null) {
            try {
                String filePath = selectedFile.getAbsolutePath();
                filePath = FileManager.ensureExtension(filePath, ".json");
                
                // Get background image path and convert to relative path
                String bgImagePath = drawingPanel.getBackgroundImagePath();
                String relativeImagePath = null;
                if (StringUtils.isNotEmpty(bgImagePath)) {
                    relativeImagePath = FileManager.toRelativeImagePath(bgImagePath);
                }
                double bgImageScale = drawingPanel.getBackgroundImageScale();
                
                sequenceManager.saveSequences(allSequences, relativeImagePath, bgImageScale, filePath);
                
                showStatus(String.format("Saved %d sequence(s) successfully", allSequences.size()));
            } catch (IOException e) {
                showError("Error saving sequences: " + e.getMessage());
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
     * Sets up keyboard binding for Delete key to remove active shape.
     */
    private void setupDeleteKeyBinding() {
        JRootPane rootPane = SwingUtilities.getRootPane(drawingPanel);
        InputMap inputMap = rootPane != null 
            ? rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            : drawingPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = rootPane != null 
            ? rootPane.getActionMap()
            : drawingPanel.getActionMap();
        
        // Delete key: remove active shape
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "removeShape");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), "removeShape"); // Also handle Backspace
        
        actionMap.put("removeShape", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sequenceController.removeSelectedOrLastShape();
            }
        });
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
        
        // Set default file name: use loaded JSON file name if available, otherwise use active sequence name
        String defaultName = "export";
        if (StringUtils.isNotEmpty(loadedJsonFileName)) {
            defaultName = loadedJsonFileName;
        } else if (activeSequence != null && StringUtils.isNotEmpty(activeSequence.getName())) {
            defaultName = activeSequence.getName();
        }
        
        FileNameExtensionFilter filter = new FileNameExtensionFilter("PNG Images", "png");
        JFileChooser fileChooser = createFileChooser(
            FileManager.getExportDirectory(),
            "Export Image",
            filter,
            new File(defaultName + ".png")
        );
        
        File file = showFileChooserAndGetFile(fileChooser, true);
        if (file == null) {
            return;
        }
        
        // Ensure .png extension
        String filePath = FileManager.ensureExtension(file.getAbsolutePath(), ".png");
        file = new File(filePath);
        
        try {
            // Export
            ImageExporter.exportToPNG(
                shapeSequencePanel.getSequences(),
                drawingPanel.getBackgroundImage(),
                drawingPanel.getBackgroundImageScale(),
                dialog.isShowBackgroundImage(),
                dialog.isShowKey(),
                dialog.isShapesReport(),
                dialog.getScale(),
                file
            );
            
            showStatus("Image exported successfully: " + file.getName());
        } catch (Exception e) {
            showError("Export failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Generates and displays the shape usage report.
     */
    private void showShapeReport() {
        // Generate report
        ShapeReportGenerator generator = new ShapeReportGenerator();
        ShapeReportGenerator.Report report = generator.generateReport(shapeSequencePanel.getSequences());
        
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
     * Validates the shape library at application startup.
     * Throws RuntimeException if the library cannot be loaded or is invalid.
     */
    private void validateShapeLibrary() {
        try {
            ShapeLibrary library = new ShapeLibrary();
            ShapeConfig config = new ShapeConfig();
            config.loadShapes();
            library.loadLibrary(config);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load shape library at application startup: " + e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Shape library validation failed at application startup: " + e.getMessage(), e);
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
    
    /**
     * Gets the sequence controller (for testing purposes).
     * @return Sequence controller instance
     */
    public ShapeSequenceController getSequenceController() {
        return sequenceController;
    }
    
    /**
     * Gets the shape list panel (for testing purposes).
     * @return Shape list panel instance
     */
    public ShapeListPanel getShapeListPanel() {
        return shapeListPanel;
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
