import java.io.File;

public class NFSServer {

    public static void main(String[] args) {
        String nfsMountPath = "/mnt/nfs_server"; // Ruta al montaje NFS
        File nfsDirectory = new File(nfsMountPath);

        if (nfsDirectory.exists()) {
            System.out.println("NFS Mounted at: " + nfsMountPath);
            // Lógica para gestionar archivos en el montaje NFS
        } else {
            System.out.println("Error: NFS not mounted.");
        }
    }
}
