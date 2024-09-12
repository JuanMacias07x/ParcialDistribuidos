import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
        return folder.list(); // Devuelve una lista de nombres de archivos y carpetas
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

    @Override
    public boolean createFolder(String path) throws RemoteException {
        File folder = new File(path);
        if (!folder.exists()) {
            return folder.mkdirs();
        }
        return false;
    }

    @Override
    public String deleteFile(String fileName) throws RemoteException {
        File file = new File("server_storage/" + fileName);
        if (file.exists()) {
            if (file.isDirectory()) {
                file.delete(); // Borra la carpeta
            } else {
                file.delete(); // Borra el archivo
            }
            return "File deleted successfully.";
        } else {
            return "File not found.";
        }
    }

    @Override
    public String moveFile(String fileName, String targetFolder) throws RemoteException {
        File file = new File("server_storage/" + fileName);
        File targetDir = new File("server_storage/" + targetFolder);
        if (file.exists() && targetDir.isDirectory()) {
            File newFile = new File(targetDir, file.getName());
            if (file.renameTo(newFile)) {
                return "File moved successfully.";
            } else {
                return "Failed to move file.";
            }
        } else {
            return "File or target folder not found.";
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
