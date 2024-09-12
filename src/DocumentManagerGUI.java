import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.nio.file.Files;
import java.util.Enumeration;

public class DocumentManagerGUI extends JFrame {
    private RPCClient client;
    private JTree fileTree;
    private DefaultTreeModel treeModel;
    private JTextArea logArea;
    private JTextField searchField; // Campo de búsqueda

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
        setLayout(new BorderLayout());

        // Añadir barra de búsqueda en la parte superior
        searchField = new JTextField(20);
        add(searchField, BorderLayout.NORTH);

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
        JButton createFolderButton = new JButton("Crear Carpeta");
        JButton deleteButton = new JButton("Eliminar");
        JButton moveButton = new JButton("Mover Archivo");
        actionPanel.add(uploadButton);
        actionPanel.add(renameButton);
        actionPanel.add(propertiesButton);
        actionPanel.add(downloadButton);
        actionPanel.add(createFolderButton);
        actionPanel.add(deleteButton);
        actionPanel.add(moveButton);
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

        // Acción para crear carpeta
        createFolderButton.addActionListener(e -> createFolder());

        // Acción para eliminar archivo/carpeta
        deleteButton.addActionListener(e -> deleteFile());

        // Acción para mover archivo
        moveButton.addActionListener(e -> moveFile());

        // Añadir la funcionalidad de búsqueda
        searchField.addActionListener(e -> searchFile(searchField.getText()));

        fileTree.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger() || SwingUtilities.isRightMouseButton(e)) {
                    int row = fileTree.getClosestRowForLocation(e.getX(), e.getY());
                    fileTree.setSelectionRow(row);
                    showContextMenu(e.getX(), e.getY());
                }
            }
        });

        // Renderizador de íconos personalizados
        DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
        renderer.setLeafIcon(UIManager.getIcon("FileView.fileIcon"));
        renderer.setOpenIcon(UIManager.getIcon("FileView.directoryIcon"));
        renderer.setClosedIcon(UIManager.getIcon("FileView.directoryIcon"));
        fileTree.setCellRenderer(renderer);
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

    private void createFolder() {
        String folderName = JOptionPane.showInputDialog(this, "Nombre de la nueva carpeta (use '/' para subcarpetas):");
        if (folderName != null && !folderName.trim().isEmpty()) {
            String path = "server_storage/" + folderName;
            boolean created = client.createFolder(path);
            if (created) {
                logArea.append("Carpeta creada: " + folderName + "\n");
                refreshFileList();
            } else {
                logArea.append("Error al crear la carpeta. Puede que ya exista.\n");
            }
        }
    }

    private void deleteFile() {
        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) fileTree.getLastSelectedPathComponent();
        if (selectedNode != null) {
            String fileName = selectedNode.getUserObject().toString();
            int confirm = JOptionPane.showConfirmDialog(this, "¿Estás seguro de que quieres eliminar " + fileName + "?", "Confirmar eliminación", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                String response = client.deleteFile(fileName);
                logArea.append(response + "\n");
                refreshFileList();
            }
        } else {
            logArea.append("Selecciona un archivo o carpeta para eliminar.\n");
        }
    }

    private void moveFile() {
        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) fileTree.getLastSelectedPathComponent();
        if (selectedNode != null) {
            String fileName = selectedNode.getUserObject().toString();
            String targetFolder = JOptionPane.showInputDialog(this, "Especifique la carpeta de destino:");
            if (targetFolder != null && !targetFolder.trim().isEmpty()) {
                String response = client.moveFile(fileName, targetFolder);
                logArea.append(response + "\n");
                refreshFileList();
            }
        } else {
            logArea.append("Selecciona un archivo para mover.\n");
        }
    }

    private void refreshFileList() {
        try {
            String[] files = client.listFiles();
            DefaultMutableTreeNode root = (DefaultMutableTreeNode) treeModel.getRoot();
            root.removeAllChildren();
            for (String fileName : files) {
                File file = new File("server_storage/" + fileName);
                DefaultMutableTreeNode node;
                if (file.isDirectory()) {
                    node = new DefaultMutableTreeNode(fileName + "/"); // Indica que es una carpeta
                } else {
                    node = new DefaultMutableTreeNode(fileName); // Es un archivo
                }
                root.add(node);
            }
            treeModel.reload();
        } catch (Exception e) {
            e.printStackTrace();
            logArea.append("Error al actualizar la lista de archivos.\n");
        }
    }

    // Método para buscar archivos en el árbol
    private void searchFile(String query) {
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) treeModel.getRoot();
        Enumeration<?> e = root.breadthFirstEnumeration(); // Recorre todos los nodos del árbol
        while (e.hasMoreElements()) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.nextElement();
            String nodeName = node.getUserObject().toString();
            if (nodeName.toLowerCase().contains(query.toLowerCase())) {
                // Si coincide, expandimos el árbol y seleccionamos el nodo
                TreePath path = new TreePath(node.getPath());
                fileTree.setSelectionPath(path);
                fileTree.scrollPathToVisible(path);
                break; // Detenemos la búsqueda tras encontrar el primer resultado
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            DocumentManagerGUI gui = new DocumentManagerGUI();
            gui.setVisible(true);
        });
    }
}
