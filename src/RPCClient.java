
import java.rmi.Naming;

public class RPCClient {
    private FileManager fileManager;

    public RPCClient() throws Exception {
        fileManager = (FileManager) Naming.lookup("rmi://localhost:1099/FileManager");
        System.out.println("Conectado al servidor RPC");
    }

    public void uploadFile(String fileName, byte[] fileData) throws Exception {
        fileManager.uploadFile(fileName, fileData);
    }

    public byte[] downloadFile(String fileName) throws Exception {
        return fileManager.downloadFile(fileName);
    }

    public void renameFile(String oldFileName, String newFileName) throws Exception {
        fileManager.renameFile(oldFileName, newFileName);
    }

    public String getFileProperties(String fileName) throws Exception {
        return fileManager.getFileProperties(fileName);
    }
}
