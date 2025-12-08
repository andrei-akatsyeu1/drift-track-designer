package com.trackdraw.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data class for background image information.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BackgroundImageData {
    private String imagePath; // Path to the image file
    private double scale; // Scale factor (default 1.0 = 100%)
    
    public BackgroundImageData(String imagePath) {
        this.imagePath = imagePath;
        this.scale = 1.0; // Default scale
    }
}

