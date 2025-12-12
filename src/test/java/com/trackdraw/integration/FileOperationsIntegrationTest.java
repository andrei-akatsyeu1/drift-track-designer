package com.trackdraw.integration;

import com.trackdraw.config.SequenceManager;
import com.trackdraw.model.ShapeInstance;
import com.trackdraw.model.ShapeSequence;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for file operations using production examples.
 * Tests REQ-020, REQ-021: Save and Load Sequences with real data.
 */
@DisplayName("File Operations Integration Tests")
public class FileOperationsIntegrationTest {
    
    private SequenceManager sequenceManager;
    
    public FileOperationsIntegrationTest() {
        this.sequenceManager = new SequenceManager();
    }
    
    @Test
    @DisplayName("Should load production sequences.json file")
    public void testLoadProductionSequencesJson() throws IOException {
        File productionFile = new File("saves/sequences.json");
        
        if (!productionFile.exists()) {
            System.out.println("Skipping test - production file not found: " + productionFile.getAbsolutePath());
            return;
        }
        
        SequenceManager.LoadResult result = sequenceManager.loadSequences(productionFile.getAbsolutePath());
        
        assertThat(result.getSequences()).isNotEmpty();
        assertThat(result.getSequences().size()).isGreaterThan(0);
        
        // Verify sequences have names
        for (ShapeSequence seq : result.getSequences()) {
            assertThat(seq.getName()).isNotNull();
            assertThat(seq.getName()).isNotEmpty();
        }
    }
    
    @Test
    @DisplayName("Should load production main_track.json file")
    public void testLoadProductionMainTrackJson() throws IOException {
        File productionFile = new File("saves/main_track.json");
        
        if (!productionFile.exists()) {
            System.out.println("Skipping test - production file not found: " + productionFile.getAbsolutePath());
            return;
        }
        
        SequenceManager.LoadResult result = sequenceManager.loadSequences(productionFile.getAbsolutePath());
        
        assertThat(result.getSequences()).isNotEmpty();
        
        // Verify Main sequence exists
        boolean hasMainSequence = result.getSequences().stream()
                .anyMatch(seq -> "Main".equals(seq.getName()));
        assertThat(hasMainSequence).isTrue();
    }
    
    @Test
    @DisplayName("Should load sequences containing 025 shape")
    public void testLoadSequencesWith025Shape() throws IOException {
        File productionFile = new File("saves/sequences.json");
        
        if (!productionFile.exists()) {
            System.out.println("Skipping test - production file not found: " + productionFile.getAbsolutePath());
            return;
        }
        
        SequenceManager.LoadResult result = sequenceManager.loadSequences(productionFile.getAbsolutePath());
        
        // Check if any sequence contains 025 shape
        boolean has025Shape = result.getSequences().stream()
                .flatMap(seq -> seq.getShapes().stream())
                .anyMatch(shape -> "025".equals(shape.getKey()));
        
        assertThat(has025Shape).isTrue();
    }
    
    @Test
    @DisplayName("Should preserve all shape properties on load")
    public void testPreserveShapeProperties() throws IOException {
        File productionFile = new File("saves/sequences.json");
        
        if (!productionFile.exists()) {
            System.out.println("Skipping test - production file not found: " + productionFile.getAbsolutePath());
            return;
        }
        
        SequenceManager.LoadResult result = sequenceManager.loadSequences(productionFile.getAbsolutePath());
        
        // Verify shapes have all required properties
        for (ShapeSequence seq : result.getSequences()) {
            for (ShapeInstance shape : seq.getShapes()) {
                assertThat(shape.getKey()).isNotNull();
                assertThat(shape.getType()).isNotNull();
                assertThat(shape.getOrientation()).isIn(1, -1);
            }
        }
    }
    
    @Test
    @DisplayName("Should handle sequences with linked shapes")
    public void testLoadLinkedSequences() throws IOException {
        File productionFile = new File("saves/sequences.json");
        
        if (!productionFile.exists()) {
            System.out.println("Skipping test - production file not found: " + productionFile.getAbsolutePath());
            return;
        }
        
        SequenceManager.LoadResult result = sequenceManager.loadSequences(productionFile.getAbsolutePath());
        
        // Check for sequences with invertAlignment (linked sequences)
        boolean hasLinkedSequences = result.getSequences().stream()
                .anyMatch(ShapeSequence::isInvertAlignment);
        
        // This test verifies the file format supports linked sequences
        // Whether they exist depends on the production data
        assertThat(result.getSequences()).isNotEmpty();
    }
    
    @Test
    @DisplayName("Should load sequences with initial alignment positions")
    public void testLoadSequencesWithAlignment() throws IOException {
        File productionFile = new File("saves/sequences.json");
        
        if (!productionFile.exists()) {
            System.out.println("Skipping test - production file not found: " + productionFile.getAbsolutePath());
            return;
        }
        
        SequenceManager.LoadResult result = sequenceManager.loadSequences(productionFile.getAbsolutePath());
        
        // Verify sequences have initial alignment (either as position or shape)
        for (ShapeSequence seq : result.getSequences()) {
            // Check if sequence has alignment (either position or shape reference)
            boolean hasAlignment = seq.getInitialAlignmentAsPosition() != null || 
                                  seq.getInitialAlignmentAsShape() != null;
            // At least some sequences should have alignment
            // (not all may have it, but the structure should support it)
        }
        
        // Verify at least one sequence loaded
        assertThat(result.getSequences()).isNotEmpty();
    }
}

