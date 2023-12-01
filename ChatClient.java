import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class ChatClient {

    private String serverAddress;
    private Scanner in;
    private PrintWriter out;
    private JFrame frame = new JFrame("Chatter");
    private JTextField textField;
    private JTextArea messageArea;

    public ChatClient(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    private void createAndShowGUI() {
        ImagePanel panel = new ImagePanel("C:\\Users\\hp\\Documents\\NetBeansProjects\\GroupChatSandC\\src\\test\\java\\Image1.jpg");
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BorderLayout());

        JButton startButton = new JButton("Start");
        startButton.setPreferredSize(new Dimension(100, 40));
        startButton.setBackground(new Color(255, 191, 0)); // Set the button background color to RGB(255, 191, 0)
        startButton.addActionListener(e -> {
            frame.dispose();
            new Thread(() -> {
                try {
                    run();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }).start();
        });

        buttonPanel.add(startButton, BorderLayout.CENTER);
        frame.getContentPane().add(panel, BorderLayout.CENTER);
        frame.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setSize(500, 400);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private String getName() {
        return JOptionPane.showInputDialog(frame, "Choose a screen name:", "Screen name selection",
                JOptionPane.PLAIN_MESSAGE);
    }

    private String getGroup() {
        return JOptionPane.showInputDialog(frame, "Choose a group name:", "Group name selection",
                JOptionPane.PLAIN_MESSAGE);
    }

    private void printErrorMessage() {
        JOptionPane.showMessageDialog(frame, "The room is occupied!", "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void setupChatUI(String username) {
        frame.getContentPane().removeAll();
        frame.setLayout(new BorderLayout());

        messageArea = new JTextArea(16, 50);
        messageArea.setEditable(false);
        messageArea.setBackground(new Color(249, 239, 148)); // Set the background color to yellow

        JScrollPane scrollPane = new JScrollPane(messageArea);
        frame.add(scrollPane, BorderLayout.CENTER);

        textField = new JTextField(50);
        textField.addActionListener(e -> {
            out.println(textField.getText());
            textField.setText("");
        });

        frame.add(textField, BorderLayout.SOUTH);

        frame.pack();

        frame.setTitle("Chatter - " + username);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    private void run() throws IOException {
        try {
            var socket = new Socket(serverAddress, 59001);
            in = new Scanner(socket.getInputStream());
            out = new PrintWriter(socket.getOutputStream(), true);

            while (in.hasNextLine()) {
                var line = in.nextLine();
                if (line.startsWith("SUBMITNAME")) {
                    out.println(getName());
                } else if (line.startsWith("NAMEACCEPTED")) {
                    String username = getGroup();
                    out.println(username);
                    setupChatUI(username);
                } else if (line.startsWith("MESSAGE")) {
                    messageArea.append(line.substring(8) + "\n");
                } else if (line.startsWith("GROUPERROR")) {
                    frame.setVisible(false);
                    printErrorMessage();
                    return;
                }
            }
        } finally {
            frame.setVisible(false);
            frame.dispose();
        }
    }

    public static void main(String[] args) {
        var client = new ChatClient("localhost");
        client.createAndShowGUI();
    }

}

class ImagePanel extends JPanel {
    private Image backgroundImage;

    public ImagePanel(String imagePath) {
        backgroundImage = new ImageIcon(imagePath).getImage();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
    }
}
