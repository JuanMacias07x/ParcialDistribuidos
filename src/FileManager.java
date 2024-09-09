import java.rmi.Remote;
import java.rmi.RemoteException;

public interface FileManager extends Remote {
    String uploadFile(String fileName, byte[] fileData) throws RemoteException;

    byte[] downloadFile(String fileName) throws RemoteException;

    void renameFile(String oldFileName, String newFileName) throws RemoteException;

    String getFileProperties(String fileName) throws RemoteException;
}
