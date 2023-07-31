import java.sql.DriverManager;
import java.sql.SQLException;

public class Demo {

	public static void main(String[] args) {
		String url="jdbc:mysql://localhost:3306/test";
		String user="root";
		String password="";
		
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			DriverManager.getConnection(url,user,password);
			System.out.println("Connection is Succesful to the database" +url);
			
		}catch (ClassNotFoundException e) {
			e.printStackTrace();
		}catch (SQLException throwables) {
			throwables.printStackTrace();
		}
	}
}
