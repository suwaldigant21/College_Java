import java.util.Scanner;

/**
 * MainClass demonstrates the usage of the TurtleGraphics class.
 */
public class MainClass {
    /**
     * Main method to initialize and test the TurtleGraphics functionality.
     * @param args Command-line arguments (not used)
     */
    public static void main(String[] args) {
        System.out.println("Starting TurtleGraphics demonstration...");
        
        // Create an instance of TurtleGraphics with a title for the window
        TurtleGraphics turtle = new TurtleGraphics("Turtle Graphics Demo");
        
        // Set up console command input
        Scanner scanner = new Scanner(System.in);
        String command = "";
        
        System.out.println("Enter commands (type 'exit' to quit):");
        
        // Process commands until user types "exit"
        while (!command.equalsIgnoreCase("exit")) {
            System.out.print("> ");
            command = scanner.nextLine().trim();
            
            if (command.equalsIgnoreCase("about")) {
                System.out.println("Executing 'about' command...");
                turtle.about();
            } else if (command.equalsIgnoreCase("exit")) {
                System.out.println("Exiting program...");
            } else if (!command.isEmpty()) {
                System.out.println("Unknown command: " + command);
            }
        }
        
        scanner.close();
        System.out.println("TurtleGraphics demonstration complete.");
    }
}