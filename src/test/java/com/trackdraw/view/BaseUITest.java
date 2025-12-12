package com.trackdraw.view;

import org.assertj.swing.core.BasicRobot;
import org.assertj.swing.core.Robot;
import org.assertj.swing.fixture.FrameFixture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.awt.*;

/**
 * Base class for UI tests using AssertJ Swing with JUnit 5.
 * Provides common setup and teardown for Swing component testing.
 */
public abstract class BaseUITest {
    
    protected Robot robot;
    protected FrameFixture window;
    protected MainWindow mainWindow;
    
    @BeforeEach
    public void setUp() {
        // Set headless mode for CI/CD environments (optional)
        // System.setProperty("java.awt.headless", "false");
        
        // Create robot for UI interactions (JUnit 5 approach)
        robot = BasicRobot.robotWithNewAwtHierarchy();
        
        // Create and show the main window
        mainWindow = new MainWindow();
        window = new FrameFixture(robot, mainWindow);
        window.show(new Dimension(1200, 800));
    }
    
    @AfterEach
    public void tearDown() {
        if (window != null) {
            window.cleanUp();
        }
        if (robot != null) {
            robot.cleanUp();
        }
    }
    
    /**
     * Helper method to wait for UI to be ready.
     * Use this when you need to ensure components are fully initialized.
     */
    protected void waitForUI() {
        try {
            Thread.sleep(100); // Small delay for UI to settle
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

