package com.trackdraw.config;

import com.trackdraw.model.ShapeInstance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for ShapeConfig.
 * Tests shape loading and the new 025 shape.
 */
@DisplayName("ShapeConfig Tests")
public class ShapeConfigTest {
    
    private ShapeConfig shapeConfig;
    
    @BeforeEach
    public void setUp() throws IOException {
        shapeConfig = new ShapeConfig();
        shapeConfig.loadShapes();
    }
    
    @Test
    @DisplayName("Should load all shapes from config")
    public void testLoadShapes() {
        assertThat(shapeConfig.getAllShapes()).isNotEmpty();
    }
    
    @Test
    @DisplayName("Should load new 025 shape")
    public void testLoad025Shape() {
        ShapeInstance shape025 = shapeConfig.getShape("025");
        assertThat(shape025).isNotNull();
        assertThat(shape025.getKey()).isEqualTo("025");
        assertThat(shape025.getType()).isEqualTo("annular_sector");
    }
    
    @Test
    @DisplayName("Should load all expected shape types")
    public void testLoadAllShapeTypes() {
        // Annular sectors
        assertThat(shapeConfig.getShape("025")).isNotNull();
        assertThat(shapeConfig.getShape("05")).isNotNull();
        assertThat(shapeConfig.getShape("10")).isNotNull();
        assertThat(shapeConfig.getShape("15")).isNotNull();
        assertThat(shapeConfig.getShape("20")).isNotNull();
        assertThat(shapeConfig.getShape("25")).isNotNull();
        assertThat(shapeConfig.getShape("30")).isNotNull();
        
        // Rectangles
        assertThat(shapeConfig.getShape("L")).isNotNull();
        assertThat(shapeConfig.getShape("L3")).isNotNull();
        assertThat(shapeConfig.getShape("L5")).isNotNull();
        assertThat(shapeConfig.getShape("L7")).isNotNull();
        
        // Half-circle
        assertThat(shapeConfig.getShape("E")).isNotNull();
    }
    
    @Test
    @DisplayName("025 shape should have correct properties")
    public void test025ShapeProperties() {
        ShapeInstance shape025 = shapeConfig.getShape("025");
        assertThat(shape025).isNotNull();
        assertThat(shape025).isInstanceOf(com.trackdraw.model.AnnularSector.class);
        
        com.trackdraw.model.AnnularSector annular025 = (com.trackdraw.model.AnnularSector) shape025;
        assertThat(annular025.getExternalDiameter()).isEqualTo(25.0);
        assertThat(annular025.getAngleDegrees()).isEqualTo(90.0);
        assertThat(annular025.getWidth()).isEqualTo(6.0);
    }
    
    @Test
    @DisplayName("Should return null for non-existent shape key")
    public void testGetNonExistentShape() {
        ShapeInstance shape = shapeConfig.getShape("NONEXISTENT");
        assertThat(shape).isNull();
    }
}

