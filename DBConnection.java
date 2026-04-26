import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnection {

    public static Connection getConnection() {
        try {
            Connection con = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/todo_db",
                "root",
                "mysql1234"
            );

           System.out.println("CONNECTED SUCCESSFULLY");
            return con;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}