package com.trackdraw.validation;

import com.trackdraw.config.AllowedCombinationsLoader;
import com.trackdraw.model.HalfCircle;
import com.trackdraw.model.ShapeInstance;
import com.trackdraw.model.ShapeSequence;

import java.io.IOException;
import java.util.Set;

/**
 * Validates shape sequence operations according to business rules.
 */
public class SequenceValidator {
    private static AllowedCombinationsLoader combinationsLoader;
    
    static {
        try {
            combinationsLoader = new AllowedCombinationsLoader();
            combinationsLoader.load();
        } catch (IOException e) {
            System.err.println("Failed to load allowed combinations: " + e.getMessage());
            e.printStackTrace();
            // Fallback to empty loader (will allow all combinations)
            combinationsLoader = new AllowedCombinationsLoader();
        }
    }
    
    /**
     * Validates if a shape can be added to a sequence.
     * Rule: E (HalfCircle) is the end of sequence, nothing can be added after it.
     * 
     * @param sequence The sequence to add to
     * @param newShape The shape to add
     * @return ValidationResult with isValid flag and error message if invalid
     */
    public static ValidationResult validateAddShape(ShapeSequence sequence, ShapeInstance newShape) {
        if (sequence == null || newShape == null) {
            return ValidationResult.invalid("Sequence or shape is null");
        }
        
        // Check if sequence already ends with E (HalfCircle)
        if (!sequence.isEmpty()) {
            ShapeInstance lastShape = sequence.getShapes().get(sequence.size() - 1);
            if (lastShape instanceof HalfCircle) {
                return ValidationResult.invalid("Cannot add shapes after E (HalfCircle) - it is the end of sequence");
            }
        }
        
        return ValidationResult.valid();
    }
    
    /**
     * Validates if a sequence can be linked to a shape.
     * Rules:
     * - If invertAlignment = false: only specific combinations are allowed
     * - If invertAlignment = true: any combination is allowed, but colors must be different
     * 
     * @param linkedShape The shape to link to
     * @param newSequence The sequence being created
     * @return ValidationResult with isValid flag and error message if invalid
     */
    public static ValidationResult validateLinkedSequence(ShapeInstance linkedShape, ShapeSequence newSequence) {
        if (linkedShape == null || newSequence == null) {
            return ValidationResult.invalid("Linked shape or sequence is null");
        }
        
        if (newSequence.isEmpty()) {
            return ValidationResult.valid(); // Empty sequence is valid
        }
        
        ShapeInstance firstShape = newSequence.getShapes().get(0);
        boolean invertAlignment = newSequence.isInvertAlignment();
        
        if (invertAlignment) {
            // Rule 2.1: Any combination allowed, but colors must be different
            boolean linkedShapeEffectiveRed = linkedShape.getEffectiveIsRed();
            boolean firstShapeEffectiveRed = firstShape.getEffectiveIsRed();
            
            if (linkedShapeEffectiveRed == firstShapeEffectiveRed) {
                return ValidationResult.invalid(
                    String.format("When invertAlignment is true, linked shape (%s) and first shape (%s) must have different colors",
                        linkedShape.getKey(), firstShape.getKey())
                );
            }
            
            return ValidationResult.valid();
        } else {
            // Rule 2: Validation rules apply to the pair of shapes, regardless of which is linked/first
            // Get both keys (order doesn't matter for pair validation)
            String key1 = linkedShape.getKey();
            String key2 = firstShape.getKey();
            
            // Check if this pair allows same orientation (exception pairs)
            // Exception: 05 + L|30|25 allow same orientation
            boolean allowsSameOrientation = isExceptionPair(key1, key2);
            
            // Check orientation difference (unless exception applies)
            if (!allowsSameOrientation) {
                int orientation1 = linkedShape.getOrientation();
                int orientation2 = firstShape.getOrientation();
                if (orientation1 == orientation2) {
                    return ValidationResult.invalid(
                        String.format("When invertAlignment is false, shapes (%s) and (%s) must have different orientations (both have %d)",
                            key1, key2, orientation1)
                    );
                }
            }
            
            // Check allowed combinations (symmetric - order doesn't matter)
            // Allowed pairs:
            // - 05 can pair with: 05, 10, 15, 20, 25, 30, L
            // - 10 can pair with: 05, 10, 15
            
            if (!isAllowedPair(key1, key2)) {
                // Determine which shape has restrictions to provide better error message
                Set<String> allowedFor1 = getAllowedPartners(key1);
                Set<String> allowedFor2 = getAllowedPartners(key2);
                
                if (!allowedFor1.isEmpty()) {
                    return ValidationResult.invalid(
                        String.format("Shape %s can only be paired with: %s (found: %s)", 
                            key1, String.join(", ", allowedFor1), key2)
                    );
                } else if (!allowedFor2.isEmpty()) {
                    return ValidationResult.invalid(
                        String.format("Shape %s can only be paired with: %s (found: %s)", 
                            key2, String.join(", ", allowedFor2), key1)
                    );
                } else {
                    return ValidationResult.invalid(
                        String.format("Shapes %s and %s cannot be paired", key1, key2)
                    );
                }
            }
            
            return ValidationResult.valid();
        }
    }
    
    /**
     * Checks if a pair of shape keys is an exception pair that allows same orientation.
     * @param key1 First shape key
     * @param key2 Second shape key
     * @return true if this pair allows same orientation
     */
    private static boolean isExceptionPair(String key1, String key2) {
        return combinationsLoader.isExceptionPair(key1, key2);
    }
    
    /**
     * Checks if a pair of shape keys is allowed.
     * @param key1 First shape key
     * @param key2 Second shape key
     * @return true if this pair is allowed
     */
    private static boolean isAllowedPair(String key1, String key2) {
        return combinationsLoader.isAllowedPair(key1, key2);
    }
    
    /**
     * Gets allowed partners for a shape (for error messages).
     * @param shapeKey The shape key
     * @return Set of allowed partner keys
     */
    private static Set<String> getAllowedPartners(String shapeKey) {
        return combinationsLoader.getAllowedPartners(shapeKey);
    }
    
    /**
     * Result of a validation operation.
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String errorMessage;
        
        private ValidationResult(boolean valid, String errorMessage) {
            this.valid = valid;
            this.errorMessage = errorMessage;
        }
        
        public static ValidationResult valid() {
            return new ValidationResult(true, null);
        }
        
        public static ValidationResult invalid(String errorMessage) {
            return new ValidationResult(false, errorMessage);
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public String getErrorMessage() {
            return errorMessage;
        }
    }
}

