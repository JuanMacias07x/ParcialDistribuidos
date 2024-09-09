import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class RPCServer extends UnicastRemoteObject implements FileManager {

    public RPCServer() throws RemoteException {
        super();
    }

    @Override
    public String uploadFile(String fileName, byte[] fileData) throws RemoteException {
        try {
            File file = new File("server_storage/" + fileName);
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(fileData);
            fos.close();
            return "File uploaded successfully.";
        } catch (IOException e) {
            e.printStackTrace();
            return "File upload failed.";
        }
    }

    @Override
    public byte[] downloadFile(String fileName) throws RemoteException {
        try {
            File file = new File("server_storage/" + fileName);
            if (!file.exists()) {
                return null;
            }
            FileInputStream fis = new FileInputStream(file);
            byte[] fileData = new byte[(int) file.length()];
            fis.read(fileData);
            fis.close();
            return fileData;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String[] listFiles() throws RemoteException {
        File folder = new File("server_storage");
        return folder.list(); // Devuelve una lista de nombres de archivos
    }

    @Override
    public String renameFile(String oldFileName, String newFileName) throws RemoteException {
        File oldFile = new File("server_storage/" + oldFileName);
        File newFile = new File("server_storage/" + newFileName);
        if (oldFile.exists() && !newFile.exists()) {
            boolean success = oldFile.renameTo(newFile);
            if (success) {
                return "File renamed successfully.";
            } else {
                return "File renaming failed.";
            }
        } else {
            return "File not found or new file name already exists.";
        }
    }

    @Override
    public String getFileProperties(String fileName) throws RemoteException {
        File file = new File("server_storage/" + fileName);
        if (file.exists()) {
            return "File: " + file.getName() + "\nSize: " + file.length() + " bytes\nLast Modified: "
                    + file.lastModified();
        } else {
            return "File not found.";
        }
    }

    public static void main(String[] args) {
        try {
            File folder = new File("server_storage");
            if (!folder.exists()) {
                folder.mkdir(); // Crear carpeta si no existe
            }

            RPCServer server = new RPCServer();
            Registry registry = LocateRegistry.createRegistry(1099);
            registry.rebind("FileManager", server);
            System.out.println("RPC Server is running...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
