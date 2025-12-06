package com.trackdraw.config;

/**
 * Global scale holder for the application.
 * Used throughout the application to access the current scale factor.
 */
public class GlobalScale {
    private static double scale = 3.0;
    
    public static double getScale() {
        return scale;
    }
    
    public static void setScale(double newScale) {
        if (newScale < 0.1) {
            newScale = 0.1;
        }
        scale = newScale;
    }
}

