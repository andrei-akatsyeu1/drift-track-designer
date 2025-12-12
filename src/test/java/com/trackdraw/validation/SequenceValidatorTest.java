package com.trackdraw.validation;

import com.trackdraw.config.ShapeConfig;
import com.trackdraw.model.AnnularSector;
import com.trackdraw.model.HalfCircle;
import com.trackdraw.model.Rectangle;
import com.trackdraw.model.ShapeInstance;
import com.trackdraw.model.ShapeSequence;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for SequenceValidator.
 * Tests REQ-010: Validation Rules, including new 025 shape.
 */
@DisplayName("SequenceValidator Tests")
public class SequenceValidatorTest {
    
    private ShapeConfig shapeConfig;
    
    @BeforeEach
    public void setUp() throws IOException {
        shapeConfig = new ShapeConfig();
        shapeConfig.loadShapes();
    }
    
    /**
     * Helper method to create a copy of a shape instance.
     */
    private ShapeInstance copyShape(ShapeInstance original) {
        if (original instanceof AnnularSector) {
            AnnularSector sector = (AnnularSector) original;
            AnnularSector copy = new AnnularSector(sector.getKey(), 
                sector.getExternalDiameter(), sector.getAngleDegrees(), sector.getWidth());
            copy.setOrientation(original.getOrientation());
            copy.setRed(original.isRed());
            return copy;
        } else if (original instanceof Rectangle) {
            Rectangle rect = (Rectangle) original;
            Rectangle copy = new Rectangle(rect.getKey(), rect.getLength(), rect.getWidth());
            copy.setOrientation(original.getOrientation());
            copy.setRed(original.isRed());
            return copy;
        } else if (original instanceof HalfCircle) {
            HalfCircle hc = (HalfCircle) original;
            HalfCircle copy = new HalfCircle(hc.getKey(), hc.getDiameter());
            copy.setOrientation(original.getOrientation());
            copy.setRed(original.isRed());
            return copy;
        }
        throw new IllegalArgumentException("Unknown shape type: " + original.getType());
    }
    
    @Test
    @DisplayName("Should validate allowed 025 combinations")
    public void test025AllowedCombinations() {
        ShapeInstance shape025 = shapeConfig.getShape("025");
        ShapeInstance shape05 = shapeConfig.getShape("05");
        ShapeInstance shapeL = shapeConfig.getShape("L");
        
        // Create sequences for validation
        ShapeSequence seq1 = new ShapeSequence("Test1");
        ShapeInstance shape025Copy = copyShape(shape025);
        shape025Copy.setOrientation(1);
        seq1.addShape(shape025Copy);
        
        ShapeSequence seq2 = new ShapeSequence("Test2");
        ShapeInstance shape05Copy = copyShape(shape05);
        shape05Copy.setOrientation(-1);
        seq2.addShape(shape05Copy);
        
        // 025,05 with different orientations (allowed)
        SequenceValidator.ValidationResult result = SequenceValidator.validateLinkedSequence(
            seq1.getShapes().get(0), seq2);
        assertThat(result.isValid()).isTrue();
        
        // 025,L with different orientations (allowed)
        ShapeSequence seq3 = new ShapeSequence("Test3");
        ShapeInstance shapeLCopy = copyShape(shapeL);
        shapeLCopy.setOrientation(1); // L always has orientation 1
        seq3.addShape(shapeLCopy);
        
        result = SequenceValidator.validateLinkedSequence(seq1.getShapes().get(0), seq3);
        assertThat(result.isValid()).isTrue();
    }
    
    @Test
    @DisplayName("Should reject 025,025 with same orientation")
    public void test025SameOrientationRejected() {
        ShapeInstance shape025 = shapeConfig.getShape("025");
        
        ShapeSequence seq1 = new ShapeSequence("Test1");
        ShapeInstance shape025Copy1 = copyShape(shape025);
        shape025Copy1.setOrientation(1);
        seq1.addShape(shape025Copy1);
        
        ShapeSequence seq2 = new ShapeSequence("Test2");
        ShapeInstance shape025Copy2 = copyShape(shape025);
        shape025Copy2.setOrientation(1); // Same orientation
        seq2.addShape(shape025Copy2);
        
        // Same orientation should be rejected when invertAlignment = false
        SequenceValidator.ValidationResult result = SequenceValidator.validateLinkedSequence(
            seq1.getShapes().get(0), seq2);
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrorMessage()).contains("different orientations");
    }
    
    @Test
    @DisplayName("Should allow 025,025 with different orientations")
    public void test025DifferentOrientationsAllowed() {
        ShapeInstance shape025 = shapeConfig.getShape("025");
        
        ShapeSequence seq1 = new ShapeSequence("Test1");
        ShapeInstance shape025Copy1 = copyShape(shape025);
        shape025Copy1.setOrientation(1);
        seq1.addShape(shape025Copy1);
        
        ShapeSequence seq2 = new ShapeSequence("Test2");
        ShapeInstance shape025Copy2 = copyShape(shape025);
        shape025Copy2.setOrientation(-1);
        seq2.addShape(shape025Copy2);
        
        SequenceValidator.ValidationResult result = SequenceValidator.validateLinkedSequence(
            seq1.getShapes().get(0), seq2);
        assertThat(result.isValid()).isTrue();
    }
    
    @Test
    @DisplayName("Should validate with invertAlignment - colors must differ")
    public void testInvertAlignmentColorRequirement() {
        // Use shapes that are definitely allowed together (025,10 from CSV)
        ShapeInstance shape1 = shapeConfig.getShape("025");
        ShapeInstance shape2 = shapeConfig.getShape("10");
        
        ShapeSequence seq1 = new ShapeSequence("Test1");
        ShapeInstance shape1Copy = copyShape(shape1);
        shape1Copy.setRed(true);
        seq1.addShape(shape1Copy);
        
        ShapeSequence seq2 = new ShapeSequence("Test2");
        seq2.setInvertAlignment(true);
        ShapeInstance shape2Copy = copyShape(shape2);
        shape2Copy.setRed(true); // Same color as shape1
        seq2.addShape(shape2Copy);
        
        // Same colors with invertAlignment = true should be rejected
        SequenceValidator.ValidationResult result = SequenceValidator.validateLinkedSequence(
            seq1.getShapes().get(0), seq2);
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrorMessage()).contains("different colors");
        
        // Different colors with invertAlignment = true should be allowed
        ShapeSequence seq3 = new ShapeSequence("Test3");
        seq3.setInvertAlignment(true);
        ShapeInstance shape2Copy2 = copyShape(shape2);
        shape2Copy2.setRed(false); // Different color
        seq3.addShape(shape2Copy2);
        result = SequenceValidator.validateLinkedSequence(seq1.getShapes().get(0), seq3);
        assertThat(result.isValid()).isTrue();
    }
    
    @Test
    @DisplayName("Should reject shape E having shapes after it")
    public void testShapeECannotHaveShapesAfter() {
        ShapeInstance shapeE = shapeConfig.getShape("E");
        ShapeInstance shapeL = shapeConfig.getShape("L");
        
        ShapeSequence seq = new ShapeSequence("Test");
        seq.addShape(copyShape(shapeE));
        
        // E cannot have shapes added after it
        SequenceValidator.ValidationResult result = SequenceValidator.validateAddShape(
            seq, copyShape(shapeL), 1);
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrorMessage()).contains("E");
        assertThat(result.getErrorMessage()).contains("end of sequence");
    }
    
    @Test
    @DisplayName("Should validate 025 combinations from allowed_combinations.csv")
    public void test025CombinationsFromCSV() {
        ShapeInstance shape025 = shapeConfig.getShape("025");
        ShapeInstance shape10 = shapeConfig.getShape("10");
        ShapeInstance shape15 = shapeConfig.getShape("15");
        ShapeInstance shape20 = shapeConfig.getShape("20");
        ShapeInstance shape25 = shapeConfig.getShape("25");
        ShapeInstance shape30 = shapeConfig.getShape("30");
        ShapeInstance shapeL = shapeConfig.getShape("L");
        
        // Test combinations from CSV: 025,10/15/20/25/30/L all allowed with different orientations
        ShapeSequence seq025 = new ShapeSequence("Test025");
        ShapeInstance shape025Copy = copyShape(shape025);
        shape025Copy.setOrientation(1);
        seq025.addShape(shape025Copy);
        
        // Test each combination
        ShapeSequence seq10 = new ShapeSequence("Test10");
        ShapeInstance shape10Copy = copyShape(shape10);
        shape10Copy.setOrientation(-1);
        seq10.addShape(shape10Copy);
        assertThat(SequenceValidator.validateLinkedSequence(seq025.getShapes().get(0), seq10).isValid()).isTrue();
        
        ShapeSequence seq15 = new ShapeSequence("Test15");
        ShapeInstance shape15Copy = copyShape(shape15);
        shape15Copy.setOrientation(-1);
        seq15.addShape(shape15Copy);
        assertThat(SequenceValidator.validateLinkedSequence(seq025.getShapes().get(0), seq15).isValid()).isTrue();
        
        ShapeSequence seq20 = new ShapeSequence("Test20");
        ShapeInstance shape20Copy = copyShape(shape20);
        shape20Copy.setOrientation(-1);
        seq20.addShape(shape20Copy);
        assertThat(SequenceValidator.validateLinkedSequence(seq025.getShapes().get(0), seq20).isValid()).isTrue();
        
        ShapeSequence seq25 = new ShapeSequence("Test25");
        ShapeInstance shape25Copy = copyShape(shape25);
        shape25Copy.setOrientation(-1);
        seq25.addShape(shape25Copy);
        assertThat(SequenceValidator.validateLinkedSequence(seq025.getShapes().get(0), seq25).isValid()).isTrue();
        
        ShapeSequence seq30 = new ShapeSequence("Test30");
        ShapeInstance shape30Copy = copyShape(shape30);
        shape30Copy.setOrientation(-1);
        seq30.addShape(shape30Copy);
        assertThat(SequenceValidator.validateLinkedSequence(seq025.getShapes().get(0), seq30).isValid()).isTrue();
        
        ShapeSequence seqL = new ShapeSequence("TestL");
        ShapeInstance shapeLCopy = copyShape(shapeL);
        shapeLCopy.setOrientation(1); // L always 1
        seqL.addShape(shapeLCopy);
        assertThat(SequenceValidator.validateLinkedSequence(seq025.getShapes().get(0), seqL).isValid()).isTrue();
    }
}

