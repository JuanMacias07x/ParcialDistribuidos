import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.nio.file.Files;

public class DocumentManagerGUI extends JFrame {

    private JTextArea logArea;
    private RPCClient rpcClient;

    public DocumentManagerGUI(RPCClient rpcClient) {
        this.rpcClient = rpcClient;
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Gestor Documental - Cliente RPC");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Panel izquierdo: Explorador de archivos (removido porque era un ejemplo)
        JPanel fileTreePanel = new JPanel(new BorderLayout());
        JLabel treeLabel = new JLabel("Explorador de Archivos");
        JTree fileTree = new JTree(); // Aún no funcional
        fileTreePanel.add(treeLabel, BorderLayout.NORTH);
        fileTreePanel.add(new JScrollPane(fileTree), BorderLayout.CENTER);

        // Panel derecho: Lista de archivos con menú contextual
        JPanel fileListPanel = new JPanel(new BorderLayout());
        JLabel listLabel = new JLabel("Archivos en el servidor");
        DefaultListModel<String> fileListModel = new DefaultListModel<>(); // Modelo para la lista de archivos
        JList<String> fileList = new JList<>(fileListModel); // Lista dinámica
        fileListPanel.add(listLabel, BorderLayout.NORTH);
        fileListPanel.add(new JScrollPane(fileList), BorderLayout.CENTER);

        // Menú contextual
        fileList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    int index = fileList.locationToIndex(e.getPoint());
                    String selectedFile = fileList.getModel().getElementAt(index);
                    showFileOptionsMenu(e.getX(), e.getY(), selectedFile);
                }
            }
        });

        // Panel inferior: Área de log
        logArea = new JTextArea();
        logArea.setEditable(false);
        JScrollPane logScrollPane = new JScrollPane(logArea);

        // Botón para subir archivo
        JButton uploadButton = new JButton("Subir Archivo");
        uploadButton.addActionListener(e -> uploadFile());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(uploadButton);

        add(fileTreePanel, BorderLayout.WEST);
        add(fileListPanel, BorderLayout.CENTER);
        add(logScrollPane, BorderLayout.SOUTH);
        add(buttonPanel, BorderLayout.NORTH);

        setVisible(true);
    }

    private void uploadFile() {
        try {
            JFileChooser fileChooser = new JFileChooser();
            int option = fileChooser.showOpenDialog(this);
            if (option == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                byte[] fileData = Files.readAllBytes(selectedFile.toPath());
                rpcClient.uploadFile(selectedFile.getName(), fileData);
                logArea.append("Archivo subido: " + selectedFile.getName() + "\n");
            }
        } catch (Exception e) {
            logArea.append("Error al subir el archivo.\n");
            e.printStackTrace();
        }
    }

    private void showFileOptionsMenu(int x, int y, String selectedFileName) {
        JPopupMenu menu = new JPopupMenu();

        JMenuItem downloadItem = new JMenuItem("Descargar");
        downloadItem.addActionListener(e -> downloadFile(selectedFileName));

        JMenuItem renameItem = new JMenuItem("Renombrar");
        renameItem.addActionListener(e -> renameFile(selectedFileName));

        JMenuItem propertiesItem = new JMenuItem("Propiedades");
        propertiesItem.addActionListener(e -> showFileProperties(selectedFileName));

        menu.add(downloadItem);
        menu.add(renameItem);
        menu.add(propertiesItem);

        menu.show(this, x, y);
    }

    private void downloadFile(String fileName) {
        try {
            byte[] fileData = rpcClient.downloadFile(fileName);
            if (fileData != null) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setSelectedFile(new File(fileName));
                int option = fileChooser.showSaveDialog(this);
                if (option == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    Files.write(selectedFile.toPath(), fileData);
                    logArea.append("Archivo descargado: " + fileName + "\n");
                }
            } else {
                logArea.append("Error al descargar el archivo.\n");
            }
        } catch (Exception e) {
            logArea.append("Error al descargar el archivo.\n");
            e.printStackTrace();
        }
    }

    private void renameFile(String selectedFileName) {
        String newFileName = JOptionPane.showInputDialog(this, "Nuevo nombre del archivo:", selectedFileName);
        if (newFileName != null && !newFileName.trim().isEmpty()) {
            try {
                rpcClient.renameFile(selectedFileName, newFileName);
                logArea.append("Archivo renombrado: " + selectedFileName + " a " + newFileName + "\n");
            } catch (Exception e) {
                logArea.append("Error al renombrar el archivo.\n");
                e.printStackTrace();
            }
        }
    }

    private void showFileProperties(String selectedFileName) {
        try {
            String properties = rpcClient.getFileProperties(selectedFileName);
            JOptionPane.showMessageDialog(this, properties, "Propiedades del archivo", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            logArea.append("Error al obtener las propiedades del archivo.\n");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            RPCClient rpcClient = new RPCClient();
            SwingUtilities.invokeLater(() -> new DocumentManagerGUI(rpcClient));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
