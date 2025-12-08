package com.trackdraw.config;

import com.trackdraw.model.AlignPosition;
import com.trackdraw.model.AnnularSector;
import com.trackdraw.model.Rectangle;
import com.trackdraw.model.ShapeInstance;
import com.trackdraw.model.ShapeSequence;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.util.*;

/**
 * Manages saving and loading sequences to/from JSON files.
 */
public class SequenceManager {
    private static final String DEFAULT_FILE = "sequences.json";
    private final Gson gson;
    
    public SequenceManager() {
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();
        this.gson = builder.create();
    }
    
    /**
     * Saves sequences to a JSON file.
     * 
     * @param sequences List of sequences to save
     * @param filePath Path to the file (null for default)
     * @throws IOException If file writing fails
     */
    public void saveSequences(List<ShapeSequence> sequences, String filePath) throws IOException {
        String path = filePath != null ? filePath : DEFAULT_FILE;
        File file = new File(path);
        file.getParentFile().mkdirs();
        
        // Convert sequences to data objects
        List<SequenceData> sequenceDataList = new ArrayList<>();
        for (ShapeSequence sequence : sequences) {
            sequenceDataList.add(convertToData(sequence));
        }
        
        try (Writer writer = new FileWriter(file, java.nio.charset.StandardCharsets.UTF_8)) {
            gson.toJson(sequenceDataList, writer);
        }
    }
    
    /**
     * Loads sequences from a JSON file.
     * 
     * @param filePath Path to the file (null for default)
     * @return List of loaded sequences
     * @throws IOException If file reading fails
     */
    public List<ShapeSequence> loadSequences(String filePath) throws IOException {
        String path = filePath != null ? filePath : DEFAULT_FILE;
        File file = new File(path);
        
        if (!file.exists()) {
            return new ArrayList<>();
        }
        
        try (Reader reader = new FileReader(file, java.nio.charset.StandardCharsets.UTF_8)) {
            List<SequenceData> sequenceDataList = gson.fromJson(
                reader,
                new TypeToken<List<SequenceData>>(){}.getType()
            );
            
            if (sequenceDataList == null) {
                return new ArrayList<>();
            }
            
            // First pass: create all sequences and shapes, build UUID map
            Map<UUID, ShapeInstance> idToShapeMap = new HashMap<>();
            List<ShapeSequence> sequences = new ArrayList<>();
            
            for (SequenceData seqData : sequenceDataList) {
                ShapeSequence sequence = new ShapeSequence(seqData.getName());
                sequence.setActive(seqData.isActive());
                sequence.setInvertAlignment(seqData.isInvertAlignment());
                
                // Create shapes and build UUID map
                for (SequenceData.ShapeInstanceData shapeData : seqData.getShapes()) {
                    ShapeInstance shape = createShapeFromData(shapeData);
                    sequence.addShape(shape);
                    idToShapeMap.put(shape.getId(), shape);
                }
                
                sequences.add(sequence);
            }
            
            // Second pass: resolve initial alignment references
            for (int i = 0; i < sequenceDataList.size(); i++) {
                SequenceData seqData = sequenceDataList.get(i);
                ShapeSequence sequence = sequences.get(i);
                
                if (seqData.getInitialAlignment() != null) {
                    SequenceData.InitialAlignmentData alignData = seqData.getInitialAlignment();
                    
                    if ("position".equals(alignData.getType())) {
                        if (alignData.getPosition() != null) {
                            sequence.setInitialAlignment(alignData.getPosition().toAlignPosition());
                        }
                    } else if ("shape".equals(alignData.getType())) {
                        if (alignData.getShapeId() != null) {
                            ShapeInstance linkedShape = idToShapeMap.get(alignData.getShapeId());
                            if (linkedShape != null) {
                                sequence.setInitialAlignment(linkedShape);
                            }
                        }
                    }
                }
            }
            
            return sequences;
        }
    }
    
    /**
     * Converts a ShapeSequence to SequenceData for serialization.
     */
    private SequenceData convertToData(ShapeSequence sequence) {
        SequenceData data = new SequenceData();
        data.setName(sequence.getName());
        data.setActive(sequence.isActive());
        data.setInvertAlignment(sequence.isInvertAlignment());
        
        // Convert shapes
        List<SequenceData.ShapeInstanceData> shapeDataList = new ArrayList<>();
        for (ShapeInstance shape : sequence.getShapes()) {
            shapeDataList.add(convertShapeToData(shape));
        }
        data.setShapes(shapeDataList);
        
        // Convert initial alignment
        if (sequence.getInitialAlignmentAsPosition() != null) {
            data.setInitialAlignment(SequenceData.InitialAlignmentData.fromPosition(
                sequence.getInitialAlignmentAsPosition()
            ));
        } else if (sequence.getInitialAlignmentAsShape() != null) {
            data.setInitialAlignment(SequenceData.InitialAlignmentData.fromShape(
                sequence.getInitialAlignmentAsShape()
            ));
        }
        
        return data;
    }
    
    /**
     * Converts a ShapeInstance to ShapeInstanceData for serialization.
     */
    private SequenceData.ShapeInstanceData convertShapeToData(ShapeInstance shape) {
        SequenceData.ShapeInstanceData data = new SequenceData.ShapeInstanceData();
        data.setId(shape.getId());
        data.setType(shape.getType());
        data.setKey(shape.getKey());
        data.setOrientation(shape.getOrientation());
        data.setActive(shape.isActive());
        data.setRed(shape.isRed());
        data.setForceInvertColor(shape.isForceInvertColor());
        
        if (shape instanceof AnnularSector) {
            AnnularSector sector = (AnnularSector) shape;
            data.setExternalDiameter(sector.getExternalDiameter());
            data.setAngleDegrees(sector.getAngleDegrees());
            data.setWidth(sector.getWidth());
        } else if (shape instanceof Rectangle) {
            Rectangle rect = (Rectangle) shape;
            data.setLength(rect.getLength());
            data.setWidth(rect.getWidth());
        }
        
        return data;
    }
    
    /**
     * Creates a ShapeInstance from ShapeInstanceData.
     */
    private ShapeInstance createShapeFromData(SequenceData.ShapeInstanceData shapeData) {
        ShapeInstance shape;
        
        if ("annular_sector".equals(shapeData.getType())) {
            shape = new AnnularSector(
                shapeData.getKey(),
                shapeData.getExternalDiameter(),
                shapeData.getAngleDegrees(),
                shapeData.getWidth()
            );
        } else if ("rectangle".equals(shapeData.getType())) {
            shape = new Rectangle(
                shapeData.getKey(),
                shapeData.getLength(),
                shapeData.getWidth()
            );
        } else {
            throw new IllegalArgumentException("Unknown shape type: " + shapeData.getType());
        }
        
        // Restore instance properties
        shape.setId(shapeData.getId());
        shape.setOrientation(shapeData.getOrientation());
        shape.setActive(shapeData.isActive());
        shape.setRed(shapeData.isRed());
        shape.setForceInvertColor(shapeData.isForceInvertColor());
        
        return shape;
    }
}

