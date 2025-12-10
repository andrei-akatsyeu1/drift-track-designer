# Track Draw - User Guide

## About Track Draw

Track Draw is a Java application designed for creating and managing geometric shape sequences for **1/10 RC Drift car tracks**. 

### Purpose

The application helps you design tracks that are built from **3D printed parts**, similar to LEGO Duplo railway systems. Each track consists of modular pieces that connect together to form complete racing circuits.

### Key Concepts

- **Modular Design**: Tracks are built from standardized geometric shapes (annular sectors, rectangles, half-circles)
- **3D Printing**: All parts are designed to be 3D printed as physical components
- **Complex Shapes**: Some shape combinations form "complex shapes" that are printed as single integrated parts
- **Validation**: The application validates shape combinations to ensure:
  - Complex shapes can be printed without self-intersection
  - Parts can physically connect to other parts in the track system
- **3D Models**: Reference 3D models and part designs are available in the `3d_models/` directory

### How It Works

1. **Design**: Create sequences of shapes that represent track segments
2. **Link**: Connect sequences together to form complete track layouts
3. **Validate**: The application ensures all connections are physically possible
4. **Export**: Save your designs for 3D printing and manufacturing

Track Draw provides precise alignment, positioning, and validation tools to ensure your track designs are manufacturable and connect properly.

## Table of Contents

1. [Overview](#overview)
2. [Getting Started](#getting-started)
3. [User Interface](#user-interface)
4. [Working with Shapes](#working-with-shapes)
5. [Working with Sequences](#working-with-sequences)
6. [Keyboard Shortcuts](#keyboard-shortcuts)
7. [File Operations](#file-operations)
8. [Advanced Features](#advanced-features)

## Overview

Track Draw allows you to:
- **Design track layouts** for 1/10 RC Drift cars using modular geometric shapes
- Create sequences of geometric shapes (annular sectors, rectangles, half-circles)
- Align shapes automatically based on their geometry
- Link sequences together to create complex patterns
- **Validate complex shapes** to ensure they can be 3D printed as single parts without self-intersection
- Measure distances on the canvas
- Load background images for reference
- Generate reports on shape usage for manufacturing planning
- Save and load your work for 3D printing preparation

### 3D Models Reference

Physical 3D models and part designs are available in the `3d_models/` directory. These models serve as reference for:
- Understanding part geometry and connections
- Validating physical compatibility
- Planning 3D printing and assembly

The application's validation rules ensure that your digital designs match the physical constraints of the 3D printed parts.

## Getting Started

### Launching the Application

**Option 1: Using Maven**
```bash
mvn clean compile
mvn exec:java -Dexec.mainClass="com.trackdraw.Main"
```

**Option 2: Using Java directly**
```bash
mvn clean compile
java -cp target/classes com.trackdraw.Main
```

**Option 3: Using a JAR file** (if built)
```bash
java -jar track-draw.jar
```

### Initial Setup

When you first launch Track Draw, you'll see:
- A default "Main" sequence (active by default)
- Shape palettes at the top showing available shapes
- An empty canvas on the right
- Sequence management panel on the left

## User Interface

### Top Panel

**Global Scale Control**
- Text field showing current scale percentage
- `-` button: Decrease scale by 10%
- `+` button: Increase scale by 10%

**Background Image Scale Control**
- Text field showing current image scale percentage
- `-` button: Decrease image scale by 10%
- `+` button: Increase image scale by 10%

**Show Keys** (Checkbox)
- Checkbox to enable/disable displaying shape keys on the canvas
- When enabled, each shape displays its key (e.g., "05", "L", "E")
- Text color: black for white shapes, white for red shapes

**Measurement Tool** (📏)
- Checkbox to enable/disable the measurement tool
- When enabled, click two points on the canvas to measure distance

**Help Button** (?)
- Hover to see all keyboard shortcuts

### Left Panel

**Sequence Management**
- Text field: Enter name for new sequence
- `+` button: Add new empty sequence
- `+` button (with shape icon): Add new sequence starting from selected shape
- `🗑` button: Delete active sequence
- `↻` button: Toggle invert alignment (only for linked sequences)
- Sequence list: Shows all sequences, click to select

**Shape List**
- Shows all shapes in the active sequence
- Click a shape to select it (highlighted in green)
- `🗑` button: Clear all shapes from active sequence

### Right Panel

**Canvas**
- Main drawing area
- Displays all sequences and shapes
- Shows background image if loaded
- Click to focus for keyboard controls
- **Canvas Panning**: Hold left mouse button and drag to pan the entire canvas

**Status Bar** (Bottom)
- Shows status messages and notifications

## Working with Shapes

### Available Shapes

**Annular Sectors** (arc shapes):
- `05`: 50mm diameter, 45° angle
- `10`: 100mm diameter, 22.5° angle
- `15`: 150mm diameter, 15° angle
- `20`: 200mm diameter, 11.25° angle
- `25`: 250mm diameter, 9° angle
- `30`: 300mm diameter, 7.5° angle

**Rectangles**:
- `L`: 19mm length
- `L3`: 3mm length
- `L5`: 5mm length
- `L7`: 7mm length

**Half-Circle**:
- `E`: 6mm diameter (closing shape)

### Adding Shapes

1. **Select a sequence** from the sequence list (or use the default "Main" sequence)
2. **Click a shape button** in the palette:
   - Top palette: Shapes with orientation = 1
   - Bottom palette: Shapes with orientation = -1 (annular sectors only)
3. The shape is added to the active sequence and automatically aligned

### Shape Orientation

- Annular sectors can have orientation 1 or -1 (affects rotation)
- Rectangles always have orientation 1
- Use the bottom palette for negative orientation annular sectors

### Shape Colors

Shapes alternate between white/black and red:
- **White/Black**: Default color scheme
- **Red**: Alternate color scheme
- Colors alternate automatically as you add shapes
- Active shape is highlighted with a green contour

### Removing Shapes

- **Remove last shape**: Click the `-` button in the shape palette
- **Remove selected shape**: Select a shape in the list, then click `-`
- **Clear all shapes**: Click the `🗑` button below the shape list

### Inverting Shape Color

1. Select a shape in the shape list
2. Click the color inversion button (↻) in the shape palette
3. The shape's color will be inverted

## Working with Sequences

### Creating Sequences

**New Empty Sequence**:
1. Enter a name in the text field
2. Click the `+` button
3. The new sequence becomes active

**New Sequence from Shape**:
1. Select a shape in the current sequence
2. Click the `+` button with the shape icon
3. A new sequence is created, linked to the selected shape

### Selecting Sequences

- Click a sequence name in the sequence list
- The selected sequence becomes active (highlighted)
- Only one sequence can be active at a time

### Linking Sequences

When you create a sequence from a shape:
- The new sequence is **linked** to that shape
- Linked sequences follow their parent shape's position
- You can toggle **invert alignment** (↻ button) to flip the linked sequence 180°

**Linking Rules**:
- Shape `E` (half-circle) cannot have shapes added after it
- Certain shape combinations are allowed based on validation rules
- When `invertAlignment` is enabled, colors must differ between linked shapes

### Moving Sequences

**Active Sequence Only**:
- Use **Arrow Keys** to move by 10 pixels
- Use **Ctrl + Arrow Keys** to move by 1 pixel
- Only works if the sequence is not linked

**All Sequences**:
- Use **Shift + Arrow Keys** to move all unlinked sequences by 10 pixels
- Use **Shift + Ctrl + Arrow Keys** to move all unlinked sequences by 1 pixel
- Linked sequences are automatically excluded

### Rotating Sequences

- Use **+** or **-** keys to rotate by 10 degrees
- Use **Ctrl + +** or **Ctrl + -** to rotate by 1 degree
- Only rotates the active sequence (if not linked)

### Deleting Sequences

1. Select the sequence to delete
2. Click the `🗑` button in the sequence management panel
3. Confirm deletion if prompted

## Keyboard Shortcuts

### Sequence Movement (Active Sequence Only)

| Shortcut | Action |
|----------|--------|
| `Arrow Keys` | Move sequence by 10 pixels |
| `Ctrl + Arrow Keys` | Move sequence by 1 pixel |
| `Shift + Arrow Keys` | Move all unlinked sequences by 10 pixels |
| `Shift + Ctrl + Arrow Keys` | Move all unlinked sequences by 1 pixel |

### Sequence Rotation (Active Sequence Only)

| Shortcut | Action |
|----------|--------|
| `+` or `=` | Rotate clockwise by 10° |
| `-` | Rotate counter-clockwise by 10° |
| `Ctrl + +` or `Ctrl + =` | Rotate clockwise by 1° |
| `Ctrl + -` | Rotate counter-clockwise by 1° |

### Global Scale

| Shortcut | Action |
|----------|--------|
| `Mouse Wheel Up` | Increase scale by 10% |
| `Mouse Wheel Down` | Decrease scale by 10% |
| `Ctrl + Mouse Wheel Up` | Increase scale by 1% |
| `Ctrl + Mouse Wheel Down` | Decrease scale by 1% |

### Background Image Scale

| Shortcut | Action |
|----------|--------|
| `Page Up` | Increase image scale by 10% |
| `Page Down` | Decrease image scale by 10% |
| `Ctrl + Page Up` | Increase image scale by 1% |
| `Ctrl + Page Down` | Decrease image scale by 1% |

### Measurement Tool

| Shortcut | Action |
|----------|--------|
| `Esc` | Disable measurement tool |

### Tips

- Click on the canvas to ensure keyboard shortcuts work
- Keyboard shortcuts work even when other UI elements have focus
- Linked sequences cannot be moved/rotated with keyboard shortcuts

## File Operations

### Saving Sequences

1. Go to **File → Save Sequences...**
2. Navigate to the `saves/` directory (default)
3. Enter a filename (e.g., `my_track.json`)
4. Click Save

**What Gets Saved**:
- All sequences and their shapes
- Shape positions, orientations, and colors
- Sequence linking relationships
- Background image path (relative to `images/` directory)
- Background image scale

### Loading Sequences

1. Go to **File → Load Sequences...**
2. Navigate to the `saves/` directory
3. Select a JSON file
4. Click Open

**What Gets Loaded**:
- All sequences and shapes
- Background image (if present)
- All positions and relationships

### Background Images

**Loading an Image**:
1. Go to **File → Load Background Image...**
2. Navigate to the `images/` directory (default)
3. Select an image file (JPG, PNG, GIF, BMP)
4. Click Open

**Note**: If you load an image from outside the `images/` directory, it will be automatically copied there.

**Clearing an Image**:
- Go to **File → Clear Background Image**

**Image Scale**:
- Use the image scale control in the top panel
- Or use keyboard shortcuts: `Page Up/Down` (10%) or `Ctrl + Page Up/Down` (1%)

### Exporting Images

**Export to PNG**:
1. Go to **File → Export Image...**
2. Configure export options in the dialog:
   - **Show Background Image**: Include background image in export
   - **Show Shape Keys**: Display shape keys on exported image
   - **Include Shapes Report**: Add a table report at the bottom showing shape counts by type and color
   - **Scale**: Export scale multiplier (1x to 10x)
3. Click OK
4. Choose a location and filename for the PNG file
5. Click Save

**Export Features**:
- Exported images include all sequences with proper scaling
- Background image is included if enabled
- Shape keys are displayed if enabled (scaled appropriately)
- Shapes report shows a compact table format with shape counts
- Images are automatically sized to fit all content with padding

## Advanced Features

### Canvas Panning

You can pan (move) the entire canvas to navigate around your design:

1. **Hold left mouse button** on the canvas
2. **Drag** to move the canvas in any direction
3. **Release** to stop panning

**Note**: Panning is disabled when the measurement tool is active.

### Measurement Tool

The measurement tool allows you to measure distances on the canvas:

1. **Enable the tool**: Check the 📏 checkbox in the top panel
2. **Click first point**: Click where you want to start measuring
3. **Click second point**: Click where you want to end measuring
4. **View result**: The distance is displayed in scaled units on the canvas
5. **Disable**: Press `Esc` or uncheck the checkbox

The measurement shows:
- Distance in scaled units (based on global scale)
- A line connecting the two points
- A label showing the measurement value

### Shape Reports

Generate a report of all shapes used in your sequences:

1. Go to **Report → Generate Shape Report**
2. A window displays:
   - Count of each shape type
   - Complex shapes (linked shape pairs)
   - Color distribution

### File Structure

The application creates and uses these directories:

- `saves/`: JSON files containing sequence data
- `images/`: Background images (automatically managed)

Both directories are created automatically in your working directory.

### Sequence Validation

When linking sequences, the application validates:
- **Shape combinations are allowed**: Only certain shape pairs can form complex shapes that can be printed as single parts
- **Orientations match rules**: Shapes must have compatible orientations to ensure proper connection
- **Colors differ when using invert alignment**: Ensures visual distinction and proper part identification
- **Shape `E` cannot have shapes added after it**: The half-circle (`E`) is a closing shape that terminates a sequence

**Why Validation Matters**:
- **3D Printing**: Complex shapes must be printable as single parts without self-intersection
- **Physical Connection**: Parts must be able to physically connect to other parts in the track system
- **Manufacturing**: Validation ensures your designs can be successfully 3D printed and assembled

Validation errors are shown in the status bar. Refer to the `3d_models/` directory for physical part reference and compatibility information.

## Tips and Best Practices

1. **Organize Your Work**: Use descriptive sequence names
2. **Save Frequently**: Use File → Save Sequences regularly
3. **Use Keyboard Shortcuts**: They're faster than clicking buttons
4. **Background Images**: Load reference images to align your shapes accurately
5. **Measurement Tool**: Use it to verify distances and alignments
6. **Linked Sequences**: Create complex patterns by linking sequences together
7. **Scale Controls**: Adjust global and image scales independently for precise work

## Troubleshooting

**Arrow keys don't work**:
- Click on the canvas to give it focus
- Make sure the active sequence is not linked

**Shapes don't align correctly**:
- Check that shapes are added in the correct order
- Verify sequence linking relationships

**Image doesn't load**:
- Check that the image file is in a supported format (JPG, PNG, GIF, BMP)
- Verify file permissions

**Can't move a sequence**:
- Linked sequences cannot be moved with keyboard shortcuts
- Select an unlinked sequence to move it

## Support

For issues or questions, check the application's status bar for error messages and validation feedback.

---

**Version**: 1.0  
**Last Updated**: December 2024
