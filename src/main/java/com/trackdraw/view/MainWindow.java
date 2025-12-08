package com.trackdraw.view;

import com.trackdraw.config.SequenceManager;
import com.trackdraw.config.ShapeConfig;
import com.trackdraw.model.ShapeSequence;
import com.trackdraw.report.ShapeReportGenerator;

import javax.swing.*;
import java.awt.*;
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
    private ShapePalettePanel shapePalettePanel;
    private ShapeSequencePanel shapeSequencePanel;
    private ShapeListPanel shapeListPanel;
    
    // Controllers
    private ShapeConfig shapeConfig;
    private SequenceManager sequenceManager;
    private ShapeSequenceController sequenceController;
    private DrawingCoordinator drawingCoordinator;
    private AlignmentKeyboardController keyboardController;
    
    // State
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
        
        // Initialize UI components
        drawingPanel = new DrawingPanel();
        shapeConfig = new ShapeConfig();
        sequenceManager = new SequenceManager();
        scaleControlPanel = new ScaleControlPanel();
        shapePalettePanel = new ShapePalettePanel(shapeConfig);
        shapeSequencePanel = new ShapeSequencePanel();
        shapeListPanel = new ShapeListPanel();
        
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
        JButton clearButton = new JButton("⌧");
        Font currentFont = clearButton.getFont();
        Font largerFont = new Font(currentFont.getName(), currentFont.getStyle(), currentFont.getSize() + 4);
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
        
        shapePalettePanel.setShapeSelectionHandler((key, orientation) -> {
            sequenceController.addShapeToSequence(key, orientation);
        });
        
        // Set remove handler for the palette panel
        shapePalettePanel.setRemoveHandler(() -> sequenceController.removeSelectedOrLastShape());
        
        // Set invert color handler for the palette panel
        shapePalettePanel.setInvertColorHandler(() -> sequenceController.toggleInvertColor());
        
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
     * Loads sequences from a file.
     */
    private void loadSequences() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Load Sequences");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("JSON Files", "json"));
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                List<ShapeSequence> loadedSequences = sequenceManager.loadSequences(fileChooser.getSelectedFile().getAbsolutePath());
                
                if (loadedSequences.isEmpty()) {
                    JOptionPane.showMessageDialog(this,
                        "No sequences found in file.",
                        "Load Sequences",
                        JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
                
                // Replace current sequences
                allSequences = loadedSequences;
                shapeSequencePanel.setSequences(allSequences);
                
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
                
                JOptionPane.showMessageDialog(this,
                    String.format("Loaded %d sequence(s) successfully.", loadedSequences.size()),
                    "Load Sequences",
                    JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this,
                    "Error loading sequences: " + e.getMessage(),
                    "Load Error",
                    JOptionPane.ERROR_MESSAGE);
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
            JOptionPane.showMessageDialog(this,
                "No sequences to save.",
                "Save Sequences",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Sequences");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("JSON Files", "json"));
        fileChooser.setSelectedFile(new java.io.File("sequences.json"));
        
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                String filePath = fileChooser.getSelectedFile().getAbsolutePath();
                if (!filePath.toLowerCase().endsWith(".json")) {
                    filePath += ".json";
                }
                
                sequenceManager.saveSequences(allSequences, filePath);
                
                JOptionPane.showMessageDialog(this,
                    String.format("Saved %d sequence(s) successfully.", allSequences.size()),
                    "Save Sequences",
                    JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this,
                    "Error saving sequences: " + e.getMessage(),
                    "Save Error",
                    JOptionPane.ERROR_MESSAGE);
            }
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
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
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
