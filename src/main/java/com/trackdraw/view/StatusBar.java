package com.trackdraw.view;

import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.*;

/**
 * Status bar component to display messages instead of popups.
 */
public class StatusBar extends JPanel {
    private final JLabel statusLabel;
    
    public StatusBar() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createLoweredBevelBorder());
        setPreferredSize(new Dimension(0, 25));
        
        statusLabel = new JLabel("Ready");
        statusLabel.setHorizontalAlignment(SwingConstants.LEFT);
        add(statusLabel, BorderLayout.WEST);
    }
    
    /**
     * Sets the status message.
     * @param message Status message to display
     */
    public void setStatus(String message) {
        statusLabel.setText(StringUtils.defaultIfBlank(message, "Ready"));
    }
    
    /**
     * Sets the status message with a timeout (auto-clears after specified milliseconds).
     * @param message Status message to display
     * @param timeoutMs Timeout in milliseconds
     */
    public void setStatus(String message, int timeoutMs) {
        setStatus(message);
        Timer timer = new Timer(timeoutMs, e -> setStatus("Ready"));
        timer.setRepeats(false);
        timer.start();
    }
}

