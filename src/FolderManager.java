import java.io.File;

public class FolderManager {

    // Crear una carpeta o subcarpeta en la ruta especificada
    public boolean createFolder(String path) {
        File folder = new File(path);
        if (!folder.exists()) {
            return folder.mkdirs(); // Crea la carpeta y cualquier subcarpeta necesaria
        }
        return false; // Si ya existe, no la crea
    }
}
