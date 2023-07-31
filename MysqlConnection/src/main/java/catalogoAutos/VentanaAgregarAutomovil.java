package catalogoAutos;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

class VentanaAgregarAutomovil extends JFrame {
    private JTextField txtNuevoNombre;
    private JTextField txtNuevoTipo;
    private JTextField txtNuevoPrecio;
    private JButton btnAgregar;
    private JButton btnCargarImagen;
    private JLabel lblImagen;

    public VentanaAgregarAutomovil() {
        setTitle("Agregar Automóvil");
        setSize(300, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        txtNuevoNombre = new JTextField(15);
        txtNuevoNombre.setBounds(102, 7, 132, 20);
        txtNuevoTipo = new JTextField(15);
        txtNuevoTipo.setBounds(102, 27, 132, 20);
        txtNuevoPrecio = new JTextField(15);
        txtNuevoPrecio.setBounds(102, 52, 132, 20);
        btnAgregar = new JButton("Agregar");
        btnAgregar.setBounds(60, 320, 74, 20);
        btnAgregar.setHorizontalAlignment(SwingConstants.RIGHT);
        btnAgregar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                agregarAutomovil();
            }
        });

        btnCargarImagen = new JButton("Cargar Imagen");
        btnCargarImagen.setBounds(102, 77, 132, 20);
        btnCargarImagen.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cargarImagen();
            }
        });

        lblImagen = new JLabel();
        lblImagen.setBounds(10, 110, 280, 180);

        getContentPane().setLayout(null);
        getContentPane().add(txtNuevoNombre);
        getContentPane().add(txtNuevoTipo);
        getContentPane().add(txtNuevoPrecio);
        getContentPane().add(btnAgregar);
        getContentPane().add(btnCargarImagen);
        getContentPane().add(lblImagen);

        JLabel label = new JLabel("Nombre:");
        label.setBounds(33, 8, 62, 12);
        getContentPane().add(label);

        JLabel label_1 = new JLabel("Tipo:");
        label_1.setBounds(33, 28, 35, 12);
        getContentPane().add(label_1);

        JLabel label_2 = new JLabel("Precio:");
        label_2.setBounds(33, 47, 62, 17);
        getContentPane().add(label_2);

        setVisible(true);
    }

    private void agregarAutomovil() {
        String nombre = txtNuevoNombre.getText();
        String tipo = txtNuevoTipo.getText();
        String precio = txtNuevoPrecio.getText();
        String imagen = lblImagen.getText();

        if (!nombre.isEmpty() && !tipo.isEmpty() && !precio.isEmpty() && !imagen.isEmpty()) {
            // Insertar en la tabla catalogo
        	database.insertCatalogo(nombre, obtenerBytesDeImagen(imagen), Integer.parseInt(precio));

            // Insertar en la tabla tipo_auto (Usar el id generado por la tabla catalogo)
            int idCoche = obtenerUltimoIdInsertado();
            database.insertTipoAuto(idCoche, tipo);

            // Cerrar la ventana después de agregar el automóvil
            dispose();
        }
    }

    private void cargarImagen() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Archivos de imagen", "jpg", "jpeg"));

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            String imagePath = selectedFile.getAbsolutePath();

            lblImagen.setText(imagePath);
            BufferedImage resizedImage = CatalogoAutomoviles.resizeImage(imagePath, lblImagen.getWidth(),
                    lblImagen.getHeight());
            if (resizedImage != null) {
                ImageIcon imageIcon = new ImageIcon(resizedImage);
                lblImagen.setIcon(imageIcon);
            } else {
                lblImagen.setIcon(null);
            }
        }
    }

    private int obtenerUltimoIdInsertado() {
        try {
            String query = "SELECT LAST_INSERT_ID()";
            ResultSet rs = database.executeQuery(query);
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    private byte[] obtenerBytesDeImagen(String imagePath) {
        try {
            BufferedImage originalImage = ImageIO.read(new File(imagePath));
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(originalImage, "jpg", baos);
            return baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
