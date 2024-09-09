import java.io.*;
import java.net.*;

public class FileClient {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 8080;

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        try (Socket socket = new Socket(SERVER_HOST, SERVER_PORT);
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            // Ejemplo de subida de archivo
            out.writeObject("UPLOAD");
            out.writeObject("testfile.txt");

            File file = new File("client_storage/testfile.txt");
            byte[] fileData = new byte[(int) file.length()];
            try (FileInputStream fis = new FileInputStream(file)) {
                fis.read(fileData);
            }
            out.writeObject(fileData);

            String response = (String) in.readObject();
            System.out.println("Server response: " + response);
        }
    }
}
