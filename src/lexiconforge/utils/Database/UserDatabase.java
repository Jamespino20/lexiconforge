package lexiconforge.utils.Database;

import java.sql.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.io.File;

public class UserDatabase {

    // We'll build DB_URL dynamically from a helper method
    private static final String DB_URL = "jdbc:sqlite:" + getDbPathInAppData();

    /**
     * Builds or retrieves the path:
     * %USERPROFILE%\AppData\Roaming\LexiconForge\lexiconforge.db
     */
    private static String getDbPathInAppData() {
        // 1) Grab the user’s home directory
        String userHome = System.getProperty("user.home");
        // On Windows, typically C:\Users\Username

        // 2) Construct the path to AppData\Roaming\LexiconForge
        File appDataDir = new File(userHome, "AppData\\Roaming\\LexiconForge");
        if (!appDataDir.exists()) {
            appDataDir.mkdirs();  // create subdirs if needed
        }

        // 3) Our DB file is "lexiconforge.db" in that directory
        File dbFile = new File(appDataDir, "lexiconforge.db");
        return dbFile.getAbsolutePath();
    }

    public static void initializeDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL); Statement stmt = conn.createStatement()) {

            // Print the path to help debugging
            System.out.println("Database Path: " + getDbPathInAppData());

            String createTableSQL = "CREATE TABLE IF NOT EXISTS users ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "username TEXT UNIQUE NOT NULL, "
                    + "password TEXT NOT NULL, "
                    + "languages TEXT DEFAULT '[]', "
                    + "question1 TEXT, answer1 TEXT, "
                    + "question2 TEXT, answer2 TEXT, "
                    + "question3 TEXT, answer3 TEXT"
                    + ")";
            stmt.executeUpdate(createTableSQL);

            System.out.println("Database initialized successfully (per-user in AppData).");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Registers a user with verification Q&A (8-argument version).
     */
    public static boolean registerUser(String username, String password,
            String q1, String a1,
            String q2, String a2,
            String q3, String a3) {
        if (usernameExists(username)) {
            System.out.println("Username already exists!");
            return false;
        }
        String hashedPassword = hashPassword(password);

        String insertSQL = "INSERT INTO users "
                + "(username, password, languages, question1, answer1, "
                + " question2, answer2, question3, answer3) "
                + "VALUES (?, ?, '[]', ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL); PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {

            pstmt.setString(1, username);
            pstmt.setString(2, hashedPassword);
            pstmt.setString(3, q1);
            pstmt.setString(4, a1);
            pstmt.setString(5, q2);
            pstmt.setString(6, a2);
            pstmt.setString(7, q3);
            pstmt.setString(8, a3);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("User registered successfully: " + username);
                return true;
            } else {
                System.out.println("Failed to register user.");
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Overloaded simpler version if you only pass username/password (for older
     * code or tests).
     */
    public static boolean registerUser(String username, String password) {
        // If you do not handle Q&A here, it won't store them. 
        // We'll keep it for backward compatibility or for test usage.
        return registerUser(username, password, "", "", "", "", "", "");
    }

    /**
     * Checks if a username already exists in the DB.
     */
    private static boolean usernameExists(String username) {
        String query = "SELECT COUNT(*) FROM users WHERE username = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL); PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            return rs.getInt(1) > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Validates a user by comparing hashed password in DB.
     */
    public static boolean validateUser(String username, String password) {
        String query = "SELECT password FROM users WHERE username = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL); PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String storedHashedPassword = rs.getString("password");
                return storedHashedPassword.equals(hashPassword(password));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Resets the password if Q&A match what's in the DB for that user.
     */
    public static boolean resetPassword(String username,
            String q1, String a1,
            String q2, String a2,
            String q3, String a3,
            String newPassword) {
        String query = "SELECT question1, answer1, question2, answer2, "
                + "question3, answer3 FROM users WHERE username = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL); PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String sq1 = rs.getString("question1");
                String sa1 = rs.getString("answer1");
                String sq2 = rs.getString("question2");
                String sa2 = rs.getString("answer2");
                String sq3 = rs.getString("question3");
                String sa3 = rs.getString("answer3");

                // Compare ignoring case for answers:
                boolean match = (sq1 != null && sq1.equals(q1)
                        && sa1 != null && sa1.equalsIgnoreCase(a1))
                        && (sq2 != null && sq2.equals(q2)
                        && sa2 != null && sa2.equalsIgnoreCase(a2))
                        && (sq3 != null && sq3.equals(q3)
                        && sa3 != null && sa3.equalsIgnoreCase(a3));

                if (!match) {
                    return false; // verification failed
                }

                // If matched, update password
                String updateSQL = "UPDATE users SET password = ? WHERE username = ?";
                try (PreparedStatement upstmt = conn.prepareStatement(updateSQL)) {
                    upstmt.setString(1, hashPassword(newPassword));
                    upstmt.setString(2, username);
                    upstmt.executeUpdate();
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Returns the 'languages' field from DB if you store it, or "[]" if not
     * found.
     */
    public static String getUserLanguages(String username) {
        String query = "SELECT languages FROM users WHERE username = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL); PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("languages");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "[]";
    }

    /**
     * Utility to hash passwords with SHA-256.
     */
    private static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Deletes the user row from the DB (Account Ribbon -> "Delete account").
     */
    public static void deleteUser(String username) {
        String deleteSQL = "DELETE FROM users WHERE username = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL); PreparedStatement pstmt = conn.prepareStatement(deleteSQL)) {

            pstmt.setString(1, username);
            pstmt.executeUpdate();
            System.out.println("User " + username + " deleted from DB.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Also remove user’s folder in Documents/LexiconForge
     * Files/Users/[username].
     */
    public static void deleteUserFolder(String username) {
        File userDir = new File(System.getProperty("user.home")
                + "/Documents/LexiconForge Files/Users/" + username);
        if (userDir.exists() && userDir.isDirectory()) {
            deleteDirectoryRecursively(userDir);
            System.out.println("User folder deleted: " + userDir.getAbsolutePath());
        }
    }

    private static void deleteDirectoryRecursively(File dir) {
        File[] contents = dir.listFiles();
        if (contents != null) {
            for (File f : contents) {
                if (f.isDirectory()) {
                    deleteDirectoryRecursively(f);
                } else {
                    f.delete();
                }
            }
        }
        dir.delete();
    }
}
