# Track Draw

A Java application for drawing 2D geometric shapes and creating sequences by aligning them along common sides.

## Features

- Draw and manipulate 2D geometric shapes
- Align shapes by common sides to create sequences
- Move shapes using mouse interaction
- Store predefined shapes in configuration file
- Save results to file

## Building

```bash
mvn clean compile
```

## Running

```bash
mvn exec:java -Dexec.mainClass="com.trackdraw.Main"
```

## Project Structure

```
track_draw/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── trackdraw/
│   │   └── resources/
│   │       └── shapes.json
│   └── test/
│       └── java/
│           └── com/
│               └── trackdraw/
```

