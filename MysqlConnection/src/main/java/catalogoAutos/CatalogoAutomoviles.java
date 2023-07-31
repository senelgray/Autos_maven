package catalogoAutos;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;


public class CatalogoAutomoviles extends JFrame {

    private static List<Automovil> automoviles;
    private JTextField txtBusqueda;
    private JButton btnBuscar;
    private JPanel panelAutomoviles;

    private static MySQLDatabase database; // Database object

    public CatalogoAutomoviles() {
        setTitle("Catálogo de Automóviles");
        setSize(800, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setLayout(new BorderLayout());

        // Initialize the database connection
        String url = "jdbc:mysql://localhost:3306/ejemplos_marco";
        String username = "root";
        String password = "";
        database = new MySQLDatabase(url, username, password);

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            database.connect();

            // Create the necessary tables if they don't exist
            database.createTables();
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        automoviles = new ArrayList<>();
        automoviles.add(new Automovil("Toyota Corolla", "Sedán", "$ 255,455.00", "imagen_corolla.jpg"));
        automoviles.add(new Automovil("Ford Mustang", "Deportivo", "$ 487,980.00", "imagen_mustang.jpg"));
        automoviles.add(new Automovil("Volkswagen Golf", "Hatchback", "$320,098.00", "imagen_golf.jpg"));
        automoviles.add(new Automovil("Toyota Yaris", "Hatchback", "$ 307,900.00", "imagen_yaris.jpg"));
        automoviles.add(new Automovil("Ford Fiesta", "Deportivo", "$ 241,300.00", "imagen_fiesta.jpg"));
        automoviles.add(new Automovil("Volkswagen Jetta", "Sedán", "$ 399,990.00", "imagen_jetta.jpg"));
        automoviles.add(new Automovil("Nissan Sentra", "Sedán", "$ 396,900.00", "imagen_sentra.jpg"));
        automoviles.add(new Automovil("Nissan Versa", "Hatchback", "$ 487,980.00", "imagen_versa.jpg"));
        automoviles.add(new Automovil("Honda Civic", "Sedán", "$ 545,900.00", "imagen_civic.jpg"));
        automoviles.add(new Automovil("Honda Accord", "Sedán", "$ 779,900.00", "imagen_accord.jpg"));
        automoviles.add(new Automovil("Seat Ibiza", "Hatchback", "$ 342,900.00", "imagen_ibiza.jpg"));
        automoviles.add(new Automovil("Seat Leon", "Sedán", "$ 590,090.00", "imagen_leon.jpg"));

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
            }
        });
        panelBusqueda.add(txtBusqueda);
        panelBusqueda.add(btnBuscar);
        panelBusqueda.add(btnAgregarAutomovil);
        getContentPane().add(panelBusqueda, BorderLayout.NORTH);

        panelAutomoviles = new JPanel(new GridLayout(0, 3, 10, 10));
        cargarAutomoviles(automoviles);
        getContentPane().add(panelAutomoviles, BorderLayout.CENTER);
        panelAutomoviles.setBackground(new Color(0, 128, 192));

        JScrollPane scrollPane = new JScrollPane(panelAutomoviles);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        getContentPane().add(scrollPane, BorderLayout.CENTER);

        setVisible(true);
    }

    private void cargarAutomoviles(List<Automovil> automoviles) {
        panelAutomoviles.removeAll();

        for (Automovil automovil : automoviles) {
            JPanel panelAutomovil = new JPanel(new BorderLayout());
            JLabel lblImagen = new JLabel();
            BufferedImage resizedImage = resizeImage(automovil.getImagen(), 200, 150);
            lblImagen.setIcon(new ImageIcon(resizedImage));
            JLabel lblNombre = new JLabel(automovil.getNombre());
            JLabel lblTipo = new JLabel(automovil.getTipo());
            JLabel lblPrecio = new JLabel(automovil.getPrecio());

            panelAutomovil.add(lblImagen, BorderLayout.CENTER);
            panelAutomovil.add(lblNombre, BorderLayout.NORTH);

            JButton btnInfo = new JButton("Info");
            btnInfo.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    mostrarInformacion(automovil);
                }
            });
            panelAutomovil.add(btnInfo, BorderLayout.SOUTH);
            panelAutomoviles.add(panelAutomovil);
        }

        panelAutomoviles.revalidate();
        panelAutomoviles.repaint();
    }

    public static void agregarAutomovil(String nombre, String tipo, String precio, String imagen) {
        automoviles.add(new Automovil(nombre, tipo, precio, imagen));
        CatalogoAutomoviles catalogo = new CatalogoAutomoviles();
        catalogo.cargarAutomoviles(automoviles);
    }

    private void cargarImagen() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Seleccionar imagen");
        int seleccion = fileChooser.showOpenDialog(this);
        if (seleccion == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            String imagePath = file.getAbsolutePath();
            ImageIcon icon = new ImageIcon(imagePath);

        }
    }

    private void mostrarInformacion(Automovil automovil) {
        JOptionPane.showMessageDialog(this, automovil.toString(), "Información del Automóvil",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void filtrarAutomoviles(String filtro) {
        List<Automovil> automovilesFiltrados = new ArrayList<>();

        for (Automovil automovil : automoviles) {
            if (automovil.getNombre().toLowerCase().contains(filtro.toLowerCase())
                    || automovil.getTipo().toLowerCase().contains(filtro.toLowerCase())
                    || automovil.getPrecio().toLowerCase().contains(filtro.toLowerCase())) {
                automovilesFiltrados.add(automovil);
            }
        }

        cargarAutomoviles(automovilesFiltrados);
    }

    public static BufferedImage resizeImage(String imagePath, int width, int height) {
        try {
            File originalFile = new File(imagePath);
            BufferedImage originalImage = ImageIO.read(originalFile);

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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                CatalogoAutomoviles catalogo = new CatalogoAutomoviles();
            }
        });
    }
}