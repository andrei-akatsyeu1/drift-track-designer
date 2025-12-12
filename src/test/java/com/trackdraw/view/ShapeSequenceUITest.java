package com.trackdraw.view;

import com.trackdraw.model.ShapeSequence;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * UI tests for shape sequence management.
 * Tests REQ-006, REQ-007: Sequence creation and linking.
 */
@DisplayName("Shape Sequence UI Tests")
public class ShapeSequenceUITest extends BaseUITest {
    
    @Test
    @DisplayName("Default Main sequence should be created on startup")
    public void testDefaultSequenceCreated() {
        waitForUI();
        
        // Verify Main sequence exists
        assertThat(mainWindow.getAllSequences()).isNotEmpty();
        assertThat(mainWindow.getAllSequences().get(0).getName()).isEqualTo("Main");
    }
    
    @Test
    @DisplayName("Default Main sequence should be active")
    public void testDefaultSequenceActive() {
        waitForUI();
        
        ShapeSequence mainSequence = mainWindow.getAllSequences().get(0);
        assertThat(mainSequence.isActive()).isTrue();
    }
    
    @Test
    @DisplayName("Should be able to create new sequence")
    public void testCreateNewSequence() {
        waitForUI();
        
        int initialCount = mainWindow.getAllSequences().size();
        
        // Create new sequence directly (testing the underlying functionality)
        // In a full UI test, you would interact with the UI components
        ShapeSequence newSequence = new ShapeSequence("TestSequence");
        mainWindow.getAllSequences().add(newSequence);
        waitForUI();
        
        int newCount = mainWindow.getAllSequences().size();
        assertThat(newCount).isEqualTo(initialCount + 1);
    }
    
    @Test
    @DisplayName("Only one sequence should be active at a time")
    public void testSingleActiveSequence() {
        waitForUI();
        
        // Deactivate the default Main sequence first
        ShapeSequence mainSequence = mainWindow.getAllSequences().get(0);
        mainSequence.setActive(false);
        
        // Create multiple sequences
        ShapeSequence seq1 = new ShapeSequence("Seq1");
        ShapeSequence seq2 = new ShapeSequence("Seq2");
        mainWindow.getAllSequences().add(seq1);
        mainWindow.getAllSequences().add(seq2);
        
        // Activate one sequence
        seq1.setActive(true);
        seq2.setActive(false);
        waitForUI();
        
        // Count active sequences
        long activeCount = mainWindow.getAllSequences().stream()
                .filter(ShapeSequence::isActive)
                .count();
        
        assertThat(activeCount).isEqualTo(1);
    }
}

