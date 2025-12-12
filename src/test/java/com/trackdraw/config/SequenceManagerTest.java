package com.trackdraw.config;

import com.trackdraw.model.ShapeSequence;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for SequenceManager.
 * Tests REQ-020, REQ-021: Save and Load Sequences.
 */
@DisplayName("SequenceManager Tests")
public class SequenceManagerTest {
    
    private SequenceManager sequenceManager;
    
    @BeforeEach
    public void setUp() {
        sequenceManager = new SequenceManager();
    }
    
    @Test
    @DisplayName("Should save and load empty sequences list")
    public void testSaveLoadEmptySequences(@TempDir Path tempDir) throws IOException {
        File testFile = tempDir.resolve("test_empty.json").toFile();
        List<ShapeSequence> emptyList = new ArrayList<>();
        
        sequenceManager.saveSequences(emptyList, testFile.getAbsolutePath());
        
        SequenceManager.LoadResult result = sequenceManager.loadSequences(testFile.getAbsolutePath());
        assertThat(result.getSequences()).isEmpty();
        assertThat(result.getBackgroundImagePath()).isNull();
        assertThat(result.getBackgroundImageScale()).isEqualTo(1.0);
    }
    
    @Test
    @DisplayName("Should save and load sequences with single sequence")
    public void testSaveLoadSingleSequence(@TempDir Path tempDir) throws IOException {
        File testFile = tempDir.resolve("test_single.json").toFile();
        List<ShapeSequence> sequences = new ArrayList<>();
        ShapeSequence seq = new ShapeSequence("TestSequence");
        sequences.add(seq);
        
        sequenceManager.saveSequences(sequences, testFile.getAbsolutePath());
        
        SequenceManager.LoadResult result = sequenceManager.loadSequences(testFile.getAbsolutePath());
        assertThat(result.getSequences()).hasSize(1);
        assertThat(result.getSequences().get(0).getName()).isEqualTo("TestSequence");
    }
    
    @Test
    @DisplayName("Should save and load multiple sequences")
    public void testSaveLoadMultipleSequences(@TempDir Path tempDir) throws IOException {
        File testFile = tempDir.resolve("test_multiple.json").toFile();
        List<ShapeSequence> sequences = new ArrayList<>();
        sequences.add(new ShapeSequence("Seq1"));
        sequences.add(new ShapeSequence("Seq2"));
        sequences.add(new ShapeSequence("Seq3"));
        
        sequenceManager.saveSequences(sequences, testFile.getAbsolutePath());
        
        SequenceManager.LoadResult result = sequenceManager.loadSequences(testFile.getAbsolutePath());
        assertThat(result.getSequences()).hasSize(3);
        assertThat(result.getSequences().get(0).getName()).isEqualTo("Seq1");
        assertThat(result.getSequences().get(1).getName()).isEqualTo("Seq2");
        assertThat(result.getSequences().get(2).getName()).isEqualTo("Seq3");
    }
    
    @Test
    @DisplayName("Should save and load background image path")
    public void testSaveLoadBackgroundImage(@TempDir Path tempDir) throws IOException {
        File testFile = tempDir.resolve("test_bgimage.json").toFile();
        List<ShapeSequence> sequences = new ArrayList<>();
        sequences.add(new ShapeSequence("Test"));
        
        String bgImagePath = "images/test.jpg";
        double bgImageScale = 1.5;
        
        sequenceManager.saveSequences(sequences, bgImagePath, bgImageScale, testFile.getAbsolutePath());
        
        SequenceManager.LoadResult result = sequenceManager.loadSequences(testFile.getAbsolutePath());
        assertThat(result.getBackgroundImagePath()).isEqualTo(bgImagePath);
        assertThat(result.getBackgroundImageScale()).isEqualTo(bgImageScale);
    }
    
    @Test
    @DisplayName("Should return empty list for non-existent file")
    public void testLoadNonExistentFile() throws IOException {
        SequenceManager.LoadResult result = sequenceManager.loadSequences("nonexistent.json");
        assertThat(result.getSequences()).isEmpty();
        assertThat(result.getBackgroundImagePath()).isNull();
    }
    
    @Test
    @DisplayName("Should preserve sequence properties on save/load")
    public void testPreserveSequenceProperties(@TempDir Path tempDir) throws IOException {
        File testFile = tempDir.resolve("test_properties.json").toFile();
        List<ShapeSequence> sequences = new ArrayList<>();
        ShapeSequence seq = new ShapeSequence("TestSeq");
        seq.setActive(true);
        seq.setInvertAlignment(true);
        sequences.add(seq);
        
        sequenceManager.saveSequences(sequences, testFile.getAbsolutePath());
        
        SequenceManager.LoadResult result = sequenceManager.loadSequences(testFile.getAbsolutePath());
        ShapeSequence loadedSeq = result.getSequences().get(0);
        assertThat(loadedSeq.getName()).isEqualTo("TestSeq");
        assertThat(loadedSeq.isActive()).isTrue();
        assertThat(loadedSeq.isInvertAlignment()).isTrue();
    }
}

