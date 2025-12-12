# Track Draw - Business Requirements Document

**Document Version:** 1.0  
**Last Updated:** December 12, 2025  
**Based on:** Chat history analysis (2025-12-05 to 2025-12-11) and README.md

---

## 1. Executive Summary

### 1.1 Purpose
Track Draw is a Java desktop application designed for creating and managing geometric shape sequences for **1/10 RC Drift car tracks**. The application enables users to design tracks built from **3D printed modular parts**, similar to LEGO Duplo railway systems.

### 1.2 Business Context
- **Target Users:** RC drift car enthusiasts and track designers
- **Primary Use Case:** Design modular track layouts for 1/10 scale RC drift cars
- **Manufacturing Method:** All parts are 3D printed as physical components
- **Design Philosophy:** Modular design with standardized geometric shapes that connect together

### 1.3 Key Business Value
- Enables precise track design with automatic shape alignment
- Validates designs to ensure 3D printability and physical connectivity
- Supports complex shape creation (multiple shapes printed as single parts)
- Provides manufacturing reports for production planning

---

## 2. Core Business Requirements

### 2.1 Application Purpose
**REQ-001: Track Design Application**
- The application MUST enable users to design tracks for 1/10 RC Drift cars
- Tracks MUST be built from modular 3D printed parts
- The system MUST support creating sequences of geometric shapes that represent track segments
- Reference 3D models MUST be available in the `3d_models/` directory

**Source:** Chat history 2025-12-09 07:20Z (latest requirement clarification)

### 2.2 Shape Management

**REQ-002: Shape Types**
The application MUST support three types of geometric shapes:

1. **Annular Sectors** (arc shapes):
   - `05`: 50cm diameter, 45° angle
   - `10`: 100cm diameter, 22.5° angle
   - `15`: 150cm diameter, 15° angle
   - `20`: 200cm diameter, 11.25° angle
   - `25`: 250cm diameter, 9° angle
   - `30`: 300cm diameter, 7.5° angle

2. **Rectangles**:
   - `L`: 19cm length
   - `L3`: 3cm length
   - `L5`: 5cm length
   - `L7`: 7cm length

3. **Half-Circle** (closing shape):
   - `E`: 6cm diameter
   - MUST be a closing shape that terminates a sequence
   - Cannot have shapes added after it

**Source:** Chat history 2025-12-08 06:13Z (half-circle addition)

**REQ-003: Shape Orientation**
- Annular sectors MUST support orientation 1 or -1 (affects rotation)
- Rectangles MUST always have orientation 1
- Shape orientation MUST be displayed in separate palettes (top: orientation 1, bottom: orientation -1)

**REQ-004: Shape Colors**
- Shapes MUST alternate between white/black and red color schemes
- Colors MUST alternate automatically as shapes are added
- Active shape MUST be highlighted with a green contour
- Shape keys MUST display in black for white shapes, white for red shapes

**REQ-005: Shape Alignment**
- Shapes MUST automatically align based on their geometry when added to a sequence
- Each shape MUST have an alignment position calculated from the previous shape
- The half-circle (`E`) MUST be aligned by its diameter

### 2.3 Sequence Management

**REQ-006: Sequence Creation**
- Users MUST be able to create new empty sequences with custom names
- Users MUST be able to create sequences linked to a selected shape
- A default "Main" sequence MUST be created on application startup
- Only one sequence can be active at a time

**REQ-007: Sequence Linking**
- Sequences CAN be linked to shapes in other sequences
- Linked sequences MUST follow their parent shape's position
- Linked sequences MUST support invert alignment (180° flip)
- When `invertAlignment` is enabled, the first shape color MUST be opposite to the linked shape color

**Source:** Chat history 2025-12-09 07:20Z (color requirement clarification)

**REQ-008: Sequence Movement and Rotation**
- Users MUST be able to move the active sequence using arrow keys (10px) or Ctrl+Arrow (1px)
- Users MUST be able to move all unlinked sequences using Shift+Arrow (10px) or Shift+Ctrl+Arrow (1px)
- Users MUST be able to rotate the active sequence using +/- keys (10°) or Ctrl+/- (1°)
- Linked sequences MUST NOT be movable/rotatable with keyboard shortcuts

### 2.4 Complex Shape Validation

**REQ-009: Validation Purpose**
- The application MUST validate shape combinations to ensure:
  - Complex shapes can be printed as single parts without self-intersection
  - Parts can physically connect to other parts in the track system
  - Designs match physical constraints of 3D printed parts

**Source:** Chat history 2025-12-09 07:20Z (business context clarification)

**REQ-010: Validation Rules**

**Rule 1: Shape E Restriction**
- Shape `E` (half-circle) MUST NOT have shapes added after it

**Rule 2: Orientation Requirements (invertAlignment = false)**
- For specific allowed combinations (defined in `allowed_combinations.csv`):
  - Shapes MUST have different orientations (1 vs -1)
- For all other shape pairs:
  - Shapes MUST have different orientations (1 vs -1)
- Validation MUST compare shape pairs regardless of which is linked and which is first

**Source:** Chat history 2025-12-10 09:04Z (orientation comparison fix)

**Rule 3: Color Requirements (invertAlignment = true)**
- Any combination is allowed, BUT
- Colors MUST differ between linked shape and first shape
- When invertAlignment is enabled, first shape color MUST be opposite to linked shape color

**Source:** Chat history 2025-12-09 07:20Z (color requirement)

**REQ-011: Validation Feedback**
- Validation errors MUST be displayed in the status bar
- Users MUST be prevented from creating invalid shape combinations

### 2.5 Scaling and Viewing

**REQ-012: Global Scale**
- The application MUST support global scale control (affects all shapes and sequences)
- Global scale MUST be adjustable via mouse wheel:
  - Mouse wheel up: Increase scale by 10%
  - Mouse wheel down: Decrease scale by 10%
  - Ctrl + Mouse wheel up: Increase scale by 1%
  - Ctrl + Mouse wheel down: Decrease scale by 1%
- Global scale MUST also be controllable via UI buttons (+/- 10%)
- Scale MUST be displayed as percentage in a text field

**Source:** Chat history 2025-12-10 09:04Z (mouse wheel requirement, replaced keyboard shortcuts)

**REQ-013: Background Image Scale**
- Background images MUST support independent scale control
- Image scale MUST be multiplied by global scale when drawing (totalImageScale = globalScale * imageScale)
- Image scale MUST be adjustable via keyboard:
  - Page Up: Increase image scale by 10%
  - Page Down: Decrease image scale by 10%
  - Ctrl + Page Up: Increase image scale by 1%
  - Ctrl + Page Down: Decrease image scale by 1%
- Image scale MUST also be controllable via UI buttons (+/- 10%)
- Scale MUST be displayed as percentage in a text field

**Source:** Chat history 2025-12-08 06:13Z (globalScale * imageScale requirement), 2025-12-10 09:04Z (keyboard shortcuts)

**REQ-014: Relative Position Maintenance**
- Background images and sequences MUST maintain their relative positions when:
  - Global scale changes
  - Window is resized
- The system MUST NOT anchor images to (0,0) but preserve relative positioning

**Source:** Chat history 2025-12-08 06:13Z (position maintenance requirement)

**REQ-015: Canvas Panning**
- Users MUST be able to pan the canvas by holding left mouse button and dragging
- Panning MUST move both background image and all sequences together
- Panning MUST be disabled when measurement tool is active

**Source:** Chat history 2025-12-10 09:04Z (canvas panning requirement)

### 2.6 Background Images

**REQ-016: Image Loading**
- Users MUST be able to load background images via File menu
- Supported formats: JPG, PNG, GIF, BMP
- Images loaded from outside `images/` directory MUST be automatically copied to `images/` directory
- Images MUST be assigned unique names if needed (timestamp-based)
- Image path MUST be stored relative to `images/` directory

**Source:** Chat history 2025-12-08 06:13Z (image copying requirement)

**REQ-017: Image Management**
- Users MUST be able to clear background images via File menu
- Background image scale MUST reset to 1.0 (100%) when image is cleared
- Background images MUST be saved/loaded with sequence data

### 2.7 Measurement Tool

**REQ-018: Distance Measurement**
- Users MUST be able to measure distances on the canvas
- Measurement tool MUST be enabled/disabled via checkbox in top panel
- Users MUST click two points to measure distance
- Measurement MUST display:
  - Distance in scaled units (based on global scale)
  - A line connecting the two points
  - A label showing the measurement value
- Measurement tool MUST account for canvas pan offset
- Users MUST be able to disable measurement tool via Esc key

**Source:** Chat history 2025-12-08 06:13Z (ruler/measurement tool requirement)

### 2.8 Shape Keys Display

**REQ-019: Shape Key Labels**
- Users MUST be able to toggle display of shape keys on canvas
- When enabled, each shape MUST display its key (e.g., "05", "L", "E")
- Text color MUST be black for white shapes, white for red shapes
- Shape keys MUST be included in image exports when enabled

**Source:** Chat history 2025-12-09 07:20Z (show keys checkbox requirement)

### 2.9 File Operations

**REQ-020: Save Sequences**
- Users MUST be able to save all sequences to JSON files
- Save dialog MUST default to `saves/` directory
- Saved data MUST include:
  - All sequences and their shapes
  - Shape positions, orientations, and colors
  - Sequence linking relationships
  - Background image path (relative to `images/` directory)
  - Background image scale

**REQ-021: Load Sequences**
- Users MUST be able to load sequences from JSON files
- Load dialog MUST default to `saves/` directory
- Loading MUST restore:
  - All sequences and shapes
  - Background image (if present)
  - All positions and relationships
- Application MUST support backward compatibility with older JSON formats

**REQ-022: Image Export**
- Users MUST be able to export canvas to PNG images
- Export dialog MUST allow configuration of:
  - Show Background Image (checkbox)
  - Show Shape Keys (checkbox)
  - Include Shapes Report (checkbox)
  - Scale multiplier (1x to 10x)
- Exported images MUST include all sequences with proper scaling
- Shape reports MUST display as a compact table format with shape counts
- Images MUST be automatically sized to fit all content with padding

**Source:** Chat history 2025-12-11 08:30Z (export settings requirement)

**REQ-023: Default Export Settings**
- Export dialog MUST remember last used settings:
  - Show Background Image: ON (default)
  - Show Shape Keys: OFF (default)
  - Include Shapes Report: OFF (default)
  - Scale: 1x (default)

**Source:** Chat history 2025-12-11 08:30Z (default settings requirement)

### 2.10 Reporting

**REQ-024: Shape Reports**
- Users MUST be able to generate reports on shape usage
- Reports MUST display:
  - Count of each shape type
  - Complex shapes (linked shape pairs)
  - Color distribution
- Reports MUST support manufacturing planning use cases

### 2.11 User Interface

**REQ-025: Layout**
- Application MUST have:
  - Top panel: Global scale control, image scale control, show keys checkbox, measurement tool checkbox, help button
  - Left panel: Sequence management, shape list
  - Right panel: Canvas (main drawing area)
  - Bottom: Status bar

**REQ-026: Help System**
- Help button (?) MUST display keyboard shortcuts on hover
- Keyboard shortcuts documentation MUST be kept up-to-date

**REQ-027: Status Bar**
- Status bar MUST display:
  - Status messages and notifications
  - Validation errors
  - Operation feedback
- Status messages MUST have configurable timeout (short: 3s, long: 5s)

### 2.12 Keyboard Shortcuts

**REQ-028: Keyboard Controls**
The application MUST support the following keyboard shortcuts:

**Sequence Movement (Active Sequence Only):**
- Arrow Keys: Move sequence by 10 pixels
- Ctrl + Arrow Keys: Move sequence by 1 pixel
- Shift + Arrow Keys: Move all unlinked sequences by 10 pixels
- Shift + Ctrl + Arrow Keys: Move all unlinked sequences by 1 pixel

**Sequence Rotation (Active Sequence Only):**
- `+` or `=`: Rotate clockwise by 10°
- `-`: Rotate counter-clockwise by 10°
- Ctrl + `+` or Ctrl + `=`: Rotate clockwise by 1°
- Ctrl + `-`: Rotate counter-clockwise by 1°

**Global Scale:**
- Mouse Wheel Up: Increase scale by 10%
- Mouse Wheel Down: Decrease scale by 10%
- Ctrl + Mouse Wheel Up: Increase scale by 1%
- Ctrl + Mouse Wheel Down: Decrease scale by 1%

**Background Image Scale:**
- Page Up: Increase image scale by 10%
- Page Down: Decrease image scale by 10%
- Ctrl + Page Up: Increase image scale by 1%
- Ctrl + Page Down: Decrease image scale by 1%

**Measurement Tool:**
- Esc: Disable measurement tool

**Source:** Chat history 2025-12-10 09:04Z (latest keyboard shortcut requirements)

---

## 3. Technical Requirements

### 3.1 Platform
- **REQ-029:** Application MUST be built in Java
- **REQ-030:** Application MUST use Java Swing for UI
- **REQ-031:** Application MUST be built with Maven
- **REQ-032:** Application MUST support Java 11+

### 3.2 Data Storage
- **REQ-033:** Sequences MUST be stored in JSON format
- **REQ-034:** Shape configurations MUST be stored in JSON (`shapes.json`)
- **REQ-035:** Allowed shape combinations MUST be stored in CSV (`allowed_combinations.csv`)
- **REQ-036:** Application MUST create and manage directories:
  - `saves/`: JSON files containing sequence data
  - `images/`: Background images (automatically managed)
  - `export/`: Exported images

### 3.3 File Management
- **REQ-037:** Application MUST handle file paths relative to working directory
- **REQ-038:** Images loaded from outside `images/` directory MUST be copied automatically
- **REQ-039:** File operations MUST handle UTF-8 encoding

---

## 4. Non-Functional Requirements

### 4.1 Usability
- **REQ-040:** Application MUST provide intuitive UI for track design
- **REQ-041:** Keyboard shortcuts MUST work even when UI elements have focus
- **REQ-042:** Application MUST provide clear feedback for all user actions

### 4.2 Performance
- **REQ-043:** Application MUST handle multiple sequences efficiently
- **REQ-044:** Canvas rendering MUST be smooth during panning and scaling

### 4.3 Reliability
- **REQ-045:** Application MUST validate data before saving
- **REQ-046:** Application MUST handle file errors gracefully
- **REQ-047:** Application MUST support backward compatibility with older save formats

---

## 5. Requirements Evolution Timeline

### 2025-12-05: Initial Project Setup
- Basic Java 2D shape drawing application
- Shape sequence management
- Automatic shape alignment

### 2025-12-08: Core Features Added
- Background image support with scale control
- Image scale multiplication with global scale (globalScale * imageScale)
- Measurement/ruler tool
- Half-circle shape (E) added as closing shape
- Image auto-copy to images/ directory
- Relative position maintenance on scale/window resize

### 2025-12-09: Validation and UI Enhancements
- Business context clarification: 1/10 RC Drift car tracks, 3D printed parts
- Complex shape validation rules refined
- Color requirements for invertAlignment clarified
- Show keys checkbox added
- README documentation created

### 2025-12-10: Interaction Improvements
- Canvas panning with mouse drag
- Keyboard shortcuts changed: Mouse wheel for global scale (replaced Page Up/Down)
- Page Up/Down freed for image scale control
- Measurement tool pan offset handling

### 2025-12-11: Export Enhancements
- Default export settings implementation
- Export settings persistence

---

## 6. Out of Scope

The following are explicitly NOT part of the current requirements:
- 3D model generation/editing
- Direct 3D printer communication
- Online collaboration features
- Version control integration
- Undo/redo functionality (not explicitly required)
- Shape editing after creation (shapes are added/removed, not edited)

---

## 7. Assumptions and Dependencies

### Assumptions
- Users have access to 3D printing capabilities
- Reference 3D models are available in `3d_models/` directory
- Users understand basic geometric concepts
- Java runtime environment is available

### Dependencies
- Java 11 or higher
- Maven build system
- Java Swing libraries
- Gson library for JSON processing
- Standard Java image I/O libraries

---

## 8. Success Criteria

The application is considered successful when:
1. Users can design complete track layouts using modular shapes
2. Validation prevents creation of non-printable or non-connectable parts
3. Designs can be saved and loaded reliably
4. Background images assist in accurate shape alignment
5. Exported images support manufacturing planning
6. Shape reports enable production planning

---

**Document End**

