package com.trackdraw.view;

import com.trackdraw.config.GlobalScale;
import com.trackdraw.model.AlignPosition;
import com.trackdraw.model.ShapeInstance;
import com.trackdraw.model.ShapeSequence;
import com.trackdraw.report.ShapeReportGenerator;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Utility class for exporting the canvas to an image file.
 */
public class ImageExporter {
    
    /**
     * Exports the canvas to a PNG image file.
     * 
     * @param sequences List of sequences to export
     * @param backgroundImage Background image (can be null)
     * @param backgroundImageScale Background image scale factor
     * @param showBackgroundImage Whether to show background image
     * @param showKeys Whether to show shape keys
     * @param shapesReport Whether to include shapes report at bottom
     * @param exportScale Export scale multiplier
     * @param outputFile Output file to save to
     * @throws IOException If export fails
     */
    public static void exportToPNG(List<ShapeSequence> sequences, 
                                   BufferedImage backgroundImage,
                                   double backgroundImageScale,
                                   boolean showBackgroundImage,
                                   boolean showKeys,
                                   boolean shapesReport,
                                   int exportScale,
                                   File outputFile) throws IOException {
        
        // Save current GlobalScale and temporarily set it to exportScale
        double originalGlobalScale = GlobalScale.getScale();
        try {
            // Set GlobalScale to exportScale for all calculations
            GlobalScale.setScale(exportScale);
            
            // Calculate bounds of all sequences
            // Rule: min/max of align positions, add/subtract 20px
            BoundsResult boundsResult = calculateBounds(sequences, backgroundImage, 
                                                         backgroundImageScale, showBackgroundImage, exportScale);
            Rectangle2D.Double bounds = boundsResult.bounds;
            
            // Image dimensions: bounds width/height (already at exportScale)
            int imageWidth = (int)Math.ceil(bounds.getWidth());
            int imageHeight = (int)Math.ceil(bounds.getHeight());
            
            // Add space for shapes report if enabled
            int reportHeight = 0;
            if (shapesReport) {
                reportHeight = calculateReportHeight(sequences, exportScale);
                imageHeight += reportHeight;
            }
            
            // Create image
            BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = image.createGraphics();
            
            try {
                // Fill with white background
                g2d.setColor(Color.WHITE);
                g2d.fillRect(0, 0, imageWidth, imageHeight);
                
                // Enable anti-aliasing
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Translate to account for offset: move everything so it's positioned correctly
                // Bounds start at minX = originalMinX - padding (at exportScale)
                // We want content at originalMinX to be at padding pixels from the left edge
                // So we translate by -originalMinX + padding
                // But since bounds.getX() = minX = originalMinX - padding, we can use -bounds.getX()
                double offsetX = -bounds.getX();
                double offsetY = -bounds.getY();
                g2d.translate(offsetX, offsetY);
                
                // No need to scale - GlobalScale is already set to exportScale
                // Shapes will use GlobalScale.getScale() which is now exportScale
                
                // Draw background image if enabled
                if (showBackgroundImage && backgroundImage != null) {
                    double totalImageScale = GlobalScale.getScale() * backgroundImageScale;
                    int scaledWidth = (int)(backgroundImage.getWidth() * totalImageScale);
                    int scaledHeight = (int)(backgroundImage.getHeight() * totalImageScale);
                    g2d.drawImage(backgroundImage, 0, 0, scaledWidth, scaledHeight, null);
                }
                
                // Draw sequences with export colors (active schema, ignore active shape colors)
                for (ShapeSequence sequence : sequences) {
                    if (sequence.isEmpty()) {
                        continue;
                    }
                    
                    AlignPosition initialAlignPosition = getInitialAlignPosition(sequence);
                    if (initialAlignPosition == null) {
                        continue;
                    }
                    
                    // Draw sequence with export colors
                    drawSequenceForExport(g2d, sequence, initialAlignPosition, showKeys);
                }
                
                // Draw shapes report if enabled
                if (shapesReport) {
                    // Reset transform for report
                    g2d.setTransform(new java.awt.geom.AffineTransform());
                    
                    // Calculate report position (at bottom of image)
                    int reportY = imageHeight - reportHeight;
                    
                    // Draw report background
                    g2d.setColor(Color.WHITE);
                    g2d.fillRect(0, reportY, imageWidth, reportHeight);
                    
                    // Draw report text
                    drawShapesReport(g2d, sequences, 10 * exportScale, reportY + 10 * exportScale, 
                                    exportScale);
                }
                
            } finally {
                g2d.dispose();
            }
            
            // Save image
            ImageIO.write(image, "PNG", outputFile);
            
        } finally {
            // Always restore original GlobalScale
            GlobalScale.setScale(originalGlobalScale);
        }
    }
    
    /**
     * Result class for bounds calculation.
     * Contains bounds rectangle.
     */
    private static class BoundsResult {
        Rectangle2D.Double bounds;
        
        BoundsResult(Rectangle2D.Double bounds) {
            this.bounds = bounds;
        }
    }
    
    /**
     * Calculates the bounding box of all sequences and background image.
     * Rule: Calculate min/max x/y of all align positions and background image,
     * add 20px padding to sequences (not background image).
     * Returns both the bounds and the original min values for offset calculation.
     */
    private static BoundsResult calculateBounds(List<ShapeSequence> sequences,
                                                 BufferedImage backgroundImage,
                                                 double backgroundImageScale,
                                                 boolean includeBackgroundImage,
                                                 int exportScale) {
        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE;
        double maxY = Double.MIN_VALUE;
        
        // Include background image bounds if enabled (without padding)
        // GlobalScale is already set to exportScale at this point
        if (includeBackgroundImage && backgroundImage != null) {
            double totalImageScale = GlobalScale.getScale() * backgroundImageScale;
            double imageWidth = backgroundImage.getWidth() * totalImageScale;
            double imageHeight = backgroundImage.getHeight() * totalImageScale;
            
            // Background image is at (0,0) at current scale
            minX = Math.min(minX, 0);
            minY = Math.min(minY, 0);
            maxX = Math.max(maxX, imageWidth);
            maxY = Math.max(maxY, imageHeight);
        }
        
        // Collect all align positions from sequences
        BufferedImage tempImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        Graphics2D tempG2d = tempImage.createGraphics();
        tempG2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        try {
            for (ShapeSequence sequence : sequences) {
                if (sequence.isEmpty()) {
                    continue;
                }
                
                AlignPosition initialPos = getInitialAlignPosition(sequence);
                if (initialPos == null) {
                    continue;
                }
                
                // Traverse shapes to collect all align positions
                AlignPosition currentPos = initialPos;
                for (ShapeInstance shape : sequence.getShapes()) {
                    // Collect align position
                    double alignX = currentPos.getX();
                    double alignY = currentPos.getY();
                    
                    minX = Math.min(minX, alignX);
                    minY = Math.min(minY, alignY);
                    maxX = Math.max(maxX, alignX);
                    maxY = Math.max(maxY, alignY);
                    
                    // Calculate next position by drawing (public method)
                    shape.setAlignPosition(currentPos);
                    currentPos = shape.draw(tempG2d);
                }
            }
        } finally {
            tempG2d.dispose();
        }
        
        if (minX == Double.MAX_VALUE) {
            // No bounds found, use default
            return new BoundsResult(new Rectangle2D.Double(0, 0, 800, 600));
        }
        
        // Add 20px padding for sequences (not background image)
        // Padding extends bounds outward: subtract from min (makes it more negative), add to max
        double padding = 20.0 * GlobalScale.getScale();
        minX = minX - padding;  // Extend left (subtract padding to make more negative)
        minY = minY - padding;  // Extend up (subtract padding to make more negative)
        maxX = maxX + padding;  // Extend right (add padding)
        maxY = maxY + padding;  // Extend down (add padding)
        
        // Return bounds in unscaled coordinates
        // The image size will be calculated as (max - min) * exportScale
        return new BoundsResult(new Rectangle2D.Double(minX, minY, maxX - minX, maxY - minY));
    }
    
    /**
     * Gets the initial align position for a sequence.
     * GlobalScale is already set to exportScale at this point.
     */
    private static AlignPosition getInitialAlignPosition(ShapeSequence sequence) {
        if (sequence.getInitialAlignmentAsShape() != null) {
            return sequence.getEffectiveInitialAlignment();
        } else {
            AlignPosition storedPosition = sequence.getInitialAlignmentAsPosition();
            if (storedPosition != null) {
                // Stored position is relative to (0,0) at scale 1.0
                // Scale it by current GlobalScale (which is exportScale)
                double currentScale = GlobalScale.getScale();
                double scaledX = storedPosition.getX() * currentScale;
                double scaledY = storedPosition.getY() * currentScale;
                return new AlignPosition(scaledX, scaledY, storedPosition.getAngle());
            }
        }
        return null;
    }
    
    /**
     * Draws a sequence with export colors (active schema, ignore active shape colors).
     */
    private static void drawSequenceForExport(Graphics2D g2d, ShapeSequence sequence,
                                               AlignPosition initialAlignPosition,
                                               boolean showKeys) {
        if (sequence.isEmpty()) {
            return;
        }
        
        AlignPosition currentAlignPosition = initialAlignPosition;
        
        for (ShapeInstance shape : sequence.getShapes()) {
            // Calculate export colors (active schema for all sequences, ignore active shape colors)
            Color[] colors = calculateExportColors(shape);
            shape.setContourColor(colors[0]);
            shape.setInfillColor(colors[1]);
            
            // Set align position
            shape.setAlignPosition(currentAlignPosition);
            
            // Draw the shape
            currentAlignPosition = shape.draw(g2d);
            
            // Draw key if enabled
            if (showKeys) {
                shape.drawText(g2d, true);
            }
        }
    }
    
    /**
     * Calculates colors for export (active schema for all sequences, ignore active shape colors).
     */
    private static Color[] calculateExportColors(ShapeInstance shape) {
        boolean isRed = shape.getEffectiveIsRed();
        
        Color contourColor;
        Color infillColor;
        
        // Always use active schema colors (ignore sequence active state)
        if (isRed) {
            // Red base color: bright red (active schema)
            contourColor = Color.RED;
            infillColor = Color.RED;
        } else {
            // White/black base color: dark gray contour, white infill (active schema)
            contourColor = new Color(64, 64, 64); // Dark gray
            infillColor = Color.WHITE;
        }
        
        return new Color[]{contourColor, infillColor};
    }
    
    /**
     * Calculates the height needed for the shapes report based on actual shape types count.
     * @param sequences List of sequences to analyze
     * @param scale Export scale multiplier
     * @return Height in pixels needed for the report
     */
    private static int calculateReportHeight(List<ShapeSequence> sequences, int scale) {
        // Use ShapeReportGenerator to get proper counts including complex shapes
        ShapeReportGenerator generator = new ShapeReportGenerator();
        ShapeReportGenerator.Report report = generator.generateReport(sequences);
        
        // Build combined counts from regular shapes
        Map<String, Integer> redCounts = new HashMap<>(report.getRegularShapesRed());
        Map<String, Integer> whiteCounts = new HashMap<>(report.getRegularShapesWhite());
        
        // Add complex shapes to the counts
        for (Map.Entry<ShapeReportGenerator.ComplexShape, Integer> entry : report.getComplexShapes().entrySet()) {
            ShapeReportGenerator.ComplexShape complex = entry.getKey();
            int count = entry.getValue();
            
            // Format complex shape as "key1+key2" for display
            String complexKey = complex.getShapeKey1() + "+" + complex.getShapeKey2();
            
            if (complex.isRed()) {
                redCounts.put(complexKey, redCounts.getOrDefault(complexKey, 0) + count);
            } else {
                whiteCounts.put(complexKey, whiteCounts.getOrDefault(complexKey, 0) + count);
            }
        }
        
        // Count unique keys (rows that will be displayed)
        Set<String> allKeys = new TreeSet<>();
        allKeys.addAll(redCounts.keySet());
        allKeys.addAll(whiteCounts.keySet());
        
        // Count non-empty rows
        int dataRowCount = 0;
        for (String key : allKeys) {
            int redCount = redCounts.getOrDefault(key, 0);
            int whiteCount = whiteCounts.getOrDefault(key, 0);
            if (redCount > 0 || whiteCount > 0) {
                dataRowCount++;
            }
        }
        
        // Calculate height based on layout:
        // - Top padding: 10 * scale
        // - Title: 1 line
        // - Spacing after title: 2 * lineHeight
        // - Header: 1 line
        // - Separator: 1 line
        // - Data rows: dataRowCount lines
        // - Bottom padding: 10 * scale
        int lineHeight = 15 * scale;
        int topPadding = 10 * scale;
        int bottomPadding = 10 * scale;
        int titleHeight = lineHeight;
        int spacingAfterTitle = lineHeight * 2;
        int headerHeight = lineHeight;
        int separatorHeight = lineHeight;
        int dataRowsHeight = dataRowCount * lineHeight;
        
        int totalHeight = topPadding + titleHeight + spacingAfterTitle + headerHeight + 
                         separatorHeight + dataRowsHeight + bottomPadding;
        
        return totalHeight;
    }
    
    /**
     * Draws the shapes report at the bottom of the image.
     * Uses ShapeReportGenerator to properly count complex shapes.
     * Format: table with rows for shape keys, columns for red/white, cells show counts.
     */
    private static void drawShapesReport(Graphics2D g2d, List<ShapeSequence> sequences,
                                         int x, int y, int scale) {
        // Use ShapeReportGenerator to get proper counts including complex shapes
        ShapeReportGenerator generator = new ShapeReportGenerator();
        ShapeReportGenerator.Report report = generator.generateReport(sequences);
        
        // Build combined counts from regular shapes
        Map<String, Integer> redCounts = new HashMap<>(report.getRegularShapesRed());
        Map<String, Integer> whiteCounts = new HashMap<>(report.getRegularShapesWhite());
        
        // Add complex shapes to the counts
        // Complex shapes are counted as regular shapes in the table format
        for (Map.Entry<ShapeReportGenerator.ComplexShape, Integer> entry : report.getComplexShapes().entrySet()) {
            ShapeReportGenerator.ComplexShape complex = entry.getKey();
            int count = entry.getValue();
            
            // Format complex shape as "key1+key2" for display
            String complexKey = complex.getShapeKey1() + "+" + complex.getShapeKey2();
            
            if (complex.isRed()) {
                redCounts.put(complexKey, redCounts.getOrDefault(complexKey, 0) + count);
            } else {
                whiteCounts.put(complexKey, whiteCounts.getOrDefault(complexKey, 0) + count);
            }
        }
        
        // Collect all unique keys (sorted)
        Set<String> allKeys = new TreeSet<>();
        allKeys.addAll(redCounts.keySet());
        allKeys.addAll(whiteCounts.keySet());
        
        // Draw report text
        g2d.setColor(Color.BLACK);
        Font reportFont = new Font(Font.MONOSPACED, Font.PLAIN, 10 * scale);
        g2d.setFont(reportFont);
        
        int lineHeight = 15 * scale;
        int currentY = y;
        
        // Calculate column widths
        int keyColWidth = 80 * scale; // Wider for complex shapes
        int redColWidth = 40 * scale;
        int whiteColWidth = 40 * scale;
        
        // Title
        g2d.drawString("Shapes Report:", x, currentY);
        currentY += lineHeight * 2;
        
        // Table header
        int headerX = x;
        g2d.drawString("Key", headerX, currentY);
        headerX += keyColWidth;
        g2d.drawString("Red", headerX, currentY);
        headerX += redColWidth;
        g2d.drawString("White", headerX, currentY);
        currentY += lineHeight;
        
        // Draw separator line
        g2d.drawLine(x, currentY, x + keyColWidth + redColWidth + whiteColWidth, currentY);
        currentY += lineHeight;
        
        // Table rows
        for (String key : allKeys) {
            int redCount = redCounts.getOrDefault(key, 0);
            int whiteCount = whiteCounts.getOrDefault(key, 0);
            
            // Skip rows with zero counts in both columns
            if (redCount == 0 && whiteCount == 0) {
                continue;
            }
            
            int rowX = x;
            g2d.drawString(key, rowX, currentY);
            rowX += keyColWidth;
            g2d.drawString(String.valueOf(redCount), rowX, currentY);
            rowX += redColWidth;
            g2d.drawString(String.valueOf(whiteCount), rowX, currentY);
            currentY += lineHeight;
        }
    }
}

