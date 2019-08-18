import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ChatServer {
    List<Writer> clientOutputStreams;

    public static void main(String[] args) {
        new ChatServer().start();
    }

    public void start() {
        clientOutputStreams = new ArrayList<>();
        try {
            ServerSocket socket = new ServerSocket(5000);
            System.out.println("Server is listening");

            while (true) {
                Socket clientSocket = socket.accept();
                System.out.println(String.format("Accepted client from %s", clientSocket.getLocalAddress()));
                // make a PrintWriter chained to the Socket's low-level output stream
                PrintWriter writer = new PrintWriter(clientSocket.getOutputStream());
                // server will need to know where it should send data back
                // so we store a writer for each client into a list
                clientOutputStreams.add(writer);

                Thread t = new Thread(new ListenerHandler(clientSocket));
                t.start();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void broadcast(String message) {
        // send message to all clients
        Iterator<Writer> iter = clientOutputStreams.iterator();
        while (iter.hasNext()) {
            PrintWriter writer = (PrintWriter) iter.next();
            writer.println(message);
            writer.flush();
        }
    }

    public class ListenerHandler implements Runnable {
        BufferedReader reader;
        Socket socket;

        public ListenerHandler(Socket clientSocket) {
            try {
                this.socket = clientSocket;
                // make an InputStreamReader chained to the Socket's low-level input stream
                InputStreamReader isReader = new InputStreamReader(socket.getInputStream());
                // make a BufferedReader to read later
                this.reader = new BufferedReader(isReader);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            String message;
            try {
                // read message from the BufferedReader
                while ((message = reader.readLine()) != null) {
                    System.out.println(String.format("Received message: %s", message));
                    broadcast(message);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
