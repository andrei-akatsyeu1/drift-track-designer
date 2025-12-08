package com.trackdraw.view;

import com.trackdraw.model.AlignPosition;
import com.trackdraw.model.ShapeInstance;
import com.trackdraw.model.ShapeSequence;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Panel for managing multiple ShapeSequences.
 * Allows creating, deleting, and selecting sequences.
 */
public class ShapeSequencePanel extends JPanel {
    private JTextField newSeqField;
    private JButton addButton;
    private JButton addFromShapeButton;
    private JButton deleteButton;
    private JButton invertAlignmentButton;
    private JList<String> sequenceList;
    private DefaultListModel<String> sequenceModel;
    
    private List<ShapeSequence> sequences;
    private Consumer<ShapeSequence> sequenceChangeHandler; // Handler for when active sequence changes
    private java.util.function.Supplier<ShapeSequence> activeSequenceSupplier; // Supplier to get active sequence from MainWindow
    private java.util.function.Supplier<List<ShapeSequence>> allSequencesSupplier; // Supplier to get all sequences from MainWindow
    
    public ShapeSequencePanel() {
        this.sequences = new ArrayList<>();
        initializeComponents();
        setupLayout();
    }
    
    /**
     * Sets a larger font for a button.
     * @param button Button to set font for
     */
    private void setButtonFont(JButton button) {
        Font currentFont = button.getFont();
        Font largerFont = new Font(currentFont.getName(), currentFont.getStyle(), currentFont.getSize() + 4);
        button.setFont(largerFont);
    }
    
    private void initializeComponents() {
        newSeqField = new JTextField(15);
        newSeqField.setToolTipText("Name for new sequence");
        
        addButton = new JButton("✚");
        setButtonFont(addButton);
        addButton.setToolTipText("Add new sequence");
        addButton.addActionListener(e -> addNewSequence());
        
        addFromShapeButton = new JButton("✚🔗");
        setButtonFont(addFromShapeButton);
        addFromShapeButton.setToolTipText("Add new sequence from active shape");
        addFromShapeButton.addActionListener(e -> addSequenceFromShape());
        
        deleteButton = new JButton("🗑");
        setButtonFont(deleteButton);
        deleteButton.setToolTipText("Delete active sequence");
        deleteButton.addActionListener(e -> deleteSelectedSequence());
        
        invertAlignmentButton = new JButton("↻"); // Unicode rotate symbol
        setButtonFont(invertAlignmentButton);
        invertAlignmentButton.setToolTipText("Invert alignment (only for sequences linked to shapes)");
        invertAlignmentButton.addActionListener(e -> toggleInvertAlignment());
        
        sequenceModel = new DefaultListModel<>();
        sequenceList = new JList<>(sequenceModel);
        sequenceList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        sequenceList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                onSequenceSelectionChanged();
            }
        });
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Top panel: text field on first line
        JPanel textFieldPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        textFieldPanel.add(new JLabel("New seq:"));
        textFieldPanel.add(newSeqField);
        
        // Buttons panel: buttons on second line
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonsPanel.add(addButton);
        buttonsPanel.add(addFromShapeButton);
        buttonsPanel.add(deleteButton);
        buttonsPanel.add(invertAlignmentButton);
        
        // Combine text field and buttons panels
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(textFieldPanel, BorderLayout.NORTH);
        topPanel.add(buttonsPanel, BorderLayout.CENTER);
        
        add(topPanel, BorderLayout.NORTH);
        
        // Center: list of sequences
        JScrollPane scrollPane = new JScrollPane(sequenceList);
        scrollPane.setPreferredSize(new Dimension(0, 150));
        add(scrollPane, BorderLayout.CENTER);
    }
    
    /**
     * Sets the list of sequences to display.
     * @param sequences List of ShapeSequence objects
     */
    public void setSequences(List<ShapeSequence> sequences) {
        this.sequences = sequences != null ? new ArrayList<>(sequences) : new ArrayList<>();
        updateListModel();
        
        // Select the active sequence if any
        ShapeSequence activeSeq = getActiveSequence();
        if (activeSeq != null) {
            int index = this.sequences.indexOf(activeSeq);
            if (index >= 0) {
                sequenceList.setSelectedIndex(index);
            }
        }
        
        // Pre-fill next sequence default name
        newSeqField.setText("Sequence " + (this.sequences.size() + 1));
        
        // Update invert button state
        updateInvertButtonState();
    }
    
    /**
     * Gets the list of sequences.
     * @return List of ShapeSequence objects
     */
    public List<ShapeSequence> getSequences() {
        return new ArrayList<>(sequences);
    }
    
    /**
     * Gets the currently selected sequence.
     * @return Selected ShapeSequence, or null if none selected
     */
    public ShapeSequence getSelectedSequence() {
        int selectedIndex = sequenceList.getSelectedIndex();
        if (selectedIndex >= 0 && selectedIndex < sequences.size()) {
            return sequences.get(selectedIndex);
        }
        return null;
    }
    
    /**
     * Gets the active sequence (the one that is active).
     * @return Active ShapeSequence, or null if none active
     */
    public ShapeSequence getActiveSequence() {
        for (ShapeSequence seq : sequences) {
            if (seq.isActive()) {
                return seq;
            }
        }
        return null;
    }
    
    /**
     * Sets the handler to be called when the active sequence changes.
     * @param handler Handler that receives the new active sequence
     */
    public void setSequenceChangeHandler(Consumer<ShapeSequence> handler) {
        this.sequenceChangeHandler = handler;
    }
    
    /**
     * Sets suppliers to get active sequence and all sequences from MainWindow.
     * This allows the panel to access the current state when buttons are clicked.
     * @param activeSeqSupplier Supplier that returns the active sequence
     * @param allSeqSupplier Supplier that returns all sequences
     */
    public void setSequenceSuppliers(java.util.function.Supplier<ShapeSequence> activeSeqSupplier,
                                      java.util.function.Supplier<List<ShapeSequence>> allSeqSupplier) {
        this.activeSequenceSupplier = activeSeqSupplier;
        this.allSequencesSupplier = allSeqSupplier;
    }
    
    /**
     * Adds a new sequence with the name from the text field.
     * Sets align position to center of canvas, makes it active and selected.
     */
    private void addNewSequence() {
        String name = newSeqField.getText().trim();
        if (name.isEmpty()) {
            name = "Sequence " + (sequences.size() + 1);
        }
        
        ShapeSequence newSeq = new ShapeSequence(name);
        
        // Set initial alignment to center (will be set when drawing)
        // For now, we'll set it when drawing, but we can store it here if needed
        
        // Deactivate all other sequences
        for (ShapeSequence seq : sequences) {
            seq.setActive(false);
        }
        
        // Add new sequence and make it active
        sequences.add(newSeq);
        newSeq.setActive(true);
        
        // Update UI
        updateListModel();
        
        // Select the new active sequence
        int newSeqIndex = sequences.indexOf(newSeq);
        if (newSeqIndex >= 0) {
            sequenceList.setSelectedIndex(newSeqIndex);
        }
        
        newSeqField.setText("");
        
        // Notify handler
        if (sequenceChangeHandler != null) {
            sequenceChangeHandler.accept(newSeq);
        }
    }
    
    /**
     * Adds a new sequence from the currently selected (active) shape in the active sequence.
     * If there's no active shape, shows a warning.
     */
    private void addSequenceFromShape() {
        // Get active sequence from MainWindow if supplier is available
        ShapeSequence activeSeq = (activeSequenceSupplier != null) ? activeSequenceSupplier.get() : getActiveSequence();
        if (activeSeq == null || activeSeq.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "No active sequence with shapes found.",
                "No Active Shape",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Find the active shape in the active sequence
        ShapeInstance activeShape = null;
        for (ShapeInstance shape : activeSeq.getShapes()) {
            if (shape.isActive()) {
                activeShape = shape;
                break;
            }
        }
        
        if (activeShape == null) {
            JOptionPane.showMessageDialog(this,
                "No active shape found in the active sequence. Please select a shape first.",
                "No Active Shape",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Check if shape has align position set (it should if it's been drawn)
        if (activeShape.getAlignPosition() == null) {
            JOptionPane.showMessageDialog(this,
                "Selected shape has not been positioned yet. Please draw the sequence first.",
                "Shape Not Positioned",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Calculate the next align position by simulating drawing
        // Create a dummy Graphics2D to calculate the next position
        java.awt.image.BufferedImage dummyImage = new java.awt.image.BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D dummyG2d = dummyImage.createGraphics();
        AlignPosition nextPosition = activeShape.draw(dummyG2d);
        dummyG2d.dispose();
        
        if (nextPosition == null) {
            JOptionPane.showMessageDialog(this,
                "Cannot calculate next alignment position from selected shape.",
                "Alignment Error",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String name = newSeqField.getText().trim();
        if (name.isEmpty()) {
            name = "Sequence " + (sequences.size() + 1);
        }
        
        ShapeSequence newSeq = new ShapeSequence(name);
        
        // Set initial alignment to link to the active shape (so first shape color can be opposite)
        newSeq.setInitialAlignment(activeShape);
        
        // Deactivate all other sequences
        for (ShapeSequence seq : sequences) {
            seq.setActive(false);
        }
        
        // Add new sequence and make it active
        sequences.add(newSeq);
        newSeq.setActive(true);
        
        // Update UI
        updateListModel();
        
        // Select the new active sequence
        int newSeqIndex = sequences.indexOf(newSeq);
        if (newSeqIndex >= 0) {
            sequenceList.setSelectedIndex(newSeqIndex);
        }
        
        // Pre-fill next sequence default name
        newSeqField.setText("Sequence " + (sequences.size() + 1));
        
        // Update invert button state
        updateInvertButtonState();
        
        // Notify handler to sync with MainWindow
        if (sequenceChangeHandler != null) {
            sequenceChangeHandler.accept(newSeq);
        }
    }
    
    /**
     * Deletes the active sequence.
     * If there are links on shapes from this sequence, shows warning and doesn't delete.
     */
    private void deleteSelectedSequence() {
        // Get the active sequence (not just selected in list)
        ShapeSequence seqToDelete = getActiveSequence();
        
        if (seqToDelete == null) {
            JOptionPane.showMessageDialog(this,
                "No active sequence to delete.",
                "No Active Sequence",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int deleteIndex = sequences.indexOf(seqToDelete);
        if (deleteIndex < 0) {
            return;
        }
        
        // Get all sequences from MainWindow if supplier is available (to check all sequences, not just panel's list)
        List<ShapeSequence> allSeqs = (allSequencesSupplier != null) ? allSequencesSupplier.get() : sequences;
        
        // Check if any other sequence links to shapes from this sequence
        boolean hasLinks = false;
        for (ShapeSequence otherSeq : allSeqs) {
            if (otherSeq == seqToDelete) {
                continue;
            }
            
            ShapeInstance linkedShape = otherSeq.getInitialAlignmentAsShape();
            if (linkedShape != null) {
                // Check if this shape belongs to the sequence we're trying to delete
                for (ShapeInstance shape : seqToDelete.getShapes()) {
                    if (shape.getId().equals(linkedShape.getId())) {
                        hasLinks = true;
                        break;
                    }
                }
                if (hasLinks) {
                    break;
                }
            }
        }
        
        if (hasLinks) {
            JOptionPane.showMessageDialog(this,
                "Cannot delete sequence: other sequences are linked to shapes in this sequence.",
                "Cannot Delete",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Remove sequence from panel's list
        sequences.remove(deleteIndex);
        
        // Clear all shapes from the deleted sequence
        seqToDelete.clear();
        
        // If it was active, activate the first sequence (if any)
        ShapeSequence newActiveSeq = null;
        if (!sequences.isEmpty()) {
            newActiveSeq = sequences.get(0);
            newActiveSeq.setActive(true);
        }
        
        // Update UI
        updateListModel();
        
        // Select the new active sequence (always select active sequence)
        if (newActiveSeq != null) {
            int selectIndex = sequences.indexOf(newActiveSeq);
            if (selectIndex >= 0) {
                sequenceList.setSelectedIndex(selectIndex);
            }
        } else if (!sequences.isEmpty()) {
            // If no active sequence, select first one
            sequenceList.setSelectedIndex(0);
        }
        
        // Update invert button state
        updateInvertButtonState();
        
        // Notify handler to sync with MainWindow (this will update MainWindow's allSequences)
        if (sequenceChangeHandler != null) {
            sequenceChangeHandler.accept(newActiveSeq);
        }
    }
    
    /**
     * Updates the list model to reflect current sequences.
     */
    private void updateListModel() {
        sequenceModel.clear();
        for (ShapeSequence seq : sequences) {
            String displayName = seq.getName();
            if (seq.isActive()) {
                displayName += " (active)";
            }
            sequenceModel.addElement(displayName);
        }
    }
    
    /**
     * Selects the given sequence in the list.
     * @param sequence Sequence to select
     */
    private void selectSequence(ShapeSequence sequence) {
        int index = sequences.indexOf(sequence);
        if (index >= 0) {
            sequenceList.setSelectedIndex(index);
        }
    }
    
    /**
     * Toggles the invertAlignment flag for the active sequence.
     * Only works if the sequence is linked to a shape (not a position).
     */
    private void toggleInvertAlignment() {
        ShapeSequence activeSeq = getActiveSequence();
        if (activeSeq == null) {
            return;
        }
        
        // Only allow inversion if linked to a shape (not a position)
        if (activeSeq.getInitialAlignmentAsShape() == null) {
            // Do nothing if not linked to a shape
            return;
        }
        
        // Toggle the invertAlignment flag
        activeSeq.setInvertAlignment(!activeSeq.isInvertAlignment());
        
        // Update button appearance to show state
        updateInvertButtonState();
        
        // Notify handler to trigger redraw
        if (sequenceChangeHandler != null) {
            sequenceChangeHandler.accept(activeSeq);
        }
    }
    
    /**
     * Updates the invert alignment button state based on active sequence.
     */
    private void updateInvertButtonState() {
        ShapeSequence activeSeq = getActiveSequence();
        if (activeSeq != null && activeSeq.getInitialAlignmentAsShape() != null) {
            // Enable button and show state
            invertAlignmentButton.setEnabled(true);
            if (activeSeq.isInvertAlignment()) {
                invertAlignmentButton.setText("↻"); // Show inverted state (could use different symbol)
                invertAlignmentButton.setToolTipText("Invert alignment: ON (click to turn off)");
            } else {
                invertAlignmentButton.setText("↻");
                invertAlignmentButton.setToolTipText("Invert alignment: OFF (click to turn on)");
            }
        } else {
            // Disable button if not linked to shape
            invertAlignmentButton.setEnabled(false);
            invertAlignmentButton.setToolTipText("Invert alignment (only for sequences linked to shapes)");
        }
    }
    
    /**
     * Called when sequence selection changes.
     */
    private void onSequenceSelectionChanged() {
        int selectedIndex = sequenceList.getSelectedIndex();
        if (selectedIndex >= 0 && selectedIndex < sequences.size()) {
            // Deactivate all sequences
            for (ShapeSequence seq : sequences) {
                seq.setActive(false);
            }
            
            // Activate selected sequence
            ShapeSequence selectedSeq = sequences.get(selectedIndex);
            selectedSeq.setActive(true);
            
            // Temporarily disable selection listener to prevent circular calls
            javax.swing.event.ListSelectionListener[] listeners = sequenceList.getListSelectionListeners();
            for (javax.swing.event.ListSelectionListener listener : listeners) {
                sequenceList.removeListSelectionListener(listener);
            }
            
            try {
                // Update UI
                updateListModel();
                
                // Ensure the active sequence is selected (without triggering events)
                sequenceList.setSelectedIndex(selectedIndex);
            } finally {
                // Re-enable selection listeners
                for (javax.swing.event.ListSelectionListener listener : listeners) {
                    sequenceList.addListSelectionListener(listener);
                }
            }
            
            // Notify handler
            if (sequenceChangeHandler != null) {
                sequenceChangeHandler.accept(selectedSeq);
            }
            
            // Update invert button state
            updateInvertButtonState();
        }
    }
}

