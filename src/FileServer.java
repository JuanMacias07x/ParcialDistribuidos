import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.net.Socket;

public class FileServer {
    private static final int PORT = 8080;
    private static HashMap<String, File> fileStore = new HashMap<>();

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("FileServer started on port " + PORT);

        while (true) {
            Socket clientSocket = serverSocket.accept();
            System.out.println("Client connected: " + clientSocket.getInetAddress());

            // Handle client in a new thread
            new Thread(() -> handleClient(clientSocket)).start();
        }
    }

    private static void handleClient(Socket clientSocket) {
        try (ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
                ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream())) {

            String command = (String) in.readObject();
            if (command.equals("UPLOAD")) {
                // Receive file from client
                String fileName = (String) in.readObject();
                byte[] fileData = (byte[]) in.readObject();
                File file = new File("server_storage/" + fileName);
                try (FileOutputStream fos = new FileOutputStream(file)) {
                    fos.write(fileData);
                }
                fileStore.put(fileName, file);
                out.writeObject("File uploaded successfully.");
            }
            // Implement other commands like DOWNLOAD, DELETE, etc.

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
