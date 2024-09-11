import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;

public class DocumentManagerGUI extends JFrame {
    private RPCClient client;
    private JTree fileTree;
    private DefaultTreeModel treeModel;
    private JTextArea logArea;

    public DocumentManagerGUI() {
        client = new RPCClient();
        setTitle("Gestor Documental - Cliente RPC");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        initUI();
        refreshFileList();
    }

    private void initUI() {
        // Layout principalll
        setLayout(new BorderLayout());

        // Crear el panel izquierdo con el árbol de archivos
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Archivos");
        treeModel = new DefaultTreeModel(root);
        fileTree = new JTree(treeModel);
        JScrollPane treeScrollPane = new JScrollPane(fileTree);
        add(treeScrollPane, BorderLayout.CENTER);

        // Panel derecho con botones de acciones
        JPanel actionPanel = new JPanel();
        actionPanel.setLayout(new BoxLayout(actionPanel, BoxLayout.Y_AXIS));
        JButton uploadButton = new JButton("Subir Archivo");
        JButton renameButton = new JButton("Renombrar Archivo");
        JButton propertiesButton = new JButton("Ver Propiedades");
        JButton downloadButton = new JButton("Descargar Archivo");

        actionPanel.add(uploadButton);
        actionPanel.add(renameButton);
        actionPanel.add(propertiesButton);
        actionPanel.add(downloadButton);
        add(actionPanel, BorderLayout.EAST);

        // Área de logs
        logArea = new JTextArea(5, 30);
        logArea.setEditable(false);
        JScrollPane logScrollPane = new JScrollPane(logArea);
        add(logScrollPane, BorderLayout.SOUTH);

        // Acción para subir archivos
        uploadButton.addActionListener(e -> uploadFile());

        // Acción para renombrar archivos
        renameButton.addActionListener(e -> renameFile());

        // Acción para ver propiedades de archivo
        propertiesButton.addActionListener(e -> viewFileProperties());

        // Acción para descargar archivo
        downloadButton.addActionListener(e -> downloadFile());

        // Agregar menú contextual
        fileTree.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger() || SwingUtilities.isRightMouseButton(e)) {
                    int row = fileTree.getClosestRowForLocation(e.getX(), e.getY());
                    fileTree.setSelectionRow(row);
                    showContextMenu(e.getX(), e.getY());
                }
            }
        });
    }

    private void showContextMenu(int x, int y) {
        JPopupMenu contextMenu = new JPopupMenu();
        JMenuItem downloadItem = new JMenuItem("Descargar");
        JMenuItem renameItem = new JMenuItem("Renombrar");
        JMenuItem propertiesItem = new JMenuItem("Propiedades");

        contextMenu.add(downloadItem);
        contextMenu.add(renameItem);
        contextMenu.add(propertiesItem);

        downloadItem.addActionListener(e -> downloadFile());
        renameItem.addActionListener(e -> renameFile());
        propertiesItem.addActionListener(e -> viewFileProperties());

        contextMenu.show(fileTree, x, y);
    }

    private void uploadFile() {
        JFileChooser fileChooser = new JFileChooser();
        int option = fileChooser.showOpenDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                byte[] fileData = Files.readAllBytes(selectedFile.toPath());
                String response = client.uploadFile(selectedFile.getName(), fileData);
                logArea.append(response + "\n");
                refreshFileList();
            } catch (Exception e) {
                e.printStackTrace();
                logArea.append("Error al subir el archivo.\n");
            }
        }
    }

    private void downloadFile() {
        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) fileTree.getLastSelectedPathComponent();
        if (selectedNode != null) {
            String fileName = selectedNode.getUserObject().toString();
            byte[] fileData = client.downloadFile(fileName);
            if (fileData != null) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setSelectedFile(new File(fileName));
                int option = fileChooser.showSaveDialog(this);
                if (option == JFileChooser.APPROVE_OPTION) {
                    try {
                        File file = fileChooser.getSelectedFile();
                        Files.write(file.toPath(), fileData);
                        logArea.append("Archivo descargado: " + fileName + "\n");
                    } catch (Exception e) {
                        e.printStackTrace();
                        logArea.append("Error al descargar el archivo.\n");
                    }
                }
            } else {
                logArea.append("El archivo no existe o hubo un error en la descarga.\n");
            }
        } else {
            logArea.append("Selecciona un archivo para descargar.\n");
        }
    }

    private void renameFile() {
        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) fileTree.getLastSelectedPathComponent();
        if (selectedNode != null) {
            String oldFileName = selectedNode.getUserObject().toString();
            String newFileName = JOptionPane.showInputDialog(this, "Nuevo nombre:", oldFileName);
            if (newFileName != null && !newFileName.trim().isEmpty()) {
                String response = client.renameFile(oldFileName, newFileName);
                logArea.append(response + "\n");
                refreshFileList();
            }
        } else {
            logArea.append("Selecciona un archivo para renombrar.\n");
        }
    }

    private void viewFileProperties() {
        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) fileTree.getLastSelectedPathComponent();
        if (selectedNode != null) {
            String fileName = selectedNode.getUserObject().toString();
            String properties = client.getFileProperties(fileName);
            JOptionPane.showMessageDialog(this, properties, "Propiedades del archivo", JOptionPane.INFORMATION_MESSAGE);
        } else {
            logArea.append("Selecciona un archivo para ver sus propiedades.\n");
        }
    }

    private void refreshFileList() {
        try {
            String[] files = client.listFiles();
            DefaultMutableTreeNode root = (DefaultMutableTreeNode) treeModel.getRoot();
            root.removeAllChildren();
            for (String file : files) {
                root.add(new DefaultMutableTreeNode(file));
            }
            treeModel.reload();
        } catch (Exception e) {
            e.printStackTrace();
            logArea.append("Error al actualizar la lista de archivos.\n");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            DocumentManagerGUI gui = new DocumentManagerGUI();
            gui.setVisible(true);
        });
    }
}
