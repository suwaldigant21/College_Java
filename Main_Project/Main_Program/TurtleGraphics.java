import uk.ac.leedsbeckett.oop.LBUGraphics;
import javax.swing.*;
import java.awt.FlowLayout;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

public class TurtleGraphics extends LBUGraphics {
    
    // Track initial position for reset command
    private int initialX;
    private int initialY;
    
    // Track command history and unsaved state
    private List<String> commandHistory = new ArrayList<>();
    private boolean hasUnsavedCommands = false;
    private boolean hasUnsavedImage = false;
    
    // Reference to the main frame for dialogs
    private JFrame mainFrame;
    private JTextArea historyTextArea;
    
    // Window closing handler as a named class to avoid ClassNotFoundError
    private class WindowCloseHandler extends java.awt.event.WindowAdapter {
        @Override
        public void windowClosing(java.awt.event.WindowEvent windowEvent) {
            exitApplication();
        }
    }
    
    public TurtleGraphics(String title) {
        super();
        
        // Set up the JFrame
        mainFrame = new JFrame(title);                
        mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        
        // Use named class instead of anonymous class
        mainFrame.addWindowListener(new WindowCloseHandler());
        
        // Create layout with command history
        JPanel mainPanel = new JPanel(new FlowLayout());
        historyTextArea = new JTextArea(20, 30);
        historyTextArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(historyTextArea);
        
        mainPanel.add(this);
        mainPanel.add(scrollPane);
        
        mainFrame.setContentPane(mainPanel);
        mainFrame.pack();                                               
        mainFrame.setVisible(true);
        
        // Initialize
        initialSetup();
    }
    
    @Override
    public void about() {
        super.about();
        displayMessage("TurtleGraphics v6.0 - Digant Suwal");
    }
    
    private void initialSetup() {
        initialX = getWidth() / 2;
        initialY = getHeight() / 2;
        reset();
    }
    
    @Override
    public void processCommand(String command) {
        if (command == null || command.isEmpty()) {
            displayMessage("Error: Please enter a command");
            return;
        }
        
        String lowerCmd = command.toLowerCase().trim();
        boolean commandSuccessful = false;
        
        try {
            // Background command needs to be near top to be recognized properly
            if (lowerCmd.equals("background")) {
                setBackground_Col(Color.LIGHT_GRAY);
                clear();
                displayMessage("Background set to light gray");
                commandSuccessful = true;
            } else if (lowerCmd.startsWith("pencolour ")) {
                try {
                    String[] rgb = lowerCmd.substring("pencolour ".length()).trim().split(",");
                    if (rgb.length == 3) {
                        int r = Integer.parseInt(rgb[0].trim());
                        int g = Integer.parseInt(rgb[1].trim());
                        int b = Integer.parseInt(rgb[2].trim());
                        
                        if (r >= 0 && r <= 255 && g >= 0 && g <= 255 && b >= 0 && b <= 255) {
                            setPenColour(new Color(r, g, b));
                            displayMessage("Pen color set to RGB(" + r + "," + g + "," + b + ")");
                            commandSuccessful = true;
                        } else {
                            displayMessage("Error: RGB values must be between 0 and 255");
                        }
                    } else {
                        displayMessage("Error: Pencolour command requires three parameters: red,green,blue");
                    }
                } catch (NumberFormatException e) {
                    displayMessage("Error: Invalid RGB parameters for pencolour command");
                }
            } else if (lowerCmd.startsWith("penwidth ")) {
                try {
                    int width = Integer.parseInt(lowerCmd.substring("penwidth ".length()).trim());
                    if (width > 0 && width <= 10) {
                        setStroke(width);
                        displayMessage("Pen width set to " + width);
                        commandSuccessful = true;
                    } else {
                        displayMessage("Error: Pen width must be between 1 and 10");
                    }
                } catch (NumberFormatException e) {
                    displayMessage("Error: Invalid width parameter for penwidth command");
                }
            } else if (lowerCmd.equals("save image")) {
                saveImage();
                return;
            } else if (lowerCmd.equals("load image")) {
                loadImage();
                return;
            } else if (lowerCmd.equals("save commands")) {
                saveCommands();
                return;
            } else if (lowerCmd.equals("load commands")) {
                loadCommands();
                return;
            } else if (lowerCmd.equals("exit")) {
                exitApplication();
                return;
            } else if (lowerCmd.equals("about")) {
                about();
                commandSuccessful = true;
            } else if (lowerCmd.equals("penup")) {
                drawOff();
                displayMessage("Pen is up");
                commandSuccessful = true;
            } else if (lowerCmd.equals("pendown")) {
                drawOn();
                displayMessage("Pen is down");
                commandSuccessful = true;
            } else if (lowerCmd.equals("black")) {
                setPenColour(Color.BLACK);
                displayMessage("Pen color set to black");
                commandSuccessful = true;
            } else if (lowerCmd.equals("green")) {
                setPenColour(Color.GREEN);
                displayMessage("Pen color set to green");
                commandSuccessful = true;
            } else if (lowerCmd.equals("red")) {
                setPenColour(Color.RED);
                displayMessage("Pen color set to red");
                commandSuccessful = true;
            } else if (lowerCmd.equals("white")) {
                setPenColour(Color.WHITE);
                displayMessage("Pen color set to white");
                commandSuccessful = true;
            } else if (lowerCmd.equals("reset")) {
                reset();
                displayMessage("Canvas reset to initial state");
                commandSuccessful = true;
            } else if (lowerCmd.equals("clear")) {
                clear();
                displayMessage("Canvas cleared");
                commandSuccessful = true;
            } else if (lowerCmd.startsWith("left ")) {
                validateAndExecuteAngleCommand(lowerCmd, "left");
                commandSuccessful = true;
            } else if (lowerCmd.startsWith("right ")) {
                validateAndExecuteAngleCommand(lowerCmd, "right");
                commandSuccessful = true;
            } else if (lowerCmd.startsWith("move ")) {
                validateAndExecuteDistanceCommand(lowerCmd, "move");
                commandSuccessful = true;
            } else if (lowerCmd.startsWith("reverse ")) {
                validateAndExecuteDistanceCommand(lowerCmd, "reverse");
                commandSuccessful = true;
            } else if (lowerCmd.equals("left") || lowerCmd.equals("right") || 
                       lowerCmd.equals("move") || lowerCmd.equals("reverse")) {
                displayMessage("Error: The '" + lowerCmd + "' command requires a numeric parameter");
            } else if (lowerCmd.startsWith("square ")) {
                try {
                    int size = Integer.parseInt(lowerCmd.substring("square ".length()).trim());
                    if (size > 0 && size <= 300) {
                        drawSquare(size);
                        commandSuccessful = true;
                    } else {
                        displayMessage("Error: Square size must be between 1 and 300");
                    }
                } catch (NumberFormatException e) {
                    displayMessage("Error: Invalid size parameter for square command");
                }
            } else if (lowerCmd.startsWith("triangle ")) {
                if (lowerCmd.matches("triangle \\d+,\\d+,\\d+")) {
                    try {
                        String[] sides = lowerCmd.substring("triangle ".length()).trim().split(",");
                        int a = Integer.parseInt(sides[0].trim());
                        int b = Integer.parseInt(sides[1].trim());
                        int c = Integer.parseInt(sides[2].trim());
                        
                        if (a > 0 && b > 0 && c > 0 && a+b > c && a+c > b && b+c > a) {
                            drawTriangleWithSides(a, b, c);
                            commandSuccessful = true;
                        } else {
                            displayMessage("Error: Invalid triangle sides (must satisfy triangle inequality)");
                        }
                    } catch (NumberFormatException e) {
                        displayMessage("Error: Invalid parameters for triangle command");
                    }
                } else {
                    try {
                        int size = Integer.parseInt(lowerCmd.substring("triangle ".length()).trim());
                        if (size > 0 && size <= 300) {
                            drawEquilateralTriangle(size);
                            commandSuccessful = true;
                        } else {
                            displayMessage("Error: Triangle size must be between 1 and 300");
                        }
                    } catch (NumberFormatException e) {
                        displayMessage("Error: Invalid size parameter for triangle command");
                    }
                }
            } else if (lowerCmd.startsWith("circle ")) {
                try {
                    int radius = Integer.parseInt(lowerCmd.substring("circle ".length()).trim());
                    if (radius > 0 && radius <= 150) {
                        drawOn();  // Ensure pen is down before drawing
                        circle(radius);
                        displayMessage("Drew a circle with radius " + radius);
                        commandSuccessful = true;
                    } else {
                        displayMessage("Error: Circle radius must be between 1 and 150");
                    }
                } catch (NumberFormatException e) {
                    displayMessage("Error: Invalid radius parameter for circle command");
                }
            } else if (lowerCmd.startsWith("spiral ")) {
                try {
                    String[] params = lowerCmd.substring("spiral ".length()).trim().split(" ");
                    if (params.length == 3) {
                        int initial = Integer.parseInt(params[0]);
                        int increment = Integer.parseInt(params[1]);
                        int steps = Integer.parseInt(params[2]);
                        
                        if (initial > 0 && increment > 0 && steps > 0 && steps <= 50) {
                            drawSpiral(initial, increment, steps);
                            commandSuccessful = true;
                        } else {
                            displayMessage("Error: Invalid parameters for spiral command");
                        }
                    } else {
                        displayMessage("Error: Spiral command requires 3 parameters: initial length, increment, and steps");
                    }
                } catch (NumberFormatException e) {
                    displayMessage("Error: Invalid parameters for spiral command");
                }
            } else if (lowerCmd.startsWith("star ")) {
                try {
                    String[] params = lowerCmd.substring("star ".length()).trim().split(" ");
                    if (params.length == 2) {
                        int points = Integer.parseInt(params[0]);
                        int size = Integer.parseInt(params[1]);
                        
                        if (points >= 5 && points <= 12 && size > 0 && size <= 200) {
                            drawStar(points, size);
                            commandSuccessful = true;
                        } else {
                            displayMessage("Error: Star points must be 5-12 and size 1-200");
                        }
                    } else {
                        displayMessage("Error: Star command requires 2 parameters: points and size");
                    }
                } catch (NumberFormatException e) {
                    displayMessage("Error: Invalid parameters for star command");
                }
            } else {
                displayMessage("Error: Unknown command '" + command + "'");
            }
            
            if (commandSuccessful) {
                addCommandToHistory(command);
                setUnsavedChanges(true);
            }
        } catch (Exception e) {
            displayMessage("Error: " + e.getMessage());
        }
    }
    
    private void drawSquare(int sideLength) {
        drawOn();
        for (int i = 0; i < 4; i++) {
            forward(sideLength);
            right(90);
        }
        displayMessage("Drew a square with side length " + sideLength);
    }
    
    private void drawEquilateralTriangle(int sideLength) {
        drawOn();
        for (int i = 0; i < 3; i++) {
            forward(sideLength);
            right(120);
        }
        displayMessage("Drew a triangle with side length " + sideLength);
    }
    
    private void drawSpiral(int initialLength, int increment, int steps) {
        drawOn();
        int length = initialLength;
        for (int i = 0; i < steps; i++) {
            forward(length);
            right(90);
            length += increment;
        }
        displayMessage("Drew a spiral with " + steps + " segments");
    }
    
    private void drawStar(int points, int size) {
        drawOn();
        int angle = 180 - (180 / points);
        
        for (int i = 0; i < points; i++) {
            forward(size);
            right(angle);
        }
        displayMessage("Drew a " + points + "-pointed star");
    }
    
    private void drawTriangleWithSides(int a, int b, int c) {
        drawOn();
        double angleA = Math.toDegrees(Math.acos((b*b + c*c - a*a) / (2.0 * b * c)));
        double angleB = Math.toDegrees(Math.acos((a*a + c*c - b*b) / (2.0 * a * c)));
        double angleC = 180 - angleA - angleB;
        
        forward(a);
        right((int)(180.0 - angleB));
        forward(c);
        right((int)(180.0 - angleC));
        forward(b);
        
        displayMessage("Drew a triangle with sides " + a + ", " + b + ", " + c);
    }
    
    private void validateAndExecuteAngleCommand(String command, String cmdName) {
        try {
            String paramStr = command.substring(cmdName.length()).trim();
            
            if (paramStr.isEmpty()) {
                displayMessage("Error: The '" + cmdName + "' command requires a numeric angle parameter");
                return;
            }
            
            int angle;
            try {
                angle = Integer.parseInt(paramStr);
            } catch (NumberFormatException e) {
                displayMessage("Error: '" + paramStr + "' is not a valid number for the " + cmdName + " command");
                return;
            }
            
            if (angle < 0) {
                displayMessage("Error: Angle for '" + cmdName + "' must be positive");
                return;
            }
            
            if (angle > 360) {
                displayMessage("Error: Angle for '" + cmdName + "' should not exceed 360 degrees");
                return;
            }
            
            if (cmdName.equals("left")) {
                left(angle);
                displayMessage("Turned left " + angle + " degrees");
            } else {
                right(angle);
                displayMessage("Turned right " + angle + " degrees");
            }
        } catch (Exception e) {
            displayMessage("Error processing " + cmdName + " command: " + e.getMessage());
        }
    }
    
    private void validateAndExecuteDistanceCommand(String command, String cmdName) {
        try {
            String paramStr = command.substring(cmdName.length()).trim();
            
            if (paramStr.isEmpty()) {
                displayMessage("Error: The '" + cmdName + "' command requires a numeric distance parameter");
                return;
            }
            
            int distance;
            try {
                distance = Integer.parseInt(paramStr);
            } catch (NumberFormatException e) {
                displayMessage("Error: '" + paramStr + "' is not a valid number for the " + cmdName + " command");
                return;
            }
            
            if (distance <= 0) {
                displayMessage("Error: Distance for '" + cmdName + "' must be positive");
                return;
            }
            
            if (distance > 500) {
                displayMessage("Error: Distance for '" + cmdName + "' should not exceed 500 units");
                return;
            }
            
            if (cmdName.equals("move")) {
                forward(distance);
                displayMessage("Moved forward " + distance + " units");
            } else {
                reverse(distance);
                displayMessage("Moved backward " + distance + " units");
            }
        } catch (Exception e) {
            displayMessage("Error processing " + cmdName + " command: " + e.getMessage());
        }
    }
    
    private void reverse(int distance) {
        right(180);
        forward(distance);
        right(180);
    }

    private void setPenWidth(int width) {
        setStroke(width);
    }
    
    private void addCommandToHistory(String command) {
        commandHistory.add(command);
        historyTextArea.append(command + "\n");
        hasUnsavedCommands = true;
    }
    
    private void setUnsavedChanges(boolean hasChanges) {
        hasUnsavedImage = hasChanges;
        
        String title = mainFrame.getTitle();
        if (hasChanges && !title.endsWith("*")) {
            mainFrame.setTitle(title + "*");
        } else if (!hasChanges && title.endsWith("*")) {
            mainFrame.setTitle(title.substring(0, title.length() - 1));
        }
    }
    
    private void saveImage() {
        try {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Save Image");
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                    "PNG Images", "png"));
            
            if (fileChooser.showSaveDialog(mainFrame) == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                if (!file.getName().toLowerCase().endsWith(".png")) {
                    file = new File(file.getAbsolutePath() + ".png");
                }
                
                BufferedImage image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
                paint(image.getGraphics());
                
                ImageIO.write(image, "png", file);
                displayMessage("Image saved to " + file.getName());
                setUnsavedChanges(false);
            }
        } catch (IOException e) {
            displayMessage("Error saving image: " + e.getMessage());
        }
    }
    
    private void loadImage() {
        if (hasUnsavedImage && !confirmDiscardChanges("current drawing")) {
            return;
        }
        
        try {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Load Image");
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                    "PNG Images", "png"));
            
            if (fileChooser.showOpenDialog(mainFrame) == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                
                BufferedImage image = ImageIO.read(file);
                if (image == null) {
                    throw new IOException("Failed to load image");
                }
                
                clear();
                setBufferedImage(image);
                displayMessage("Image loaded from " + file.getName());
                setUnsavedChanges(false);
            }
        } catch (IOException e) {
            displayMessage("Error loading image: " + e.getMessage());
        }
    }
    
    private void saveCommands() {
        try {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Save Commands");
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                    "Text Files", "txt"));
            
            if (fileChooser.showSaveDialog(mainFrame) == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                if (!file.getName().toLowerCase().endsWith(".txt")) {
                    file = new File(file.getAbsolutePath() + ".txt");
                }
                
                try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                    for (String cmd : commandHistory) {
                        writer.println(cmd);
                    }
                }
                
                displayMessage("Commands saved to " + file.getName());
                hasUnsavedCommands = false;
            }
        } catch (IOException e) {
            displayMessage("Error saving commands: " + e.getMessage());
        }
    }
    
    private void loadCommands() {
        if (hasUnsavedCommands && !confirmDiscardChanges("command history")) {
            return;
        }
        
        try {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Load Commands");
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                    "Text Files", "txt"));
            
            if (fileChooser.showOpenDialog(mainFrame) == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                
                commandHistory.clear();
                historyTextArea.setText("");
                
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (!line.trim().isEmpty()) {
                            displayMessage("Executing: " + line);
                            processCommand(line);
                        }
                    }
                }
                
                displayMessage("Commands loaded from " + file.getName());
                hasUnsavedCommands = false;
            }
        } catch (IOException e) {
            displayMessage("Error loading commands: " + e.getMessage());
        }
    }
    
    private boolean confirmDiscardChanges(String itemType) {
        int result = JOptionPane.showConfirmDialog(mainFrame,
                "You have unsaved changes to your " + itemType + ". Save before proceeding?",
                "Unsaved Changes",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE);
        
        if (result == JOptionPane.YES_OPTION) {
            if (itemType.equals("current drawing")) {
                saveImage();
            } else {
                saveCommands();
            }
            return true;
        } else if (result == JOptionPane.NO_OPTION) {
            return true;
        } else {
            return false;
        }
    }
    
    private void exitApplication() {
        if (hasUnsavedImage && !confirmDiscardChanges("current drawing")) {
            return;
        }
        
        if (hasUnsavedCommands && !confirmDiscardChanges("command history")) {
            return;
        }
        
        mainFrame.dispose();
        System.exit(0);
    }
    
    @Override
    public void reset() {
        super.reset();
        setPenColour(Color.BLACK);  // Reset to default color
        setStroke(1);               // Reset to default width
        drawOn();                   // Ensure pen is down
        setxPos(initialX);          // Reset X position
        setyPos(initialY);          // Reset Y position
    }
    
    @Override
    public void displayMessage(String message) {
        super.displayMessage(message);
        System.out.println(message);
    }
    
    // Add this method to support demonstrateGraphics if TurtleGraphicsTest.java calls it
    public void demonstrateGraphics() {
        drawOn();
        setPenColour(Color.RED);
        drawSquare(100);
        setPenColour(Color.GREEN);
        drawEquilateralTriangle(75);
        setPenColour(Color.BLUE);
        circle(50);
        displayMessage("Graphics demonstration complete");
    }
}