package com.trackdraw.report;

import com.trackdraw.config.GlobalScale;
import com.trackdraw.model.ShapeInstance;
import com.trackdraw.model.ShapeSequence;
import com.trackdraw.util.SequenceBoundsCalculator;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.awt.geom.Rectangle2D;
import java.util.*;

/**
 * Generates reports about shape usage across all sequences.
 * Counts shapes by type and color, handling complex shapes from linked sequences.
 */
public class ShapeReportGenerator {
    
    /**
     * Represents a shape count entry in the report.
     */
    @Data
    @AllArgsConstructor
    public static class ShapeCount {
        private String shapeKey;
        private boolean isRed;
        private int count;
        
        public void increment() {
            this.count++;
        }
    }
    
    /**
     * Represents a complex shape (linked shape + first shape of linked sequence).
     * Includes shape keys, orientations, and color.
     * Order doesn't matter - pairs are normalized so (A, o1) + (B, o2) equals (B, o2) + (A, o1).
     */
    @Data
    public static class ComplexShape {
        private String shapeKey1;
        private int orientation1;
        private String shapeKey2;
        private int orientation2;
        private boolean isRed;
        
        public ComplexShape(String parentShapeKey, int parentOrientation, 
                           String childShapeKey, int childOrientation, boolean isRed) {
            // Normalize order: sort by shape key first, then by orientation if keys are equal
            // This ensures (A, o1) + (B, o2) equals (B, o2) + (A, o1)
            int keyCompare = parentShapeKey.compareTo(childShapeKey);
            if (keyCompare < 0) {
                // parentShapeKey < childShapeKey: keep order
                this.shapeKey1 = parentShapeKey;
                this.orientation1 = parentOrientation;
                this.shapeKey2 = childShapeKey;
                this.orientation2 = childOrientation;
            } else if (keyCompare > 0) {
                // parentShapeKey > childShapeKey: swap
                this.shapeKey1 = childShapeKey;
                this.orientation1 = childOrientation;
                this.shapeKey2 = parentShapeKey;
                this.orientation2 = parentOrientation;
            } else {
                // Same shape key: sort by orientation
                if (parentOrientation <= childOrientation) {
                    this.shapeKey1 = parentShapeKey;
                    this.orientation1 = parentOrientation;
                    this.shapeKey2 = childShapeKey;
                    this.orientation2 = childOrientation;
                } else {
                    this.shapeKey1 = childShapeKey;
                    this.orientation1 = childOrientation;
                    this.shapeKey2 = parentShapeKey;
                    this.orientation2 = parentOrientation;
                }
            }
            this.isRed = isRed;
        }
        
        @Override
        public String toString() {
            return shapeKey1 + "(" + (orientation1 == 1 ? "+" : "-") + ") + " +
                   shapeKey2 + "(" + (orientation2 == 1 ? "+" : "-") + ")" +
                   (isRed ? " (red)" : " (white)");
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ComplexShape that = (ComplexShape) o;
            return orientation1 == that.orientation1 &&
                   orientation2 == that.orientation2 &&
                   isRed == that.isRed &&
                   Objects.equals(shapeKey1, that.shapeKey1) &&
                   Objects.equals(shapeKey2, that.shapeKey2);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(shapeKey1, orientation1, shapeKey2, orientation2, isRed);
        }
    }
    
    /**
     * Generates a report of shape usage across all sequences.
     * 
     * @param sequences List of all sequences to analyze
     * @return Report containing shape counts and complex shapes
     */
    public Report generateReport(List<ShapeSequence> sequences) {
        // Map to count regular shapes: (shapeKey, isRed) -> count
        Map<String, Integer> regularShapesWhite = new HashMap<>();
        Map<String, Integer> regularShapesRed = new HashMap<>();
        
        // Map to count complex shapes: ComplexShape -> count
        Map<ComplexShape, Integer> complexShapes = new HashMap<>();
        
        // Set to track which shapes are part of complex shapes (to exclude from regular counts)
        Set<ShapeInstance> excludedShapes = new HashSet<>();
        
        // First pass: identify complex shapes and mark shapes to exclude
        // Build a map to find which sequence contains each shape and its index
        Map<ShapeInstance, ShapeSequence> shapeToSequenceMap = new HashMap<>();
        Map<ShapeInstance, Integer> shapeToIndexMap = new HashMap<>();
        for (ShapeSequence seq : sequences) {
            List<ShapeInstance> shapes = seq.getShapes();
            for (int i = 0; i < shapes.size(); i++) {
                ShapeInstance shape = shapes.get(i);
                shapeToSequenceMap.put(shape, seq);
                shapeToIndexMap.put(shape, i);
            }
        }
        
        for (ShapeSequence sequence : sequences) {
            if (sequence.isEmpty()) {
                continue;
            }
            
            // Check if this sequence is linked to a shape
            ShapeInstance linkedShape = sequence.getInitialAlignmentAsShape();
            if (linkedShape != null && !sequence.isEmpty()) {
                // Complex shapes only apply if invertAlignment is false
                if (!sequence.isInvertAlignment()) {
                    // This is a linked sequence with normal alignment
                    // Complex shape = linked shape + first shape of linked sequence
                    ShapeInstance firstShapeOfLinked = sequence.getShape(0);
                    
                    if (firstShapeOfLinked != null) {
                        // The linked shape + first shape of linked sequence form a complex shape
                        // Determine color: use the first shape's color (which is opposite to linked shape)
                        boolean isRed = firstShapeOfLinked.isRed();
                        
                        ComplexShape complexShape = new ComplexShape(
                            linkedShape.getKey(),
                            linkedShape.getOrientation(),
                            firstShapeOfLinked.getKey(),
                            firstShapeOfLinked.getOrientation(),
                            isRed
                        );
                        
                        complexShapes.put(complexShape, complexShapes.getOrDefault(complexShape, 0) + 1);
                        
                        // Mark these specific shape instances as excluded from regular counts
                        excludedShapes.add(linkedShape);
                        excludedShapes.add(firstShapeOfLinked);
                    }
                }
                // If invertAlignment is true, shapes are counted normally (not as complex shapes)
            }
        }
        
        // Second pass: count regular shapes (excluding those in complex shapes)
        for (ShapeSequence sequence : sequences) {
            if (sequence.isEmpty()) {
                continue;
            }
            
            for (int i = 0; i < sequence.size(); i++) {
                ShapeInstance shape = sequence.getShape(i);
                if (shape == null) {
                    continue;
                }
                
                // Skip if this shape is part of a complex shape
                if (excludedShapes.contains(shape)) {
                    continue;
                }
                
                // Count the shape
                String shapeKey = shape.getKey();
                boolean isRed = shape.isRed();
                
                if (isRed) {
                    regularShapesRed.put(shapeKey, regularShapesRed.getOrDefault(shapeKey, 0) + 1);
                } else {
                    regularShapesWhite.put(shapeKey, regularShapesWhite.getOrDefault(shapeKey, 0) + 1);
                }
            }
        }
        
        // Calculate sequence bounds (width and length) at current scale, then normalize to scale 1.0
        Rectangle2D.Double bounds = SequenceBoundsCalculator.calculateSequenceBounds(sequences);
        double currentScale = GlobalScale.getScale();
        double width = bounds != null ? bounds.getWidth() / currentScale : 0.0;
        double length = bounds != null ? bounds.getHeight() / currentScale : 0.0;
        
        return new Report(regularShapesWhite, regularShapesRed, complexShapes, width, length);
    }
    
    /**
     * Represents the complete report.
     */
    @Data
    public static class Report {
        private final Map<String, Integer> regularShapesWhite;
        private final Map<String, Integer> regularShapesRed;
        private final Map<ComplexShape, Integer> complexShapes;
        private final double width;
        private final double length;
        
        public Report(Map<String, Integer> regularShapesWhite, 
                     Map<String, Integer> regularShapesRed,
                     Map<ComplexShape, Integer> complexShapes,
                     double width,
                     double length) {
            this.regularShapesWhite = new HashMap<>(regularShapesWhite);
            this.regularShapesRed = new HashMap<>(regularShapesRed);
            this.complexShapes = new HashMap<>(complexShapes);
            this.width = width;
            this.length = length;
        }
        
        /**
         * Formats the report as a string in table format (matching image export format).
         */
        public String formatReport() {
            StringBuilder sb = new StringBuilder();
            
            // Build combined counts from regular shapes
            Map<String, Integer> redCounts = new HashMap<>(regularShapesRed);
            Map<String, Integer> whiteCounts = new HashMap<>(regularShapesWhite);
            
            // Add complex shapes to the counts
            // Complex shapes are counted as regular shapes in the table format
            for (Map.Entry<ComplexShape, Integer> entry : complexShapes.entrySet()) {
                ComplexShape complex = entry.getKey();
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
            
            // Title
            sb.append("Shapes Report:\n\n");
            
            // Table header
            sb.append(String.format("%-15s %-8s %-8s\n", "Key", "Red", "White"));
            sb.append("----------------------------------------\n");
            
            // Table rows
            int totalRed = 0;
            int totalWhite = 0;
            
            for (String key : allKeys) {
                int redCount = redCounts.getOrDefault(key, 0);
                int whiteCount = whiteCounts.getOrDefault(key, 0);
                
                // Skip rows with zero counts in both columns
                if (redCount == 0 && whiteCount == 0) {
                    continue;
                }
                
                totalRed += redCount;
                totalWhite += whiteCount;
                
                sb.append(String.format("%-15s %-8d %-8d\n", key, redCount, whiteCount));
            }
            
            // Total row
            sb.append("----------------------------------------\n");
            sb.append(String.format("%-15s %-8d %-8d\n", "Total", totalRed, totalWhite));
            
            // Detailed summary (only in dedicated report)
            sb.append("\n");
            sb.append("Summary:\n");
            int totalComplex = complexShapes.values().stream().mapToInt(Integer::intValue).sum();
            int totalRegularWhite = regularShapesWhite.values().stream().mapToInt(Integer::intValue).sum();
            int totalRegularRed = regularShapesRed.values().stream().mapToInt(Integer::intValue).sum();
            int totalRegular = totalRegularWhite + totalRegularRed;
            
            sb.append(String.format("  Complex shapes: %d\n", totalComplex));
            sb.append(String.format("  Regular shapes (white): %d\n", totalRegularWhite));
            sb.append(String.format("  Regular shapes (red): %d\n", totalRegularRed));
            sb.append(String.format("  Total regular shapes: %d\n", totalRegular));
            sb.append(String.format("  Total shapes: %d\n", totalComplex + totalRegular));
            
            // Add width and length
            sb.append("\n");
            sb.append("Dimensions:\n");
            sb.append(String.format("  Width: %.2f\n", width));
            sb.append(String.format("  Length: %.2f\n", length));
            
            return sb.toString();
        }
    }
}

