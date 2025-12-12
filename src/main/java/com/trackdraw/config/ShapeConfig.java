package com.trackdraw.config;

import com.trackdraw.model.AnnularSector;
import com.trackdraw.model.Rectangle;
import com.trackdraw.model.HalfCircle;
import com.trackdraw.model.ShapeInstance;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages shape configuration loading and saving.
 */
public class ShapeConfig {
    private static final String CONFIG_FILE = "shapes.json";
    private Map<String, ShapeInstance> shapes;
    private List<ShapeInstance> shapesInOrder; // Preserve order from JSON
    private Gson gson;
    
    public ShapeConfig() {
        this.shapes = new LinkedHashMap<>(); // Use LinkedHashMap to preserve insertion order
        this.shapesInOrder = new ArrayList<>();
        this.gson = createGson();
    }
    
    /**
     * Creates a Gson instance with custom serializers/deserializers for Shape interface.
     */
    private Gson createGson() {
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();
        
        // Custom serializer for ShapeInstance
        builder.registerTypeAdapter(ShapeInstance.class, new JsonSerializer<ShapeInstance>() {
            @Override
            public JsonElement serialize(ShapeInstance src, Type typeOfSrc, JsonSerializationContext context) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("type", src.getType());
                jsonObject.addProperty("key", src.getKey());
                
                if (src instanceof AnnularSector) {
                    AnnularSector as = (AnnularSector) src;
                    jsonObject.addProperty("externalDiameter", as.getExternalDiameter());
                    jsonObject.addProperty("angleDegrees", as.getAngleDegrees());
                    jsonObject.addProperty("width", as.getWidth());
                } else if (src instanceof Rectangle) {
                    Rectangle r = (Rectangle) src;
                    jsonObject.addProperty("length", r.getLength());
                    jsonObject.addProperty("width", r.getWidth());
                } else if (src instanceof HalfCircle) {
                    HalfCircle hc = (HalfCircle) src;
                    jsonObject.addProperty("diameter", hc.getDiameter());
                }
                
                return jsonObject;
            }
        });
        
        // Custom deserializer for ShapeInstance
        builder.registerTypeAdapter(ShapeInstance.class, new JsonDeserializer<ShapeInstance>() {
            @Override
            public ShapeInstance deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                    throws JsonParseException {
                JsonObject jsonObject = json.getAsJsonObject();
                String type = jsonObject.get("type").getAsString();
                String key = jsonObject.get("key").getAsString();
                
                if ("annular_sector".equals(type)) {
                    double externalDiameter = jsonObject.get("externalDiameter").getAsDouble();
                    double angleDegrees = jsonObject.get("angleDegrees").getAsDouble();
                    double width = jsonObject.get("width").getAsDouble();
                    return new AnnularSector(key, externalDiameter, angleDegrees, width);
                } else if ("rectangle".equals(type)) {
                    double length = jsonObject.get("length").getAsDouble();
                    double width = jsonObject.get("width").getAsDouble();
                    return new Rectangle(key, length, width);
                } else if ("half_circle".equals(type)) {
                    double diameter = jsonObject.get("diameter").getAsDouble();
                    return new HalfCircle(key, diameter);
                }
                
                throw new JsonParseException("Unknown shape type: " + type);
            }
        });
        
        return builder.create();
    }
    
    /**
     * Loads shapes from the configuration file.
     */
    public void loadShapes() throws IOException {
        // Try to load from classpath first (for packaged JAR)
        java.io.InputStream inputStream = getClass().getClassLoader().getResourceAsStream(CONFIG_FILE);
        
        if (inputStream == null) {
            // Fallback to file system (for development)
            File configFile = new File("src/main/resources", CONFIG_FILE);
            if (!configFile.exists()) {
                System.out.println("Config file not found, creating default...");
                return;
            }
            inputStream = new java.io.FileInputStream(configFile);
        }
        
        try (Reader reader = new java.io.InputStreamReader(inputStream, "UTF-8")) {
            JsonObject root = gson.fromJson(reader, JsonObject.class);
            if (root.has("shapes")) {
                // Use ArrayList to preserve order from JSON
                List<ShapeInstance> shapeList = gson.fromJson(
                    root.get("shapes"),
                    new TypeToken<ArrayList<ShapeInstance>>(){}.getType()
                );
                
                shapes.clear();
                shapesInOrder.clear();
                // Preserve exact order from JSON
                for (ShapeInstance shape : shapeList) {
                    shapes.put(shape.getKey(), shape);
                    shapesInOrder.add(shape);
                }
            }
        }
    }

    /**
     * Adds a shape to the configuration.
     */
    public void addShape(ShapeInstance shape) {
        shapes.put(shape.getKey(), shape);
    }
    
    /**
     * Gets a shape by its key.
     */
    public ShapeInstance getShape(String key) {
        return shapes.get(key);
    }
    
    /**
     * Gets all shapes.
     */
    public Map<String, ShapeInstance> getAllShapes() {
        return new HashMap<>(shapes);
    }
    
    /**
     * Gets all shapes in the order they appear in the JSON file.
     */
    public List<ShapeInstance> getShapesInOrder() {
        return new ArrayList<>(shapesInOrder);
    }
}

