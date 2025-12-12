package com.trackdraw.view;

import com.trackdraw.config.SequenceManager;
import com.trackdraw.model.ShapeSequence;
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
 * UI tests for file operations.
 * Tests REQ-020, REQ-021: Save and Load Sequences through UI.
 */
@DisplayName("File Operations UI Tests")
public class FileOperationsUITest extends BaseUITest {
    
    @Test
    @DisplayName("Should save sequences to file")
    public void testSaveSequences(@TempDir Path tempDir) throws IOException {
        waitForUI();
        
        // Create test sequences
        List<ShapeSequence> testSequences = new ArrayList<>();
        testSequences.add(new ShapeSequence("TestSeq1"));
        testSequences.add(new ShapeSequence("TestSeq2"));
        
        // Set sequences in main window
        mainWindow.getAllSequences().clear();
        mainWindow.getAllSequences().addAll(testSequences);
        
        // Save using SequenceManager (simulating UI save operation)
        File testFile = tempDir.resolve("test_save.json").toFile();
        SequenceManager manager = new SequenceManager();
        manager.saveSequences(testSequences, testFile.getAbsolutePath());
        
        // Verify file was created
        assertThat(testFile.exists()).isTrue();
        assertThat(testFile.length()).isGreaterThan(0);
    }
    
    @Test
    @DisplayName("Should load sequences from file")
    public void testLoadSequences(@TempDir Path tempDir) throws IOException {
        waitForUI();
        
        // Create and save test sequences
        List<ShapeSequence> originalSequences = new ArrayList<>();
        originalSequences.add(new ShapeSequence("LoadedSeq1"));
        originalSequences.add(new ShapeSequence("LoadedSeq2"));
        
        File testFile = tempDir.resolve("test_load.json").toFile();
        SequenceManager manager = new SequenceManager();
        manager.saveSequences(originalSequences, testFile.getAbsolutePath());
        
        // Load sequences
        SequenceManager.LoadResult result = manager.loadSequences(testFile.getAbsolutePath());
        
        // Verify loaded sequences
        assertThat(result.getSequences()).hasSize(2);
        assertThat(result.getSequences().get(0).getName()).isEqualTo("LoadedSeq1");
        assertThat(result.getSequences().get(1).getName()).isEqualTo("LoadedSeq2");
    }
    
    @Test
    @DisplayName("Should handle loading production sequences.json")
    public void testLoadProductionSequences() throws IOException {
        waitForUI();
        
        File productionFile = new File("saves/sequences.json");
        if (!productionFile.exists()) {
            System.out.println("Skipping test - production file not found");
            return;
        }
        
        SequenceManager manager = new SequenceManager();
        SequenceManager.LoadResult result = manager.loadSequences(productionFile.getAbsolutePath());
        
        assertThat(result.getSequences()).isNotEmpty();
        
        // Verify sequences can be set in main window
        mainWindow.getAllSequences().clear();
        mainWindow.getAllSequences().addAll(result.getSequences());
        
        assertThat(mainWindow.getAllSequences().size()).isEqualTo(result.getSequences().size());
    }
    
    @Test
    @DisplayName("Should preserve sequence count after save/load cycle")
    public void testSaveLoadCycle(@TempDir Path tempDir) throws IOException {
        waitForUI();
        
        // Create sequences
        List<ShapeSequence> originalSequences = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            originalSequences.add(new ShapeSequence("Seq" + i));
        }
        
        // Save
        File testFile = tempDir.resolve("test_cycle.json").toFile();
        SequenceManager manager = new SequenceManager();
        manager.saveSequences(originalSequences, testFile.getAbsolutePath());
        
        // Load
        SequenceManager.LoadResult result = manager.loadSequences(testFile.getAbsolutePath());
        
        // Verify count preserved
        assertThat(result.getSequences()).hasSize(5);
    }
}

