import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class RPCClient {
    private FileManager fileManager;

    public RPCClient() {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            fileManager = (FileManager) registry.lookup("FileManager");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String uploadFile(String fileName, byte[] fileData) {
        try {
            return fileManager.uploadFile(fileName, fileData);
        } catch (Exception e) {
            e.printStackTrace();
            return "Error uploading file.";
        }
    }

    public byte[] downloadFile(String fileName) {
        try {
            return fileManager.downloadFile(fileName);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String[] listFiles() {
        try {
            return fileManager.listFiles();
        } catch (Exception e) {
            e.printStackTrace();
            return new String[] {};
        }
    }

    public String renameFile(String oldFileName, String newFileName) {
        try {
            return fileManager.renameFile(oldFileName, newFileName);
        } catch (Exception e) {
            e.printStackTrace();
            return "Error renaming file.";
        }
    }

    public String getFileProperties(String fileName) {
        try {
            return fileManager.getFileProperties(fileName);
        } catch (Exception e) {
            e.printStackTrace();
            return "Error retrieving file properties.";
        }
    }
}
