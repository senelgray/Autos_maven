package CatalogoAutomoviles;
import java.io.ByteArrayOutputStream; 

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

public class CatalogoAutomoviles extends JFrame {

    private static Connection connection;

    private JTextField txtBusqueda;
    private JButton btnBuscar;
    private JPanel panelAutomoviles;

    public CatalogoAutomoviles() {
        setTitle("Catálogo de Automóviles");
        setSize(800, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setLayout(new BorderLayout());

        connectToDatabase(); // Conectar a la base de datos

        JPanel panelBusqueda = new JPanel();
        txtBusqueda = new JTextField(20);
        btnBuscar = new JButton("Buscar");
        btnBuscar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String filtro = txtBusqueda.getText();
                filtrarAutomoviles(filtro);
            }
        });
        JButton btnAgregarAutomovil = new JButton("Agregar Automóvil");
        btnAgregarAutomovil.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                VentanaAgregarAutomovil ventanaAgregar = new VentanaAgregarAutomovil();
                ventanaAgregar.setVisible(true);
            }
        });
        panelBusqueda.add(txtBusqueda);
        panelBusqueda.add(btnBuscar);
        panelBusqueda.add(btnAgregarAutomovil);
        getContentPane().add(panelBusqueda, BorderLayout.NORTH);

        panelAutomoviles = new JPanel(new GridLayout(0, 3, 10, 10));
        cargarAutomoviles();
        getContentPane().add(panelAutomoviles, BorderLayout.CENTER);
        panelAutomoviles.setBackground(new Color(0, 128, 192));

        JScrollPane scrollPane = new JScrollPane(panelAutomoviles);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        getContentPane().add(scrollPane, BorderLayout.CENTER);

        setVisible(true);
    }

    private void connectToDatabase() {
        String url = "jdbc:mysql://localhost:3306/ejemplos_marco";
        String username = "root";
        String password = "";

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(url, username, password);
            createTables(); // Crear las tablas si no existen
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al conectar a la base de datos", "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1); // Salir del programa si no se puede conectar a la base de datos
        }
    }

    private void createTables() {
        try {
            String catalogoTableQuery = "CREATE TABLE IF NOT EXISTS catalogo (id INT PRIMARY KEY AUTO_INCREMENT, nombre VARCHAR(100), imagen LONGBLOB, precio INT)";
            String tipoTablasQuery = "CREATE TABLE IF NOT EXISTS tipo_auto (id INT PRIMARY KEY AUTO_INCREMENT, idCoche INT,  tipo VARCHAR(100))";

            Statement statement = connection.createStatement();
            statement.executeUpdate(catalogoTableQuery);
            statement.executeUpdate(tipoTablasQuery);	
            System.out.println("Tablas creadas correctamente.");

            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void cargarAutomoviles() {
        panelAutomoviles.removeAll();

        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM catalogo");

            while (resultSet.next()) {
                final String nombre = resultSet.getString("nombre");
                final int precio = resultSet.getInt("precio");
                byte[] imagenBytes = resultSet.getBytes("imagen");

                BufferedImage resizedImage = resizeImage(imagenBytes, 200, 150);
                if (resizedImage != null) {
                    JLabel lblImagen = new JLabel(new ImageIcon(resizedImage));
                    JPanel panelAutomovil = new JPanel(new BorderLayout());
                    panelAutomovil.add(lblImagen, BorderLayout.CENTER);

                    JLabel lblNombre = new JLabel(nombre);
                    panelAutomovil.add(lblNombre, BorderLayout.NORTH);

                    JButton btnInfo = new JButton("Info");
                    btnInfo.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            mostrarInformacion(nombre, precio);
                        }
                    });
                    panelAutomovil.add(btnInfo, BorderLayout.SOUTH);

                    panelAutomoviles.add(panelAutomovil);
                }
            }


            statement.close();
            resultSet.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        panelAutomoviles.revalidate();
        panelAutomoviles.repaint();
    }

    private BufferedImage resizeImage(byte[] imageBytes, int width, int height) {
        try {
            if (imageBytes == null || imageBytes.length == 0) {
                return null;
            }

            ByteArrayInputStream bais = new ByteArrayInputStream(imageBytes); // Importar ByteArrayInputStream
            BufferedImage originalImage = ImageIO.read(bais);
            Image resizedImage = originalImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);

            BufferedImage resizedBufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = resizedBufferedImage.createGraphics();
            g2d.drawImage(resizedImage, 0, 0, null);
            g2d.dispose();

            return resizedBufferedImage;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private void mostrarInformacion(String nombre, int precio) {
    	String mensaje = "Nombre: " + nombre + "\nPrecio: " + precio + "\nTipo: ";

        try {
            String tipo = obtenerTipoAutomovil(nombre);
            mensaje += tipo;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        JOptionPane.showMessageDialog(this, mensaje, "Información del Automóvil", JOptionPane.INFORMATION_MESSAGE);
    }
    private String obtenerTipoAutomovil(String nombre) throws SQLException {
        String tipo = null;
        String query = "SELECT tipo FROM tipo_auto WHERE idCoche IN (SELECT id FROM catalogo WHERE nombre = ?)";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setString(1, nombre);
        ResultSet resultSet = preparedStatement.executeQuery();

        if (resultSet.next()) {
            tipo = resultSet.getString("tipo");
        }

        preparedStatement.close();
        resultSet.close();

        return tipo;
    }
    private void filtrarAutomoviles(String filtro) {
        panelAutomoviles.removeAll();

        try {
            String query = "SELECT * FROM catalogo WHERE nombre LIKE ? OR precio LIKE ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, "%" + filtro + "%");
            preparedStatement.setString(2, "%" + filtro + "%");
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String nombre = resultSet.getString("nombre");
                int precio = resultSet.getInt("precio");
                byte[] imagenBytes = resultSet.getBytes("imagen");

                BufferedImage resizedImage = resizeImage(imagenBytes, 200, 150);
                if (resizedImage != null) {
                    JLabel lblImagen = new JLabel(new ImageIcon(resizedImage));
                    JPanel panelAutomovil = new JPanel(new BorderLayout());
                    panelAutomovil.add(lblImagen, BorderLayout.CENTER);

                    JLabel lblNombre = new JLabel(nombre);
                    panelAutomovil.add(lblNombre, BorderLayout.NORTH);

                    JButton btnInfo = new JButton("Info");
                    btnInfo.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            mostrarInformacion(nombre, precio);
                        }
                    });
                    panelAutomovil.add(btnInfo, BorderLayout.SOUTH);

                    panelAutomoviles.add(panelAutomovil);
                }
            }

            preparedStatement.close();
            resultSet.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        panelAutomoviles.revalidate();
        panelAutomoviles.repaint();
    }
    private void insertarNuevoAutomovil(String nombre, int precio, byte[] imagenBytes, String tipo) throws SQLException {
        String insertQueryCatalogo = "INSERT INTO catalogo (nombre, imagen, precio) VALUES (?, ?, ?)";
        PreparedStatement preparedStatementCatalogo = connection.prepareStatement(insertQueryCatalogo, Statement.RETURN_GENERATED_KEYS);
        preparedStatementCatalogo.setString(1, nombre);
        preparedStatementCatalogo.setBytes(2, imagenBytes);
        preparedStatementCatalogo.setInt(3, precio);
        preparedStatementCatalogo.executeUpdate();

        ResultSet rs = preparedStatementCatalogo.getGeneratedKeys();
        int idCoche = 0;
        if (rs.next()) {
            idCoche = rs.getInt(1);
        }

        preparedStatementCatalogo.close();

        String insertQueryTipoAuto = "INSERT INTO tipo_auto (idCoche, tipo) VALUES (?, ?)";
        PreparedStatement preparedStatementTipoAuto = connection.prepareStatement(insertQueryTipoAuto);
        preparedStatementTipoAuto.setInt(1, idCoche);
        preparedStatementTipoAuto.setString(2, tipo);
        preparedStatementTipoAuto.executeUpdate();

        preparedStatementTipoAuto.close();
    }
    public class VentanaAgregarAutomovil extends JFrame {

        private JTextField txtNombre;
        private JTextField txtPrecio;
        private JTextField txtTipo;
        private JLabel lblImagen;

        public VentanaAgregarAutomovil() {
            setTitle("Agregar Nuevo Automóvil");
            setSize(400, 300);
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            setLocationRelativeTo(CatalogoAutomoviles.this);
            getContentPane().setLayout(new BorderLayout());

            JPanel panelDatos = new JPanel(new GridLayout(4, 2, 10, 10));
            txtNombre = new JTextField(20);
            txtPrecio = new JTextField(20);
            txtTipo = new JTextField(20);
            lblImagen = new JLabel();
            JButton btnSeleccionarImagen = new JButton("Seleccionar Imagen");
            btnSeleccionarImagen.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    JFileChooser fileChooser = new JFileChooser();
                    FileNameExtensionFilter filter = new FileNameExtensionFilter("Imágenes", "jpg", "jpeg", "png", "gif");
                    fileChooser.setFileFilter(filter);

                    int seleccion = fileChooser.showOpenDialog(VentanaAgregarAutomovil.this);
                    if (seleccion == JFileChooser.APPROVE_OPTION) {
                        try {
                            BufferedImage imagen = ImageIO.read(fileChooser.getSelectedFile());
                            ImageIcon icono = new ImageIcon(imagen.getScaledInstance(200, 150, Image.SCALE_SMOOTH));
                            lblImagen.setIcon(icono);
                        } catch (IOException ex) {
                            ex.printStackTrace();
                            JOptionPane.showMessageDialog(VentanaAgregarAutomovil.this, "Error al cargar la imagen", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            });

            panelDatos.add(new JLabel("Nombre:"));
            panelDatos.add(txtNombre);
            panelDatos.add(new JLabel("Precio:"));
            panelDatos.add(txtPrecio);
            panelDatos.add(new JLabel("Tipo:"));
            panelDatos.add(txtTipo);
            panelDatos.add(new JLabel("Imagen:"));
            panelDatos.add(btnSeleccionarImagen);

            getContentPane().add(panelDatos, BorderLayout.NORTH);

            JButton btnAgregar = new JButton("Agregar");
            btnAgregar.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    String nombre = txtNombre.getText();
                    int precio = Integer.parseInt(txtPrecio.getText());
                    String tipo = txtTipo.getText();

                    if (nombre.isEmpty() || tipo.isEmpty()) {
                        JOptionPane.showMessageDialog(VentanaAgregarAutomovil.this, "Por favor, complete todos los campos", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    Icon icono = lblImagen.getIcon();
                    if (icono == null) {
                        JOptionPane.showMessageDialog(VentanaAgregarAutomovil.this, "Por favor, seleccione una imagen", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    BufferedImage imagen = new BufferedImage(icono.getIconWidth(), icono.getIconHeight(), BufferedImage.TYPE_INT_RGB);
                    Graphics g = imagen.getGraphics();
                    icono.paintIcon(null, g, 0, 0);
                    g.dispose();

                    try {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        ImageIO.write(imagen, "jpg", baos);
                        byte[] imagenBytes = baos.toByteArray();
                        insertarNuevoAutomovil(nombre, precio, imagenBytes, tipo);
                        cargarAutomoviles(); // Actualizar la lista de automóviles en el catálogo
                        JOptionPane.showMessageDialog(VentanaAgregarAutomovil.this, "Automóvil agregado correctamente", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                        dispose();
                    } catch (SQLException | IOException ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(VentanaAgregarAutomovil.this, "Error al agregar el automóvil", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });

            getContentPane().add(btnAgregar, BorderLayout.SOUTH);

            setVisible(true);
        }
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new CatalogoAutomoviles();
            }
        });
    }
}
