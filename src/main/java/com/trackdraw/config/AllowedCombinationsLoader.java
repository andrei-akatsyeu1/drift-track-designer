package com.trackdraw.config;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Loads allowed shape combinations from CSV file.
 */
public class AllowedCombinationsLoader {
    private static final String CSV_FILE = "allowed_combinations.csv";
    
    private final Map<String, Set<String>> allowedPairs; // shape -> set of allowed partners
    private final Set<Pair> exceptionPairs; // pairs that allow same orientation
    
    /**
     * Represents a pair of shapes (order doesn't matter for comparison).
     */
    public static class Pair {
        private final String key1;
        private final String key2;
        
        public Pair(String key1, String key2) {
            // Normalize order for consistent comparison
            if (key1.compareTo(key2) <= 0) {
                this.key1 = key1;
                this.key2 = key2;
            } else {
                this.key1 = key2;
                this.key2 = key1;
            }
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Pair pair = (Pair) o;
            return Objects.equals(key1, pair.key1) && Objects.equals(key2, pair.key2);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(key1, key2);
        }
        
        public String getKey1() {
            return key1;
        }
        
        public String getKey2() {
            return key2;
        }
    }
    
    public AllowedCombinationsLoader() {
        this.allowedPairs = new HashMap<>();
        this.exceptionPairs = new HashSet<>();
    }
    
    /**
     * Loads allowed combinations from CSV file.
     */
    public void load() throws IOException {
        allowedPairs.clear();
        exceptionPairs.clear();
        
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(CSV_FILE);
        
        if (inputStream == null) {
            // Fallback to file system (for development)
            File csvFile = new File("src/main/resources", CSV_FILE);
            if (!csvFile.exists()) {
                throw new FileNotFoundException("Allowed combinations CSV file not found: " + CSV_FILE);
            }
            inputStream = new FileInputStream(csvFile);
        }
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"))) {
            String line = reader.readLine(); // Skip header
            if (line == null || !line.startsWith("shape1")) {
                throw new IOException("Invalid CSV format: missing header");
            }
            
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (StringUtils.isEmpty(line)) {
                    continue;
                }
                
                String[] parts = line.split(",");
                if (parts.length < 3) {
                    continue; // Skip invalid lines
                }
                
                String shape1 = parts[0].trim();
                String shape2 = parts[1].trim();
                boolean allowsSameOrientation = Boolean.parseBoolean(parts[2].trim());
                
                // Add to allowed pairs (bidirectional)
                allowedPairs.computeIfAbsent(shape1, k -> new HashSet<>()).add(shape2);
                allowedPairs.computeIfAbsent(shape2, k -> new HashSet<>()).add(shape1);
                
                // Add to exception pairs if allows same orientation
                if (allowsSameOrientation) {
                    exceptionPairs.add(new Pair(shape1, shape2));
                }
            }
        }
    }
    
    /**
     * Checks if a pair of shapes is allowed.
     * @param key1 First shape key
     * @param key2 Second shape key
     * @return true if this pair is allowed
     */
    public boolean isAllowedPair(String key1, String key2) {
        // If no restrictions exist for either shape, allow it
        if (!allowedPairs.containsKey(key1) && !allowedPairs.containsKey(key2)) {
            return true;
        }
        
        // Check if key2 is in allowed pairs for key1
        Set<String> allowedForKey1 = allowedPairs.get(key1);
        if (allowedForKey1 != null && allowedForKey1.contains(key2)) {
            return true;
        }
        
        // Check if key1 is in allowed pairs for key2
        Set<String> allowedForKey2 = allowedPairs.get(key2);
        if (allowedForKey2 != null && allowedForKey2.contains(key1)) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Checks if a pair of shapes allows same orientation (exception pair).
     * @param key1 First shape key
     * @param key2 Second shape key
     * @return true if this pair allows same orientation
     */
    public boolean isExceptionPair(String key1, String key2) {
        return exceptionPairs.contains(new Pair(key1, key2));
    }
    
    /**
     * Gets all allowed partners for a given shape.
     * @param shapeKey The shape key
     * @return Set of allowed partner keys, or empty set if no restrictions
     */
    public Set<String> getAllowedPartners(String shapeKey) {
        return allowedPairs.getOrDefault(shapeKey, Collections.emptySet());
    }
}

