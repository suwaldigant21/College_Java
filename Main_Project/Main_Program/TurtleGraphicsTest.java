/**
 * Test class for the TurtleGraphics implementation.
 * Initializes and demonstrates the TurtleGraphics functionality.
 */
public class TurtleGraphicsTest {
    /**
     * Main method to test the TurtleGraphics class.
     * @param args Command-line arguments (not used)
     */
    public static void main(String[] args) {
        System.out.println("Starting TurtleGraphics test...");
        
        // Create an instance of TurtleGraphics
        TurtleGraphics turtle = new TurtleGraphics("Turtle Graphics Test");
        
        // Demonstrate basic functionality
        turtle.demonstrateGraphics();
        
        System.out.println("TurtleGraphics test complete.");
    }
}