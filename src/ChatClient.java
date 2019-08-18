import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ChatClient {
    JTextArea incoming;
    JTextField outgoing;
    BufferedReader reader;
    PrintWriter writer;
    Socket socket;
    String clientName;

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public static void main(String[] args) {
        new ChatClient().showPromptDialog();
    }

    public void showPromptDialog() {
        String name = (String) JOptionPane.showInputDialog(null,
                "What's your name?",
                "Question",
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                "");
        if (!"".equals(name)) {
            this.setClientName(name);
            this.start();
        }
    }

    public void start() {
        JFrame frame = new JFrame("Chat Client");
        JPanel mainPanel = new JPanel();
        incoming = new JTextArea(15, 50);
        incoming.setLineWrap(true);
        incoming.setWrapStyleWord(true);
        incoming.setEditable(false);
        JScrollPane qScroller = new JScrollPane(incoming);
        qScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        qScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        outgoing = new JTextField(40);
        JButton sendButton = new JButton("Send");
        sendButton.addActionListener(new SendButtonListener());
        mainPanel.add(qScroller);
        mainPanel.add(outgoing);
        mainPanel.add(sendButton);
        frame.getContentPane().add(BorderLayout.CENTER, mainPanel);
        setUpNetworking();

        Thread readerThread = new Thread(new IncomingReader());
        readerThread.start();

        frame.setSize(650, 370);
        frame.setVisible(true);
        frame.setAlwaysOnTop(true);
        frame.setLocationRelativeTo(null);
    }

    private void setUpNetworking() {
        try {
            // make a Socket connection to the server
            socket = new Socket("127.0.0.1", 5000);
            System.out.println(String.format("Connected to server: %s", socket.getLocalAddress()));
            // make an InputStreamReader chained to the Socket's low-level input stream
            InputStreamReader streamReader = new InputStreamReader(socket.getInputStream());
            // make a BufferedReader to read message from server
            reader = new BufferedReader(streamReader);
            // make a PrintWriter to send a message to server
            writer = new PrintWriter(socket.getOutputStream());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public class SendButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent ev) {
            try {
                writer.println(String.format("%s: %s", clientName, outgoing.getText()));
                writer.flush();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            outgoing.setText("");
            outgoing.requestFocus();
        }
    }

    class IncomingReader implements Runnable {
        public void run() {
            String message;
            try {
                while ((message = reader.readLine()) != null) {
                    incoming.append(message + "\n");
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
