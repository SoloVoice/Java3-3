import java.sql.*;

public class SQLHandler {
    private static Connection connection;
    private static Statement statement;

    public static void connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:server/src/main/resources/usersdb.db");
            statement = connection.createStatement();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void disconnect() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static String getNickByLoginAndPassword(String login, String password) {
        try {
            ResultSet rs = statement.executeQuery("SELECT nick FROM users WHERE login ='" + login + "' AND pass = '" + password + "'");
            if (rs.next()) {
                return rs.getString("nick");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    public static String changeNickInBD(String newNick, String oldNick) {
        try {
            statement.executeUpdate("UPDATE users SET nick ='" + newNick + "' WHERE nick = '" + oldNick + "'");
            return newNick;
        } catch (SQLException throwables) {
            return oldNick;
        }
    }
}
