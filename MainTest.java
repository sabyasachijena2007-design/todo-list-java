import java.sql.*;

public class MainTest {
    public static void main(String[] args) {
        try {
            Connection con = DBConnection.getConnection();

            String sql = "INSERT INTO tasks (text, priority, done, date) VALUES (?, ?, ?, ?)";

            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, "Test Task");
            ps.setString(2, "HIGH");
            ps.setBoolean(3, false);
            ps.setDate(4, java.sql.Date.valueOf("2026-04-25"));

            ps.executeUpdate();

            System.out.println("DATA INSERTED");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
