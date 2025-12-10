package com.trackdraw.view;

import com.trackdraw.config.GlobalScale;
import com.trackdraw.model.AlignPosition;
import com.trackdraw.model.ShapeSequence;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

/**
 * Panel for drawing geometric shapes.
 */
public class DrawingPanel extends JPanel {
    private List<ShapeSequence> sequences = new ArrayList<>();
    private BufferedImage backgroundImage;
    private String backgroundImagePath;
    private double backgroundImageScale = 1.0; // Default scale
    private MeasurementTool measurementTool = new MeasurementTool();
    private boolean showKeys = false; // Whether to show shape keys
    private java.util.function.Consumer<String> statusMessageHandler; // Handler to set status messages
    private java.util.function.Consumer<Double> globalScaleChangeHandler; // Handler to notify when global scale changes
    
    // Pan offset for canvas dragging
    private double panX = 0.0;
    private double panY = 0.0;
    private int lastMouseX = 0;
    private int lastMouseY = 0;
    private boolean isPanning = false;
    
    public DrawingPanel() {
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(800, 600));
        setFocusable(true);
        
        // Request focus when clicked so keyboard controls work
        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                requestFocusInWindow();
                
                // Start panning if left mouse button and measurement tool is not active
                if (e.getButton() == java.awt.event.MouseEvent.BUTTON1 && !measurementTool.isActive()) {
                    isPanning = true;
                    lastMouseX = e.getX();
                    lastMouseY = e.getY();
                }
                
                // Handle measurement tool clicks (account for pan offset)
                if (measurementTool.isActive()) {
                    // Adjust click coordinates for pan offset
                    double adjustedX = e.getX() - panX;
                    double adjustedY = e.getY() - panY;
                    measurementTool.handleClick((int)adjustedX, (int)adjustedY);
                    repaint();
                }
            }
            
            @Override
            public void mouseReleased(java.awt.event.MouseEvent e) {
                // Stop panning
                if (e.getButton() == java.awt.event.MouseEvent.BUTTON1) {
                    isPanning = false;
                }
            }
            
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                // Only handle click if not panning (to avoid triggering click after drag)
                if (!isPanning && measurementTool.isActive()) {
                    // Adjust click coordinates for pan offset
                    double adjustedX = e.getX() - panX;
                    double adjustedY = e.getY() - panY;
                    measurementTool.handleClick((int)adjustedX, (int)adjustedY);
                    repaint();
                }
            }
        });
        
        // Setup keyboard bindings for Esc key to disable measurement tool
        setupMeasurementKeyboardControls();
        
        // Setup mouse wheel for global scale
        setupMouseWheelForGlobalScale();
        
        // Handle mouse movement for measurement preview and panning
        addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseDragged(java.awt.event.MouseEvent e) {
                if (isPanning) {
                    // Calculate pan delta
                    int deltaX = e.getX() - lastMouseX;
                    int deltaY = e.getY() - lastMouseY;
                    
                    // Update pan offset
                    panX += deltaX;
                    panY += deltaY;
                    
                    // Update last mouse position
                    lastMouseX = e.getX();
                    lastMouseY = e.getY();
                    
                    repaint();
                } else if (measurementTool.isActive() && measurementTool.isMeasuring()) {
                    // Adjust mouse move coordinates for pan offset
                    double adjustedX = e.getX() - panX;
                    double adjustedY = e.getY() - panY;
                    measurementTool.handleMouseMove((int)adjustedX, (int)adjustedY);
                    repaint();
                }
            }
            
            @Override
            public void mouseMoved(java.awt.event.MouseEvent e) {
                if (measurementTool.isActive() && measurementTool.isMeasuring()) {
                    measurementTool.handleMouseMove(e.getX(), e.getY());
                    repaint();
                }
            }
        });
    }
    
    /**
     * Sets the list of sequences to draw.
     * @param sequences List of ShapeSequence objects
     */
    public void setSequences(List<ShapeSequence> sequences) {
        this.sequences = sequences != null ? new ArrayList<>(sequences) : new ArrayList<>();
        repaint();
    }
    
    /**
     * Gets the list of sequences.
     * @return List of ShapeSequence objects
     */
    public List<ShapeSequence> getSequences() {
        return new ArrayList<>(sequences);
    }
    
    /**
     * Draws all shapes in the sequence following the workflow:
     * 3.3) Canvas cleared
     * 3.4) Start drawing all shapes from the list by sequence
     * 3.5) For the first shape, set default AlignPosition (center of canvas, angle 0)
     * 3.6) Draw the first shape, set the returned AlignPosition to the next shape
     * 3.7) Loop till the end of the shape list
     */
    public void drawAll() {
        repaint();
    }
    
    /**
     * Sets the background image.
     * @param imagePath Path to the image file
     */
    public void setBackgroundImage(String imagePath) {
        this.backgroundImagePath = imagePath;
        if (imagePath != null && !imagePath.isEmpty()) {
            try {
                File imageFile = new File(imagePath);
                if (imageFile.exists()) {
                    this.backgroundImage = ImageIO.read(imageFile);
                    repaint();
                    if (statusMessageHandler != null) {
                        statusMessageHandler.accept("Background image loaded");
                    }
                } else {
                    this.backgroundImage = null;
                    if (statusMessageHandler != null) {
                        statusMessageHandler.accept("Image file not found: " + imagePath);
                    }
                }
            } catch (IOException e) {
                this.backgroundImage = null;
                if (statusMessageHandler != null) {
                    statusMessageHandler.accept("Error loading image: " + e.getMessage());
                }
            }
        } else {
            this.backgroundImage = null;
            repaint();
        }
    }
    
    /**
     * Gets the background image path.
     * @return Path to the background image, or null if not set
     */
    public String getBackgroundImagePath() {
        return backgroundImagePath;
    }
    
    /**
     * Sets the background image scale.
     * @param scale Scale factor (1.0 = 100%)
     */
    public void setBackgroundImageScale(double scale) {
        this.backgroundImageScale = scale;
        repaint();
    }
    
    /**
     * Gets the background image scale.
     * @return Scale factor
     */
    public double getBackgroundImageScale() {
        return backgroundImageScale;
    }
    
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2d = (Graphics2D) g;
        
        // Enable anti-aliasing for smoother shapes
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Apply pan offset (translate the entire canvas)
        g2d.translate(panX, panY);
        
        // Use (0,0) as reference point for all scaling
        // This ensures image and sequences maintain relative positions on scale change and window resize
        double currentScale = GlobalScale.getScale();
        
        // Draw background image first
        // Image position is stored relative to (0,0) at scale 1.0
        if (backgroundImage != null) {
            double totalImageScale = GlobalScale.getScale() * backgroundImageScale;
            
            // Image position at scale 1.0 (stored as relative to 0,0)
            // For now, image is always at (0,0) at scale 1.0
            double imageXAtScale1 = 0.0;
            double imageYAtScale1 = 0.0;
            
            // Scale position from (0,0)
            double scaledX = imageXAtScale1 * currentScale;
            double scaledY = imageYAtScale1 * currentScale;
            
            int scaledWidth = (int)(backgroundImage.getWidth() * totalImageScale);
            int scaledHeight = (int)(backgroundImage.getHeight() * totalImageScale);
            g2d.drawImage(backgroundImage, (int)scaledX, (int)scaledY, scaledWidth, scaledHeight, null);
        }
        
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2.0f));
        
        // 3.3) Canvas cleared (already done by super.paint())
        
        // Draw all sequences (not just active ones)
        
        for (ShapeSequence sequence : sequences) {
            if (sequence.isEmpty()) {
                continue;
            }
            
            // Determine initial alignment position
            AlignPosition initialAlignPosition = null;
            
            // Check if sequence is linked to a shape (linked sequences don't need scaling)
            if (sequence.getInitialAlignmentAsShape() != null) {
                // Linked sequence - use effective alignment directly (already calculated correctly)
                initialAlignPosition = sequence.getEffectiveInitialAlignment();
            } else {
                // Independent sequence - scale the stored position
                AlignPosition storedPosition = sequence.getInitialAlignmentAsPosition();
                
                if (storedPosition != null) {
                    // Position is stored relative to (0,0) at scale 1.0
                    // Scale it by current scale from (0,0)
                    double scaledX = storedPosition.getX() * currentScale;
                    double scaledY = storedPosition.getY() * currentScale;
                    
                    initialAlignPosition = new AlignPosition(scaledX, scaledY, storedPosition.getAngle());
                } else {
                    // No stored position - use center of canvas and store it relative to (0,0)
                    double canvasCenterX = getWidth() / 2.0;
                    double canvasCenterY = getHeight() / 2.0;
                    
                    // Store position relative to (0,0) at scale 1.0
                    double normalizedX = canvasCenterX / currentScale;
                    double normalizedY = canvasCenterY / currentScale;
                    AlignPosition newPosition = new AlignPosition(normalizedX, normalizedY, 0.0);
                    sequence.setInitialAlignment(newPosition);
                    
                    // Use the stored position for drawing
                    initialAlignPosition = new AlignPosition(canvasCenterX, canvasCenterY, 0.0);
                }
            }

            if (initialAlignPosition == null) {
                // Fallback: use center of canvas
                double canvasCenterX = getWidth() / 2.0;
                double canvasCenterY = getHeight() / 2.0;
                initialAlignPosition = new AlignPosition(canvasCenterX, canvasCenterY, 0.0);
            }
            
            // Draw the sequence
            sequence.drawAll(g2d, initialAlignPosition, showKeys);
        }
        
        // Reset transform before drawing measurement tool (measurement is in screen coordinates)
        g2d.setTransform(new java.awt.geom.AffineTransform());
        
        // Draw measurement tool if active
        if (measurementTool.isActive()) {
            Point2D.Double start = measurementTool.getStartPoint();
            Point2D.Double end = measurementTool.getEndPoint();
            
            if (start != null && end != null) {
                // Draw measurement line
                g2d.setColor(Color.BLUE);
                g2d.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{5, 5}, 0));
                g2d.draw(new Line2D.Double(start.x, start.y, end.x, end.y));
                
                // Draw start and end points
                g2d.setStroke(new BasicStroke(1.0f));
                int pointSize = 5;
                g2d.fillOval((int)(start.x - pointSize/2), (int)(start.y - pointSize/2), pointSize, pointSize);
                g2d.fillOval((int)(end.x - pointSize/2), (int)(end.y - pointSize/2), pointSize, pointSize);
                
                // Draw distance label (show only scaled units, just the number)
                if (measurementTool.hasMeasurement() || measurementTool.isMeasuring()) {
                    double distanceScaled = measurementTool.getDistanceScaled();
                    String label = String.format("%.1f", distanceScaled);
                    
                    // Position label at midpoint of line
                    double midX = (start.x + end.x) / 2;
                    double midY = (start.y + end.y) / 2;
                    
                    // Draw background for text
                    FontMetrics fm = g2d.getFontMetrics();
                    int labelWidth = fm.stringWidth(label);
                    int labelHeight = fm.getHeight();
                    g2d.setColor(new Color(255, 255, 255, 200)); // Semi-transparent white
                    g2d.fillRect((int)(midX - labelWidth/2 - 2), (int)(midY - labelHeight/2 - 2), 
                                labelWidth + 4, labelHeight + 4);
                    
                    // Draw text
                    g2d.setColor(Color.BLUE);
                    g2d.drawString(label, (int)(midX - labelWidth/2), (int)(midY + labelHeight/4));
                }
            }
        }
    }
    
    /**
     * Gets the measurement tool.
     * @return MeasurementTool instance
     */
    public MeasurementTool getMeasurementTool() {
        return measurementTool;
    }
    
    /**
     * Sets a callback to be called when measurement tool is deactivated.
     * Used to sync UI checkbox state.
     */
    private Runnable measurementDeactivatedCallback;
    
    /**
     * Sets the callback for when measurement tool is deactivated.
     * @param callback Callback to run when tool is deactivated
     */
    public void setMeasurementDeactivatedCallback(Runnable callback) {
        this.measurementDeactivatedCallback = callback;
    }
    
    /**
     * Sets the status message handler.
     * @param handler Consumer that receives status message strings
     */
    public void setStatusMessageHandler(java.util.function.Consumer<String> handler) {
        this.statusMessageHandler = handler;
    }
    
    /**
     * Sets the handler to be called when global scale changes via mouse wheel.
     * @param handler Handler that receives the new scale value
     */
    public void setGlobalScaleChangeHandler(java.util.function.Consumer<Double> handler) {
        this.globalScaleChangeHandler = handler;
    }
    
    /**
     * Sets whether to show shape keys on the canvas.
     * @param showKeys true to show keys, false to hide them
     */
    public void setShowKeys(boolean showKeys) {
        this.showKeys = showKeys;
    }
    
    /**
     * Gets whether shape keys are shown on the canvas.
     * @return true if keys are shown, false otherwise
     */
    public boolean isShowKeys() {
        return showKeys;
    }
    
    /**
     * Gets the background image.
     * @return Background image, or null if not set
     */
    public BufferedImage getBackgroundImage() {
        return backgroundImage;
    }
    
    /**
     * Sets up keyboard controls for the measurement tool.
     * Esc key disables the measurement tool.
     */
    private void setupMeasurementKeyboardControls() {
        InputMap inputMap = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = getActionMap();
        
        inputMap.put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0), "disableMeasurement");
        actionMap.put("disableMeasurement", new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (measurementTool.isActive()) {
                    measurementTool.setActive(false);
                    repaint();
                    // Notify callback to update UI checkbox
                    if (measurementDeactivatedCallback != null) {
                        measurementDeactivatedCallback.run();
                    }
                }
            }
        });
    }
    
    /**
     * Sets up mouse wheel for global scale adjustment.
     * Mouse wheel up increases scale, mouse wheel down decreases scale.
     */
    private void setupMouseWheelForGlobalScale() {
        addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            @Override
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent e) {
                // Only handle if measurement tool is not active
                if (measurementTool.isActive()) {
                    return;
                }
                
                int rotation = e.getWheelRotation();
                double delta;
                
                // Check if Ctrl is pressed for 1% increments, otherwise 10%
                boolean ctrlPressed = (e.getModifiersEx() & java.awt.event.InputEvent.CTRL_DOWN_MASK) != 0;
                if (ctrlPressed) {
                    delta = rotation > 0 ? -0.01 : 0.01; // 1% per notch
                } else {
                    delta = rotation > 0 ? -0.1 : 0.1; // 10% per notch
                }
                
                // Change global scale
                double currentScale = GlobalScale.getScale();
                double newScale = currentScale + delta;
                
                // Normalize scale to clean increments to avoid floating point precision issues
                // If delta is 0.1 (10%), round to nearest 0.1; if delta is 0.01 (1%), round to nearest 0.01
                if (Math.abs(delta) >= 0.05) {
                    // 10% change: round to nearest 0.1
                    newScale = Math.round(newScale * 10.0) / 10.0;
                } else {
                    // 1% change: round to nearest 0.01
                    newScale = Math.round(newScale * 100.0) / 100.0;
                }
                
                // Clamp to reasonable range
                if (newScale < 0.1) newScale = 0.1;
                if (newScale > 10.0) newScale = 10.0;
                
                GlobalScale.setScale(newScale);
                
                // Notify handler if set (to update UI controls)
                if (globalScaleChangeHandler != null) {
                    globalScaleChangeHandler.accept(newScale);
                }
                
                repaint();
            }
        });
    }
}

