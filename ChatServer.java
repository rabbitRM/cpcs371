
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Set;
import java.util.HashSet;
import java.util.Scanner;
import java.util.concurrent.Executors;

/*
 * A multithreaded chat room server. When a client connects the server requests
 * a screen name by sending the client the text "SUBMITNAME", and keeps
 * requesting a name until a unique one is received. After a client submits a
 * unique name, the server acknowledges with "NAMEACCEPTED". Then all messages
 * from that client will be broadcast to all other clients that have submitted a
 * unique screen name. The broadcast messages are prefixed with "MESSAGE".
 *
 * This is just a teaching example so it can be enhanced in many ways, e.g.,
 * better logging. Another is to accept a lot of fun commands, like Slack.
 */
public class ChatServer {

    // These sets are used for managing client connections and broadcasting messages.
    // All client names, so we can check for duplicates upon registration.
    private static Set<String> names = new HashSet<>();

    // The set of all the print writers for all the clients, used for broadcast.
    private static Set<PrintWriter> writers = new HashSet<>();

    // Store the server's group name.
    private static String serverGroupName;

    // Method to get the server's group name
    public static String getGroupName() {
        return serverGroupName;
    }
// ---------------------------------------------------------------------------------------------------

    // Method to set the server's group name
    private static void setGroupName(String groupName) {
        ChatServer.serverGroupName = groupName;
    }
// ---------------------------------------------------------------------------------------------------

    public static void main(String[] args) throws Exception {

        System.out.println("The chat server is running...");
        var pool = Executors.newFixedThreadPool(500);

        // Creating a server socket to listen for client connections on port 59001
        // This way of writing is a convenient way to ensure that a resource is properly closed after being used.
        try (var listener = new ServerSocket(59001)) {
            while (true) {

                // Whenever a client connects, a new Handler thread is created
                // and added to the thread pool to handle the client's requests.
                pool.execute(new Handler(listener.accept()));

            }
        }
    }
// ---------------------------------------------------------------------------------------------------
    
    // The client handler task. Each Handler instance is responsible for handling a single client connection.
    private static class Handler implements Runnable {

        // Instance variables for each client connection
        private String name;
        private String clientGroupName;
        private Socket socket;
        private Scanner in;
        private PrintWriter out;
        private boolean withinGroup;

        /*
          Constructs a handler thread, squirreling away the socket. All the interesting
          work is done in the run method. Remember the constructor is called from the
          server's main method, so this has to be as short as possible.
         */
        public Handler(Socket socket) {
            this.socket = socket;
        }
// ---------------------------------------------------------------------------------------------------
        
        // Method to checks if the client belongs to the same group as the server. Returns true if it does, false otherwise.
        public boolean groupFunction(String cGroupName) {

            // if the clinet is the first client then set hi group name as the server group name     
            if (names.size() == 1) {
                ChatServer.setGroupName(clientGroupName);
            }

            // if the client's group name is equal to server's group then return true  
            if (clientGroupName.equals(ChatServer.getGroupName())) {
                return true;
                
            } // if the client's group name is not equal to server's group then return false 
            else {
                return false;
            }
        }
// ---------------------------------------------------------------------------------------------------
        
        /**
         * Services this thread's client by repeatedly requesting a screen name
         * until a unique one has been submitted, then acknowledges the name, and
         * requesting a group name then registers the output stream for the client in a global set, then
         * repeatedly gets inputs and broadcasts them.
         */
        public void run() {
            try {
                in = new Scanner(socket.getInputStream());
                out = new PrintWriter(socket.getOutputStream(), true);

                // Keep requesting a name until we get a unique one.
                while (true) {

                    // Request the client to submit a name 
                    out.println("SUBMITNAME");

                    // Read the name written by the client 
                    name = in.nextLine();

                    // If the name is null, then end connection with client
                    if (name == null) {
                        return;
                    }

                    // synchronization to ensure thread safety when accessing and modifying the shared names set.
                    synchronized (names) {

                        //Checking if the name is non-empty and not already in use
                        if (!name.isBlank() && !names.contains(name)) {

                            // add the client to the name list
                            names.add(name);
                            break;
                        }
                    }
                }

                // Now that a successful name has been chosen, add the socket's print writer
                // to the set of all writers so this client can receive broadcast messages.
                // but before that, let everyone else know that the new person has joined!
                // the server acknowledges 
                // A successful name has been chosen , inform the client
                out.println("NAMEACCEPTED " + name);

                // Reading the group name written by the client 
                clientGroupName = in.nextLine();

                // Flag to know if the client has joined the group or not
                withinGroup = groupFunction(clientGroupName);

                // If the client has joined the group 
                if (withinGroup) {

                    // Lettin everyone in the group know that a new client has joined!
                    for (PrintWriter writer : writers) {
                        writer.println("MESSAGE " + name + " has joined");
                    }

                    // Adding the client print writer    
                    writers.add(out);
                    // **** END OF the initial setup ****
                    // Receiveing messages from the client and broadcasts them to all other connected clients in the group.
                    while (true) {
                  
                        // Reading a message from the client
                        String input = in.nextLine();

                        // If the client wish to leave, end connection
                        if (input.toLowerCase().startsWith("/quit")) {
                            return;
                        }

                        // Broadcasting the message to all connected clients
                        for (PrintWriter writer : writers) {
                            writer.println("MESSAGE " + name + ": " + input);
                        }
                    }

                } // If the client has not joined the group 
                else {
                    // Removing the client name and informing the client
                    names.remove(name);
                    out.println("GROUPERROR The meeting room is occupied with another group");
                }

            } catch (Exception e) {
                System.out.println(e);
            } finally {

                if (out != null) {

                    // Removing the client print writer  
                    writers.remove(out);
                }

                // If the client is within the group.
                if (name != null && withinGroup) {

                    // Printing in the console that the client is leaving.
                    System.out.println(name + " is leaving");

                    // Removing his name from the names list
                    names.remove(name);

                    // Lettin everyone in the group know that a client has leaved!
                    for (PrintWriter writer : writers) {
                        writer.println("MESSAGE " + name + " has left");
                    }
                }
                try {

                    // Close the socket to free the resources
                    socket.close();

                } catch (IOException e) {
                }
            }
        }
    }

}
