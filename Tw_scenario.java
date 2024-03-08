package tw_scenario;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class Tw_scenario implements AutoCloseable {

    private Connection conn;
    private static final String URL = "jdbc:postgresql://localhost:5432/tw_scenario";
    private static final String USER = "postgres";
    private static final String PASSWORD = "admin";

    public Tw_scenario() throws SQLException {
        this.conn = DriverManager.getConnection(URL, USER, PASSWORD);
    }

    @Override
    public void close() throws Exception {
        if (conn != null && !conn.isClosed()) {
            conn.close();
        }
    }

    public String authenticateUser(String email, String password) throws SQLException {
        String sql = "SELECT * FROM users WHERE email = ? AND password = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            pstmt.setString(2, password);

            try (ResultSet rs = pstmt.executeQuery()) {
                if(rs.next()) {
                    return rs.getString("user_id");
                }
                return null; 
            }
        }
    }

    public ResultSet getUserMessages(String userId) throws SQLException {
        String sql = "SELECT * FROM messages WHERE sender_id = ? OR receiver_id = ?";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, userId);
        pstmt.setString(2, userId);
        return pstmt.executeQuery();
    }

    public ResultSet getUserTweets(String userId) throws SQLException {
        String sql = "SELECT * FROM tweets WHERE user_id = ?";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, userId);
        return pstmt.executeQuery();
    }

    public static void main(String[] args) {
        try (Tw_scenario app = new Tw_scenario();
             Scanner scanner = new Scanner(System.in)) {
            
            System.out.print("E-posta: ");
            String email = scanner.nextLine();
            System.out.print("Şifre: ");
            String password = scanner.nextLine();

            String userId = app.authenticateUser(email, password);
            if (userId != null) {
                System.out.println("Giriş başarılı!");

                // Mesajları ve Tweet'leri getir
                ResultSet messages = app.getUserMessages(userId);
                System.out.println("Mesajlar:");
                while (messages.next()) {
                    System.out.println(messages.getString("message_text"));
                }

                ResultSet tweets = app.getUserTweets(userId);
                System.out.println("Tweetler:");
                while (tweets.next()) {
                    System.out.println(tweets.getString("tweet_text"));
                }
            } else {
                System.out.println("E-posta veya şifre hatalı!");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Bir hata oluştu!");
        }
    }
}
