import java.util.HashMap;
import java.util.Map;

public class MetadataManager {
    private Map<String, Map<String, String>> metadataStore = new HashMap<>();

    // Agregar metadatos de un archivo
    public void addFileMetadata(String fileName, Map<String, String> metadata) {
        metadataStore.put(fileName, metadata);
    }

    // Buscar archivos por metadato
    public String searchByMetadata(String key, String value) {
        for (Map.Entry<String, Map<String, String>> entry : metadataStore.entrySet()) {
            if (entry.getValue().containsKey(key) && entry.getValue().get(key).equals(value)) {
                return entry.getKey(); // Devuelve el nombre del archivo si coincide
            }
        }
        return "Archivo no encontrado";
    }
}
