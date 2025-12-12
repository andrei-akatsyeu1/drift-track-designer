# Testing Guide for Track Draw

This guide explains how to test the Track Draw Java Swing application, covering both unit tests and UI tests.

## Table of Contents

1. [Testing Strategy](#testing-strategy)
2. [Testing Frameworks](#testing-frameworks)
3. [Running Tests](#running-tests)
4. [Writing UI Tests](#writing-ui-tests)
5. [Writing Unit Tests](#writing-unit-tests)
6. [Test Coverage](#test-coverage)
7. [Best Practices](#best-practices)

---

## Testing Strategy

### Three-Layer Testing Approach

1. **Unit Tests** - Test individual components in isolation
   - Business logic (models, validators, managers)
   - Utility classes
   - Controllers (with mocked dependencies)

2. **UI Component Tests** - Test Swing components individually
   - Panel components
   - Dialog components
   - Individual UI interactions

3. **Integration Tests** - Test full application workflows
   - End-to-end user workflows
   - File operations
   - Complete feature scenarios

---

## Testing Frameworks

### Dependencies

The project uses the following testing frameworks:

- **JUnit 5 (Jupiter)** - Test framework
  - `org.junit.jupiter:junit-jupiter:5.10.0`

- **AssertJ Swing** - UI testing framework
  - `org.assertj:assertj-swing-junit:3.17.1`
  - Provides robot for UI interactions
  - Component fixtures for easy access

- **Mockito** - Mocking framework
  - `org.mockito:mockito-core:5.5.0`
  - `org.mockito:mockito-junit-jupiter:5.5.0`
  - For mocking dependencies in unit tests

### Why AssertJ Swing?

- **Modern and Maintained** - Successor to FEST Assert
- **JUnit 5 Support** - Works seamlessly with JUnit Jupiter
- **Robot Pattern** - Simulates user interactions
- **Component Fixtures** - Easy component access and assertions
- **Headless Support** - Can run in CI/CD environments

---

## Running Tests

### Run All Tests

```bash
mvn test
```

### Run Specific Test Class

```bash
mvn test -Dtest=MainWindowUITest
```

### Run Tests Matching Pattern

```bash
mvn test -Dtest=*UITest
```

### Run with Coverage

```bash
mvn clean test
```

### Run in IDE

Most IDEs (IntelliJ IDEA, Eclipse) support running JUnit tests directly:
- Right-click on test class → Run
- Right-click on test method → Run
- Use keyboard shortcuts (Ctrl+Shift+F10 in IntelliJ)

---

## Writing UI Tests

### Base Test Class

All UI tests should extend `BaseUITest`:

```java
public class MyUITest extends BaseUITest {
    // robot and window are available
    // Setup and teardown handled automatically
}
```

### Example: Testing Button Clicks

```java
@Test
@DisplayName("Clicking button should perform action")
public void testButtonClick() {
    waitForUI();
    
    // Find button by name (requires component naming)
    JButtonFixture button = window.button("buttonName");
    
    // Click button
    button.click();
    
    // Verify result
    // ...
}
```

### Example: Testing Text Input

```java
@Test
@DisplayName("Text field should accept input")
public void testTextInput() {
    waitForUI();
    
    // Find text field
    JTextComponentFixture textField = window.textBox("textFieldName");
    
    // Enter text
    textField.enterText("Test Sequence");
    
    // Verify text
    textField.requireText("Test Sequence");
}
```

### Example: Testing Checkboxes

```java
@Test
@DisplayName("Checkbox should toggle state")
public void testCheckbox() {
    waitForUI();
    
    // Find checkbox
    JCheckBoxFixture checkbox = window.checkBox("checkboxName");
    
    // Verify initial state
    checkbox.requireNotSelected();
    
    // Click to select
    checkbox.click();
    
    // Verify selected
    checkbox.requireSelected();
}
```

### Example: Testing Component Visibility

```java
@Test
@DisplayName("Component should be visible")
public void testVisibility() {
    waitForUI();
    
    // Find component
    JPanelFixture panel = window.panel("panelName");
    
    // Verify visible
    panel.requireVisible();
}
```

### Finding Components

Components can be found by:
1. **Name** - Set with `component.setName("componentName")`
2. **Type** - Use robot finder methods
3. **Text** - For buttons, labels with text
4. **Tooltip** - For components with tooltips

**Important:** Add names to components for easier testing:

```java
// In your component class
button.setName("increaseScaleButton");
textField.setName("sequenceNameField");
```

---

## Writing Unit Tests

### Example: Testing Business Logic

```java
@Test
@DisplayName("Shape validation should reject invalid combinations")
public void testShapeValidation() {
    // Arrange
    ShapeInstance shape1 = new ShapeInstance(...);
    ShapeInstance shape2 = new ShapeInstance(...);
    SequenceValidator validator = new SequenceValidator();
    
    // Act
    ValidationResult result = validator.validateLink(shape1, shape2, false);
    
    // Assert
    assertThat(result.isValid()).isFalse();
    assertThat(result.getErrorMessage()).contains("different orientations");
}
```

### Example: Testing with Mocks

```java
@ExtendWith(MockitoExtension.class)
class SequenceManagerTest {
    
    @Mock
    private FileManager fileManager;
    
    @InjectMocks
    private SequenceManager sequenceManager;
    
    @Test
    @DisplayName("Should save sequences to file")
    public void testSaveSequences() throws IOException {
        // Arrange
        List<ShapeSequence> sequences = Arrays.asList(...);
        when(fileManager.getSavesDirectory()).thenReturn(new File("saves"));
        
        // Act
        sequenceManager.saveSequences(sequences, null);
        
        // Assert
        verify(fileManager).getSavesDirectory();
        // Verify file was created
    }
}
```

---

## Test Coverage

### Requirements Coverage

Tests should cover all requirements from `BUSINESS_REQUIREMENTS.md`:

#### High Priority (Core Functionality)
- ✅ REQ-002: Shape Types
- ✅ REQ-006: Sequence Creation
- ✅ REQ-009: Validation Purpose
- ✅ REQ-012: Global Scale
- ✅ REQ-013: Background Image Scale
- ✅ REQ-018: Distance Measurement
- ✅ REQ-020: Save Sequences
- ✅ REQ-021: Load Sequences

#### Medium Priority (User Interactions)
- REQ-007: Sequence Linking
- REQ-008: Sequence Movement/Rotation
- REQ-010: Validation Rules
- REQ-016: Image Loading
- REQ-022: Image Export

#### Lower Priority (UI Polish)
- REQ-019: Shape Key Labels
- REQ-025: Layout
- REQ-028: Keyboard Shortcuts

### Test Structure

```
src/test/java/com/trackdraw/
├── model/              # Model unit tests
│   ├── ShapeInstanceTest.java
│   ├── ShapeSequenceTest.java
│   └── ...
├── config/             # Config unit tests
│   ├── SequenceManagerTest.java
│   ├── ShapeConfigTest.java
│   └── ...
├── validation/         # Validation unit tests
│   └── SequenceValidatorTest.java
├── view/               # UI tests
│   ├── BaseUITest.java
│   ├── MainWindowUITest.java
│   ├── ScaleControlUITest.java
│   └── ...
└── integration/        # Integration tests
    ├── FileOperationsTest.java
    └── WorkflowTest.java
```

---

## Best Practices

### 1. Test Naming

Use descriptive test names with `@DisplayName`:

```java
@Test
@DisplayName("Clicking increase scale button should increase scale by 10%")
public void testIncreaseScaleButton() {
    // ...
}
```

### 2. Arrange-Act-Assert Pattern

Structure tests clearly:

```java
@Test
public void testSomething() {
    // Arrange - Set up test data
    double initialScale = GlobalScale.getScale();
    
    // Act - Perform action
    button.click();
    
    // Assert - Verify result
    assertThat(GlobalScale.getScale()).isGreaterThan(initialScale);
}
```

### 3. Use waitForUI() for Async Operations

Swing operations may be asynchronous:

```java
@Test
public void testAsyncOperation() {
    button.click();
    waitForUI(); // Wait for UI to update
    // Now verify
}
```

### 4. Clean Up Resources

BaseUITest handles cleanup, but for custom resources:

```java
@AfterEach
public void cleanup() {
    // Clean up test files, etc.
}
```

### 5. Test Isolation

Each test should be independent:

```java
@BeforeEach
public void setUp() {
    // Reset state before each test
    GlobalScale.setScale(1.0);
}
```

### 6. Mock External Dependencies

Don't test file system, network, etc. in unit tests:

```java
@Mock
private FileManager fileManager;

@Test
public void testWithoutFileSystem() {
    when(fileManager.getSavesDirectory()).thenReturn(mockFile);
    // Test logic without touching file system
}
```

### 7. Test Error Cases

Don't just test happy paths:

```java
@Test
@DisplayName("Should handle invalid file gracefully")
public void testInvalidFile() {
    // Test error handling
    assertThatThrownBy(() -> {
        sequenceManager.loadSequences("invalid.json");
    }).isInstanceOf(IOException.class);
}
```

### 8. Use AssertJ Assertions

Prefer AssertJ over JUnit assertions:

```java
// Good
assertThat(scale).isGreaterThan(1.0);
assertThat(sequences).hasSize(2);
assertThat(message).contains("error");

// Less readable
assertTrue(scale > 1.0);
assertEquals(2, sequences.size());
assertTrue(message.contains("error"));
```

---

## Common Testing Scenarios

### Testing Keyboard Shortcuts

```java
@Test
@DisplayName("Arrow keys should move sequence")
public void testArrowKeyMovement() {
    waitForUI();
    
    // Focus canvas
    window.panel("drawingPanel").focus();
    
    // Press arrow key
    robot.pressAndReleaseKey(KeyEvent.VK_RIGHT);
    waitForUI();
    
    // Verify sequence moved
    // ...
}
```

### Testing File Dialogs

File dialogs require special handling:

```java
@Test
@DisplayName("Save dialog should appear")
public void testSaveDialog() {
    waitForUI();
    
    // Click save menu item
    window.menuItem("Save Sequences...").click();
    
    // File dialog appears - handle it
    // Note: May need to use FileChooserFixture
    // ...
}
```

### Testing Canvas Interactions

```java
@Test
@DisplayName("Canvas should respond to mouse clicks")
public void testCanvasClick() {
    waitForUI();
    
    // Click on canvas
    window.panel("drawingPanel").click();
    
    // Verify interaction
    // ...
}
```

---

## Troubleshooting

### Tests Fail in Headless Mode

If tests fail in CI/CD (headless mode):

```java
// In BaseUITest or test class
@BeforeEach
public void setUp() {
    // Ensure not headless (if needed)
    System.setProperty("java.awt.headless", "false");
    // ...
}
```

### Components Not Found

If components can't be found:
1. Add names to components: `component.setName("name")`
2. Use robot finder methods
3. Check component hierarchy
4. Use `waitForUI()` for async components

### Tests Are Flaky

If tests are inconsistent:
1. Add `waitForUI()` calls
2. Use explicit waits instead of fixed delays
3. Ensure proper cleanup between tests
4. Check for race conditions

---

## Continuous Integration

### GitHub Actions Example

```yaml
name: Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - uses: actions/setup-java@v2
        with:
          java-version: '11'
      - run: mvn clean test
```

### Running Tests in CI

```bash
# Set display for headless mode (Linux)
export DISPLAY=:99
Xvfb :99 -screen 0 1024x768x24 &

# Run tests
mvn clean test
```

---

## Additional Resources

- [AssertJ Swing Documentation](https://assertj.github.io/doc/#assertj-swing-getting-started)
- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)

---

**Last Updated:** December 12, 2025

