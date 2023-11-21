import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Set;
import java.util.HashSet;
import java.util.Scanner;
import java.util.concurrent.Executors;

/**
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

    private static String serverGroupName ;

    public static String getGroupName() {
        return serverGroupName;
    }
    
    public static void setGroupName(String groupName) {
        ChatServer.serverGroupName = groupName;
    }
    public static void main(String[] args) throws Exception {
        System.out.println("The chat server is running...");
        var pool = Executors.newFixedThreadPool(500);

        //try-with-resources statement in Java. 
        // It is a convenient way to ensure that a resource is properly closed after being used, 
        // even if an exception occurs.
        try (var listener = new ServerSocket(59001)) {
            while (true) {

                // Whenever a client connects, a new Handler thread is created
                // and added to the thread pool to handle the client's requests.
                pool.execute(new Handler(listener.accept()));
            }
        }
    }

    /**
     * The client handler task.
     * Each Handler instance is responsible for handling a single client connection.
     */
    private static class Handler implements Runnable {
        private String name;
        private String clientGroupName ;
        private Socket socket;
        private Scanner in;
        private PrintWriter out;

        /**
         * Constructs a handler thread, squirreling away the socket. All the interesting
         * work is done in the run method. Remember the constructor is called from the
         * server's main method, so this has to be as short as possible.
         */
        public Handler(Socket socket) {
            this.socket = socket;
        }

        public  boolean groupFunction(String cGroupName){
        if(names.size() ==1){
         ChatServer.setGroupName(clientGroupName); 
         }

         if (clientGroupName.equals(ChatServer.getGroupName())){
            return true;
         }
         else {
             return false;
                }
        }

        /**
         * Services this thread's client by repeatedly requesting a screen name until a
         * unique one has been submitted, then acknowledges the name and registers the
         * output stream for the client in a global set, then repeatedly gets inputs and
         * broadcasts them.
         */
        public void run() {
            try {
                in = new Scanner(socket.getInputStream());
                out = new PrintWriter(socket.getOutputStream(), true);

                // Keep requesting a name until we get a unique one.
                while (true) {
                    out.println("SUBMITNAME");
                    name = in.nextLine();
                    if (name == null) {
                        return;
                    }
                    synchronized (names) {
                        if (!name.isBlank() && !names.contains(name)) {
                            names.add(name);
                            break;
                        }
                    }
                }


                // Now that a successful name has been chosen, add the socket's print writer
                // to the set of all writers so this client can receive broadcast messages.
                // But BEFORE THAT, let everyone else know that the new person has joined!
                // the server acknowledges 
                out.println("NAMEACCEPTED " + name);
                clientGroupName = in.nextLine();
                boolean withinGroup = groupFunction(clientGroupName);
                if(names.size() ==1){
                    ChatServer.setGroupName(clientGroupName); 
                }
                
                
                if(withinGroup){
                    for (PrintWriter writer : writers) {
                    writer.println("MESSAGE " + name + " has joined");
                }
                writers.add(out);

                // ****END OF the initial setup****
                // Accept messages from this client and broadcast them.
                while (true) {
                    String input = in.nextLine();
                    // the run method returns, terminating the client connection.
                    if (input.toLowerCase().startsWith("/quit")) {
                        return;
                    }
                    for (PrintWriter writer : writers) {
                        writer.println("MESSAGE " + name + ": " + input);
                    }
                }
               
            } else {
                   out.println("GROUPERROR The meeting room is occupied with another group");
                   return ;
            }

                
            } catch (Exception e) {
                System.out.println(e);
            } finally {

                if (out != null) {
                    writers.remove(out);
                }
                
                if (name != null) {
                    System.out.println(name + " is leaving");
                    names.remove(name);
                    for (PrintWriter writer : writers) {
                        writer.println("MESSAGE " + name + " has left");
                    }
                }
                try {
                    socket.close();
                } catch (IOException e) {
                }
            }
        }
    }


}