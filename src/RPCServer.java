import java.io.*;
import java.rmi.*;
import java.rmi.registry.*;
import java.rmi.server.*;

public class RPCServer extends UnicastRemoteObject implements FileManager {

    public RPCServer() throws RemoteException {
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
            byte[] fileData = new byte[(int) file.length()];
            FileInputStream fis = new FileInputStream(file);
            fis.read(fileData);
            fis.close();
            return fileData;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void renameFile(String oldFileName, String newFileName) throws RemoteException {
        File oldFile = new File("server_storage/" + oldFileName);
        File newFile = new File("server_storage/" + newFileName);
        if (oldFile.exists()) {
            oldFile.renameTo(newFile);
        } else {
            throw new RemoteException("El archivo no existe.");
        }
    }

    @Override
    public String getFileProperties(String fileName) throws RemoteException {
        File file = new File("server_storage/" + fileName);
        if (file.exists()) {
            long fileSize = file.length();
            long lastModified = file.lastModified();
            return "Nombre: " + file.getName() + "\nTamaño: " + fileSize + " bytes\nÚltima modificación: "
                    + new java.util.Date(lastModified);
        } else {
            throw new RemoteException("El archivo no existe.");
        }
    }

    public static void main(String[] args) {
        try {
            RPCServer server = new RPCServer();
            Registry registry = LocateRegistry.createRegistry(1099);
            registry.rebind("FileManager", server);
            System.out.println("Servidor RPC listo.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
