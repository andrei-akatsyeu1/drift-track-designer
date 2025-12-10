package com.trackdraw.view;

import javax.swing.*;
import java.awt.*;

/**
 * Dialog for configuring export options.
 */
public class ExportDialog extends JDialog {
    private JCheckBox showBackgroundImageCheckBox;
    private JCheckBox showKeyCheckBox;
    private JCheckBox shapesReportCheckBox;
    private JSpinner scaleSpinner;
    private boolean confirmed = false;
    
    public ExportDialog(Frame parent) {
        super(parent, "Export Image", true);
        initializeComponents();
        setupLayout();
        pack();
        setLocationRelativeTo(parent);
    }
    
    private void initializeComponents() {
        showBackgroundImageCheckBox = new JCheckBox("Show Background Image", true);
        showKeyCheckBox = new JCheckBox("Show Shape Keys", true);
        shapesReportCheckBox = new JCheckBox("Include Shapes Report", false);
        
        // Scale spinner: 1 to 10, default 1
        SpinnerNumberModel scaleModel = new SpinnerNumberModel(1, 1, 10, 1);
        scaleSpinner = new JSpinner(scaleModel);
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        JPanel contentPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Show Background Image
        gbc.gridx = 0;
        gbc.gridy = 0;
        contentPanel.add(showBackgroundImageCheckBox, gbc);
        
        // Show Keys
        gbc.gridy = 1;
        contentPanel.add(showKeyCheckBox, gbc);
        
        // Shapes Report
        gbc.gridy = 2;
        contentPanel.add(shapesReportCheckBox, gbc);
        
        // Scale
        gbc.gridy = 3;
        gbc.gridx = 0;
        contentPanel.add(new JLabel("Scale:"), gbc);
        gbc.gridx = 1;
        contentPanel.add(scaleSpinner, gbc);
        
        add(contentPanel, BorderLayout.CENTER);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton okButton = new JButton("OK");
        okButton.addActionListener(e -> {
            confirmed = true;
            dispose();
        });
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> {
            confirmed = false;
            dispose();
        });
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    public boolean isShowBackgroundImage() {
        return showBackgroundImageCheckBox.isSelected();
    }
    
    public boolean isShowKey() {
        return showKeyCheckBox.isSelected();
    }
    
    public boolean isShapesReport() {
        return shapesReportCheckBox.isSelected();
    }
    
    public int getScale() {
        return ((Number) scaleSpinner.getValue()).intValue();
    }
    
    public boolean isConfirmed() {
        return confirmed;
    }
}

