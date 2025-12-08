package com.trackdraw.config;

import com.trackdraw.model.AlignPosition;
import com.trackdraw.model.ShapeInstance;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Data class for serializing/deserializing sequences.
 * Handles the complex initialAlignment which can be AlignPosition or ShapeInstance reference.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SequenceData {
    private String name;
    private boolean active;
    private boolean invertAlignment;
    private List<ShapeInstanceData> shapes = new ArrayList<>();
    private InitialAlignmentData initialAlignment;
    
    public void setShapes(List<ShapeInstanceData> shapes) {
        this.shapes = shapes != null ? new ArrayList<>(shapes) : new ArrayList<>();
    }
    
    /**
     * Represents initial alignment data - can be AlignPosition or ShapeInstance reference.
     */
    @Data
    @NoArgsConstructor
    public static class InitialAlignmentData {
        private String type; // "position" or "shape"
        private AlignPositionData position; // If type is "position"
        private UUID shapeId; // If type is "shape" - reference to shape UUID
        
        public static InitialAlignmentData fromPosition(AlignPosition pos) {
            InitialAlignmentData data = new InitialAlignmentData();
            data.type = "position";
            data.position = new AlignPositionData(pos.getX(), pos.getY(), pos.getAngle());
            return data;
        }
        
        public static InitialAlignmentData fromShape(ShapeInstance shape) {
            InitialAlignmentData data = new InitialAlignmentData();
            data.type = "shape";
            data.shapeId = shape.getId();
            return data;
        }
    }
    
    /**
     * Represents AlignPosition data.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AlignPositionData {
        private double x;
        private double y;
        private double angle;
        
        public AlignPosition toAlignPosition() {
            return new AlignPosition(x, y, angle);
        }
    }
    
    /**
     * Represents ShapeInstance data with all instance properties.
     */
    @Data
    @NoArgsConstructor
    public static class ShapeInstanceData {
        private UUID id;
        private String type; // "annular_sector" or "rectangle"
        private String key;
        private double externalDiameter; // For annular_sector
        private double angleDegrees; // For annular_sector
        private double width; // For annular_sector or rectangle
        private double length; // For rectangle
        private int orientation;
        private boolean active;
        private boolean isRed;
        private boolean forceInvertColor;
    }
}
