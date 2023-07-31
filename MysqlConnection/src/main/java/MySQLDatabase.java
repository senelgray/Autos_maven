import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class MySQLDatabase {
    private String url;
    private String username;
    private String password;
    private Connection connection;

    public MySQLDatabase(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    public void connect() throws SQLException {
        connection = DriverManager.getConnection(url, username, password);
    }

    public void disconnect() throws SQLException {
        if (connection != null) {
            connection.close();
        }
    }

    public ResultSet executeQuery(String query) throws SQLException {
        Statement statement = connection.createStatement();
        return statement.executeQuery(query);
    }

    public void createTables() {
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

    public static byte[] obtenerImagenDesdeArchivo(String rutaArchivo) {
        byte[] imagenBytes = null;
        File archivo = new File(rutaArchivo);
        try (FileInputStream fis = new FileInputStream(archivo)) {
            imagenBytes = new byte[(int) archivo.length()];
            fis.read(imagenBytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return imagenBytes;
    }

    public void insertCatalogo(String nombre, byte[] imagen, int precio) {
        try {
            String query = "INSERT INTO catalogo (nombre, imagen, precio) VALUES (?, ?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, nombre);
            preparedStatement.setBytes(2, imagen);
            preparedStatement.setInt(3, precio);

            int rowsAffected = preparedStatement.executeUpdate();
            System.out.println(rowsAffected + " fila(s) insertada(s) en la tabla catalogo.");

            preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insertTipoAuto(int idCoche, String tipo) {
        try {
            String query = "INSERT INTO tipo_auto (idCoche, tipo) VALUES (?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, idCoche);
            preparedStatement.setString(2, tipo);

            int rowsAffected = preparedStatement.executeUpdate();
            System.out.println(rowsAffected + " fila(s) insertada(s) en la tabla tipo_auto.");

            preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // Ruta del archivo de imagen que deseas cargar
        String rutaImagen = "ruta/del/archivo/imagen.png";

        // Obtener la imagen como arreglo de bytes
        byte[] imagen = obtenerImagenDesdeArchivo(rutaImagen);

        String url = "jdbc:mysql://localhost:3306/ejemplos_marco";
        String username = "root";
        String password = "";

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            MySQLDatabase database = new MySQLDatabase(url, username, password);
            database.connect();

            database.createTables();
//          // Ejemplo de consulta
//          String query = "SELECT * FROM empleados";
//          ResultSet resultSet = database.executeQuery(query);
//
//          if (!resultSet.isBeforeFirst()) {
//              System.out.println("No se encontraron resultados.");
//          } else {
//              while (resultSet.next()) {
//                  // Acceder a los valores de las columnas del resultado
//                  String nombre = resultSet.getString("nombre");
//                  int salario = resultSet.getInt("salario");
//                  System.out.println("Nombre: " + nombre);
//                  System.out.println("Salario: " + salario);
//                  System.out.println("-------------------------------------");
//              }
//          }

            // Ejemplo de inserción en la tabla catalogo
            String nombreProducto = "Megan Renault";
            int precioProducto = 200000;
            database.insertCatalogo(nombreProducto, imagen, precioProducto);

            // Ejemplo de inserción en la tabla tipo_auto
            int idCoche = 5;
            String tipoCoche = "Sedán";
            database.insertTipoAuto(idCoche, tipoCoche);

            database.disconnect();
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}

