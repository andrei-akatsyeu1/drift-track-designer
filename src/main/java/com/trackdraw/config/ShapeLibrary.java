package com.trackdraw.config;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Manages shape library configuration loaded from CSV file.
 * Tracks the count of 3D printed shapes available for each shape type and color.
 */
public class ShapeLibrary {
    private static final String CONFIG_FILE = "shape_library.csv";
    
    // Pattern for complex shapes: +key1+key2, +key1-key2, -key1+key2, or -key1-key2
    // Where key1 and key2 are shape keys, and keys are alphabetically sorted
    private static final Pattern COMPLEX_SHAPE_PATTERN = Pattern.compile("^[+-]([^+-]+)[+-]([^+-]+)$");
    
    // Map: (shapeKey, isRed) -> count
    // Internal representation uses boolean isRed (true=RED, false=WHITE)
    // CSV file uses "color" column with values "RED" or "WHITE"
    private Map<String, Integer> libraryCounts;
    
    public ShapeLibrary() {
        this.libraryCounts = new HashMap<>();
    }
    
    /**
     * Loads shape library from CSV file and validates it.
     * CSV format: shapeKey,color,count
     * color: RED or WHITE (case-insensitive)
     * 
     * @param shapeConfig ShapeConfig instance to validate against (can be null to skip validation)
     * @throws IOException if file cannot be read
     * @throws IllegalArgumentException if validation fails (duplicates, unknown shapes, invalid format)
     */
    public void loadLibrary(ShapeConfig shapeConfig) throws IOException {
        libraryCounts.clear();
        
        // Try to load from classpath first (for packaged JAR)
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(CONFIG_FILE);
        
        if (inputStream == null) {
            // Fallback to file system (for development)
            File configFile = new File("src/main/resources", CONFIG_FILE);
            if (!configFile.exists()) {
                throw new IOException("Shape library file not found: " + CONFIG_FILE + 
                    " (checked classpath and " + configFile.getAbsolutePath() + ")");
            }
            inputStream = new FileInputStream(configFile);
        }
        
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            
            // Read and validate header line
            String header = reader.readLine();
            if (header == null) {
                return;
            }
            
            // Validate header format
            String[] headerParts = header.split(",");
            if (headerParts.length < 3 || 
                !"shapeKey".equalsIgnoreCase(headerParts[0].trim()) ||
                !"color".equalsIgnoreCase(headerParts[1].trim()) ||
                !"count".equalsIgnoreCase(headerParts[2].trim())) {
                throw new IllegalArgumentException(
                    String.format("Invalid header in shape library CSV: '%s' - expected: shapeKey,color,count", header));
            }
            
            // Track entries for duplicate detection
            Set<String> seenEntries = new HashSet<>();
            int lineNumber = 1; // Header is line 1, first data line is 2
            
            String line;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                
                // Parse CSV line: shapeKey,color,count
                String[] parts = line.split(",");
                if (parts.length < 3) {
                    throw new IllegalArgumentException(
                        String.format("Invalid line in shape library CSV (line %d): %s - expected format: shapeKey,color,count", 
                            lineNumber, line));
                }
                
                String shapeKey = parts[0].trim();
                String colorStr = parts[1].trim().toUpperCase();
                String countStr = parts[2].trim();
                
                // Validate color
                if (!"RED".equals(colorStr) && !"WHITE".equals(colorStr)) {
                    throw new IllegalArgumentException(
                        String.format("Invalid color value in shape library CSV (line %d): %s - expected 'RED' or 'WHITE'", 
                            lineNumber, parts[1].trim()));
                }
                
                boolean isRed = "RED".equals(colorStr);
                
                // Validate count
                int count;
                try {
                    count = Integer.parseInt(countStr);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException(
                        String.format("Invalid count in shape library CSV (line %d): %s - expected integer", 
                            lineNumber, countStr));
                }
                
                // Check for duplicates
                String compositeKey = getKey(shapeKey, isRed);
                if (seenEntries.contains(compositeKey)) {
                    throw new IllegalArgumentException(
                        String.format("Duplicate entry in shape library CSV (line %d): shapeKey='%s', color=%s", 
                            lineNumber, shapeKey, colorStr));
                }
                seenEntries.add(compositeKey);
                
                // Validate shape key if ShapeConfig is provided
                if (shapeConfig != null) {
                    validateShapeKey(shapeKey, lineNumber, shapeConfig);
                }
                
                // Store count using composite key
                libraryCounts.put(compositeKey, count);
            }
        }
    }
    
    /**
     * Overloaded method for backward compatibility - loads without validation.
     * @deprecated Use loadLibrary(ShapeConfig) for validation
     */
    @Deprecated
    public void loadLibrary() throws IOException {
        loadLibrary(null);
    }
    
    /**
     * Validates a shape key against ShapeConfig.
     * 
     * @param shapeKey The shape key to validate
     * @param lineNumber Line number for error reporting
     * @param shapeConfig ShapeConfig instance to validate against
     * @throws IllegalArgumentException if shape key is invalid
     */
    private void validateShapeKey(String shapeKey, int lineNumber, ShapeConfig shapeConfig) {
        // Check if it's a complex shape (starts with + or -)
        if (shapeKey.startsWith("+") || shapeKey.startsWith("-")) {
            validateComplexShape(shapeKey, lineNumber, shapeConfig);
        } else {
            // Simple shape - must exist in ShapeConfig
            if (shapeConfig.getShape(shapeKey) == null) {
                throw new IllegalArgumentException(
                    String.format("Unknown shape in shape library CSV (line %d): '%s' - shape not found in shapes.json", 
                        lineNumber, shapeKey));
            }
        }
    }
    
    /**
     * Validates a complex shape format and component shapes.
     * 
     * @param complexKey Complex shape key (e.g., "+05-05", "+10+15", "-10+15")
     * @param lineNumber Line number for error reporting
     * @param shapeConfig ShapeConfig instance to validate against
     * @throws IllegalArgumentException if complex shape is invalid
     */
    private void validateComplexShape(String complexKey, int lineNumber, ShapeConfig shapeConfig) {
        // Match pattern: +key1+key2, +key1-key2, -key1+key2, or -key1-key2
        java.util.regex.Matcher matcher = COMPLEX_SHAPE_PATTERN.matcher(complexKey);
        
        if (!matcher.matches()) {
            throw new IllegalArgumentException(
                String.format("Invalid complex shape format in shape library CSV (line %d): '%s' - " +
                    "expected format: +key1+key2, +key1-key2, -key1+key2, or -key1-key2 (with orientation signs)", 
                    lineNumber, complexKey));
        }
        
        String key1 = matcher.group(1);
        String key2 = matcher.group(2);
        
        // Extract orientation signs
        char sign1 = complexKey.charAt(0);
        char sign2 = complexKey.charAt(key1.length() + 1);
        
        // Validate orientation signs
        if (sign1 != '+' && sign1 != '-') {
            throw new IllegalArgumentException(
                String.format("Invalid orientation sign in complex shape (line %d): '%s' - first sign must be + or -", 
                    lineNumber, complexKey));
        }
        if (sign2 != '+' && sign2 != '-') {
            throw new IllegalArgumentException(
                String.format("Invalid orientation sign in complex shape (line %d): '%s' - second sign must be + or -", 
                    lineNumber, complexKey));
        }
        
        // Validate that both component shapes exist
        if (shapeConfig.getShape(key1) == null) {
            throw new IllegalArgumentException(
                String.format("Unknown shape in complex shape (line %d): '%s' - component shape '%s' not found in shapes.json", 
                    lineNumber, complexKey, key1));
        }
        if (shapeConfig.getShape(key2) == null) {
            throw new IllegalArgumentException(
                String.format("Unknown shape in complex shape (line %d): '%s' - component shape '%s' not found in shapes.json", 
                    lineNumber, complexKey, key2));
        }
        
        // Validate normalization: keys must be alphabetically sorted
        int keyCompare = key1.compareTo(key2);
        if (keyCompare > 0) {
            throw new IllegalArgumentException(
                String.format("Complex shape not properly normalized (line %d): '%s' - keys must be alphabetically sorted " +
                    "(expected '%s' or '%s')", 
                    lineNumber, complexKey, 
                    formatComplexShape(key2, sign2, key1, sign1),
                    formatComplexShape(key1, sign1, key2, sign2)));
        }
        
        // If keys are equal, validate that + comes before -
        if (keyCompare == 0) {
            if (sign1 == '-' && sign2 == '+') {
                throw new IllegalArgumentException(
                    String.format("Complex shape not properly normalized (line %d): '%s' - when keys are equal, " +
                        "+ orientation must come before - (expected '+%s-%s')", 
                        lineNumber, complexKey, key1, key2));
            }
        }
    }
    
    /**
     * Formats a complex shape key for error messages.
     */
    private String formatComplexShape(String key1, char sign1, String key2, char sign2) {
        return String.valueOf(sign1) + key1 + String.valueOf(sign2) + key2;
    }
    
    /**
     * Gets the count of shapes available in the library for the given shape key and color.
     * 
     * @param shapeKey The shape key (e.g., "05", "L", "05+05")
     * @param isRed true for red, false for white
     * @return The count of shapes available, or 0 if not found
     */
    public int getCount(String shapeKey, boolean isRed) {
        String key = getKey(shapeKey, isRed);
        return libraryCounts.getOrDefault(key, 0);
    }
    
    /**
     * Sets the count of shapes available in the library for the given shape key and color.
     * 
     * @param shapeKey The shape key (e.g., "05", "L", "05+05")
     * @param isRed true for red, false for white
     * @param count The count to set
     */
    public void setCount(String shapeKey, boolean isRed, int count) {
        String key = getKey(shapeKey, isRed);
        libraryCounts.put(key, count);
    }
    
    /**
     * Creates a composite key from shapeKey and isRed.
     */
    private String getKey(String shapeKey, boolean isRed) {
        return shapeKey + ":" + isRed;
    }
    
    /**
     * Gets all library entries.
     * 
     * @return Map of (shapeKey, isRed) -> count
     */
    public Map<String, Map<Boolean, Integer>> getAllEntries() {
        Map<String, Map<Boolean, Integer>> result = new HashMap<>();
        
        for (Map.Entry<String, Integer> entry : libraryCounts.entrySet()) {
            String compositeKey = entry.getKey();
            int count = entry.getValue();
            
            // Parse composite key: shapeKey:isRed
            int colonIndex = compositeKey.lastIndexOf(':');
            if (colonIndex > 0) {
                String shapeKey = compositeKey.substring(0, colonIndex);
                boolean isRed = Boolean.parseBoolean(compositeKey.substring(colonIndex + 1));
                
                result.computeIfAbsent(shapeKey, k -> new HashMap<>()).put(isRed, count);
            }
        }
        
        return result;
    }
}

