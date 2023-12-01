import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

/**
 * A simple Swing-based client for the chat server. Graphically it is a frame
 * with a text field for entering messages and a textarea to see the whole
 * dialog.
 *
 * The client follows the following Chat Protocol. When the server sends
 * "SUBMITNAME" the client replies with the desired screen name. The server will
 * keep sending "SUBMITNAME" requests as long as the client submits screen names
 * that are already in use. When the server sends a line beginning with
 * "NAMEACCEPTED" the client is now allowed to start sending the server
 * arbitrary strings to be broadcast to all chatters connected to the server.
 * When the server sends a line beginning with "MESSAGE" then all characters
 * following this string should be displayed in its message area.
 */

public class ChatClient {

    // Variables declaration
    private String serverAddress;
    private Scanner in;
    private PrintWriter out;
    private JFrame frame = new JFrame("Start");
    private JTextField textField;
    private JTextArea messageArea;
// ---------------------------------------------------------------------------------------------------

    // Construct
    public ChatClient(String serverAddress) {
        this.serverAddress = serverAddress;
    }
// ---------------------------------------------------------------------------------------------------

    // Method to GUI for start window
    private void createAndShowGUI() {

        ImagePanel panel = new ImagePanel("Image1.jpg"); // Panel for the image in the start window
        JLabel welcomeMSG = new JLabel("Welcome To Virtual Meeting Room !"); // Label for the welcome msg in the start window
        JPanel buttonPanel = new JPanel(); // Panel for the start button in the start window

        // Add start button
        JButton startButton = new JButton("Start"); 
        startButton.setFont(new Font("Tw Cen MT", 1, 20)); // Font setting
        startButton.setBackground(new Color(255, 191, 0)); // Yellow color for the button background
        startButton.setForeground(new Color(255,255,255)); // White color for the button text
        startButton.addActionListener(e -> {
            frame.dispose();
            new Thread(() -> {
                try {
                    run();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }).start();
        }); // Button action setting
        buttonPanel.setBounds(110, 275, 250, 50); // Button panel position setting       
        buttonPanel.setBackground(new Color(255, 255, 255)); // White color for the button panel background
        buttonPanel.add(startButton); // Add the button to the panel
        frame.getContentPane().add(buttonPanel); // Add the button panel to the frame


        // Add welcome message 
        welcomeMSG.setBounds(120, 222, 250, 50); // Message label position setting
        welcomeMSG.setFont(new Font("Tw Cen MT", 1, 16)); // Font setting
        welcomeMSG.setForeground(new Color(102,102,102)); // Gray color for the text
        frame.getContentPane().add(welcomeMSG); // Add the Message to the frame

        // Add panel for the image
        panel.setBackground(new Color(255, 255, 255)); // White color for the panel background
        frame.getContentPane().add(panel, BorderLayout.CENTER); // Add the panel to the center of the frame

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // To terminate the application when the frame is closed.
        
        // Frame size setting
        frame.pack();  
        frame.setSize(500, 400);

        frame.setLocationRelativeTo(null); // Set the window in the center of the screen 
        frame.setVisible(true); // Make the frame vidible in the screen
    }
// ---------------------------------------------------------------------------------------------------

    // Method to username dialog
    private String getName() {
        return JOptionPane.showInputDialog(frame, "Enter a username:", "Username selection",
                JOptionPane.PLAIN_MESSAGE);
    }
// ---------------------------------------------------------------------------------------------------

    // Method to group name dialog    
    private String getGroup() {
        return JOptionPane.showInputDialog(frame, "Choose a group name:", "Group name selection",
                JOptionPane.PLAIN_MESSAGE);
    }
// ---------------------------------------------------------------------------------------------------

    // Method to error msg dialog    
    private void printErrorMessage() {
        JOptionPane.showMessageDialog(frame, "The room is occupied!", "Error", JOptionPane.ERROR_MESSAGE);
    }
// ---------------------------------------------------------------------------------------------------

    // Method to GUI for chatter window
    private void setupChatUI(String username, String group_name) {
        frame.getContentPane().removeAll(); // Remove all component frome the pane
        frame.setLayout(new BorderLayout()); // Set layout manager to add component

        // Add message area 
        messageArea = new JTextArea(16, 50);
        messageArea.setEditable(false); // Make the message area view only
        messageArea.setFont(new Font("Tw Cen MT", 1, 14)); // Font setting
        messageArea.setBackground(new Color(249, 239, 148)); // Light yellow for the message area background
        
        // Add scroll bar
        JScrollPane scrollPane = new JScrollPane(messageArea); // Add scroll bar to the message area
        frame.add(scrollPane, BorderLayout.CENTER); // Add the scroll bar to the center of the frame

        // Add text field
        textField = new JTextField(50);
        textField.addActionListener(e -> {
            out.println(textField.getText());
            textField.setText(""); // Text field action setting
        }); // 
        frame.add(textField, BorderLayout.SOUTH); // Add the Text field to the south of the frame

        frame.pack(); // Frame size setting

        frame.setTitle("Chatter - " + group_name + " ( " + username + " )"); // Set title for the chatter window
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
// ---------------------------------------------------------------------------------------------------

    // Method for communication between the server and the client
    private void run() throws IOException {
        try {
            var socket = new Socket(serverAddress, 59001); // Create client socket
            in = new Scanner(socket.getInputStream()); // Create object to read from the server
            out = new PrintWriter(socket.getOutputStream(), true); // Create object to send to the server

            String username = ""; // variable to store username
            String group_name = ""; // variable to store group name

            while (in.hasNextLine()) {
                var line = in.nextLine();
                if (line.startsWith("SUBMITNAME")) {
                    username = getName();
                    out.println(username);
                } else if (line.startsWith("NAMEACCEPTED")) {
                    group_name = getGroup();
                    out.println(group_name);
                    setupChatUI(username, group_name);
                } else if (line.startsWith("MESSAGE")) {
                    messageArea.append(line.substring(8) + "\n");
                } else if (line.startsWith("GROUPERROR")) {
                    frame.setVisible(false);
                    printErrorMessage();
                    return;
                }
            } // end the while loop
        } finally {
            frame.setVisible(false);
            frame.dispose();
        } // The frame closed and it no longer be visible on the screen
    }
// ---------------------------------------------------------------------------------------------------

    // The main method
    public static void main(String[] args) {
        // Check if the IP address for the server passed 
        if (args.length != 1) {
            System.err.println("Pass the server IP as the sole command line argument");
            return;
        }
        var client = new ChatClient(args[0]);
        client.createAndShowGUI();
    }

}
// ___________________________________________________________________________________________________

// class to create image panel
class ImagePanel extends JPanel {
    private Image backgroundImage;

    // Construct
    public ImagePanel(String imagePath) {
        backgroundImage = new ImageIcon(imagePath).getImage();
    }

    // Method to setting the size and the location
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(backgroundImage, 110, 1, 250, 250, this);
    }
}
