package v1.utils.db;

import exceptions.AccessDeniedException;
import exceptions.SQliteException;
import v1.utils.config.ConfigProperties;
import v1.utils.crypt.Crypt;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.UUID;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class SQlite {

    private static final String DBDRIVER = "org.sqlite.JDBC";

    // USERS
    public static String getUserInfoAndCheckPassword(String user, String password) throws SQLException, ClassNotFoundException, AccessDeniedException, IOException {
        String ret = null;
        String db_password = "";
        String activation_token = "";
        String role = "";
        Class.forName(DBDRIVER);
        try (Connection c = DriverManager.getConnection("jdbc:sqlite:" + ConfigProperties.getPropertyParam("sqlite"))) {
            try (Statement stmt = c.createStatement()) {
                String sql = "SELECT * FROM users WHERE user_name = '" + user + "'";
                try (ResultSet rs = stmt.executeQuery(sql)) {
                    while (rs.next()) {
                        db_password = rs.getString("pwd");
                        activation_token = rs.getString("activation_token");
                        role = rs.getString("role");
                    }
                }
            }
            // check password
            String salt = db_password.substring(0, 25);
            String echo = salt + Crypt.SHA1(salt + password);
            if (echo.equals(db_password) && activation_token.equals("-1")) {
                ret = role;
            } else {
                throw new AccessDeniedException();
            }
        } catch (Exception e) {
            throw new AccessDeniedException(e.toString());
        }
        return ret;
    }

    public static boolean insertUser(String user, String password) throws ClassNotFoundException, IOException, NoSuchAlgorithmException {
        // hash  password
        String salt = Crypt.generateHash();
        password = salt + Crypt.SHA1(salt + password);
        boolean ret = false;
        Class.forName(DBDRIVER);
        try (Connection c = DriverManager.getConnection("jdbc:sqlite:" + ConfigProperties.getPropertyParam("sqlite"))) {
            try (Statement stmt = c.createStatement()) {
                String sql = "INSERT INTO users (user_name,pwd,activation_token,role) VALUES ('" + user + "','" + password + "','" + "-1" + "','" + "user" + "')";
                stmt.executeUpdate(sql);
            }
            ret = true;
        } catch (Exception e) {
            throw new NullPointerException(e.toString());
        } finally {
            return ret;
        }
    }

    public static boolean deactivateUser(String user_name) throws ClassNotFoundException, IOException {
        boolean ret = false;
        String activationToken = UUID.randomUUID().toString();
        Class.forName(DBDRIVER);
        try (Connection c = DriverManager.getConnection("jdbc:sqlite:" + ConfigProperties.getPropertyParam("sqlite"))) {
            try (Statement stmt = c.createStatement()) {
                String sql = "UPDATE users SET activation_token = '" + activationToken + "' WHERE user_name = '" + user_name + "';";
                stmt.executeUpdate(sql);
            }
            ret = true;
        } catch (Exception e) {
            throw new NullPointerException(e.toString());
        } finally {
            return ret;
        }
    }

    public static boolean activateUser(String user_name) throws ClassNotFoundException, IOException {
        boolean ret = false;
        String activationToken = "-1";
        Class.forName(DBDRIVER);
        try (Connection c = DriverManager.getConnection("jdbc:sqlite:" + ConfigProperties.getPropertyParam("sqlite"))) {
            try (Statement stmt = c.createStatement()) {
                String sql = "UPDATE users SET activation_token = '" + activationToken + "' WHERE user_name = '" + user_name + "';";
                stmt.executeUpdate(sql);
            }
            ret = true;
        } catch (Exception e) {
            throw new NullPointerException(e.toString());
        } finally {
            return ret;
        }
    }

    public static JSONArray getUsersInfo() throws SQLException, ClassNotFoundException, AccessDeniedException, IOException {
        JSONArray users = new JSONArray();
        Class.forName(DBDRIVER);
        try (Connection c = DriverManager.getConnection("jdbc:sqlite:" + ConfigProperties.getPropertyParam("sqlite"))) {
            try (Statement stmt = c.createStatement()) {
                String sql = "SELECT * FROM users";
                try (ResultSet rs = stmt.executeQuery(sql)) {
                    while (rs.next()) {
                        JSONObject userObject = new JSONObject();
                        userObject.put("username", rs.getString("user_name"));
                        userObject.put("role", rs.getString("role"));
                        if (rs.getString("activation_token").equals("-1")) {
                            userObject.put("status", "active");
                        } else {
                            userObject.put("status", "inactive");
                        }
                        users.add(userObject);
                    }
                }
            }
            return users;
        } catch (Exception e) {
            throw new AccessDeniedException(e.toString());
        }
    }

    public static JSONObject getUserInfo(String user_name) throws SQLException, ClassNotFoundException, AccessDeniedException, IOException {
        JSONObject userObject = new JSONObject();
        Class.forName(DBDRIVER);
        try (Connection c = DriverManager.getConnection("jdbc:sqlite:" + ConfigProperties.getPropertyParam("sqlite"))) {
            try (Statement stmt = c.createStatement()) {
                String sql = "SELECT * FROM users WHERE user_name = '" + user_name + "';";
                try (ResultSet rs = stmt.executeQuery(sql)) {
                    while (rs.next()) {
                        userObject.put("username", rs.getString("user_name"));
                        userObject.put("role", rs.getString("role"));
                        if (rs.getString("activation_token").equals("-1")) {
                            userObject.put("status", "active");
                        } else {
                            userObject.put("status", "inactive");
                        }
                    }
                }
            }
            return userObject;
        } catch (Exception e) {
            throw new AccessDeniedException(e.toString());
        }
    }

    // LOGIN
    public static boolean setLogin(String user, String role) throws ClassNotFoundException, IOException, NoSuchAlgorithmException {
        boolean ret = false;
        Calendar cal = Calendar.getInstance();
        java.util.Date time = cal.getTime();
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        String d = formatter.format(time);
        Class.forName(DBDRIVER);
        try (Connection c = DriverManager.getConnection("jdbc:sqlite:" + ConfigProperties.getPropertyParam("sqlite"))) {
            try (Statement stmt = c.createStatement()) {
                String sql = "INSERT INTO login (user_name,role,date) VALUES ('" + user + "','" + role + "','" + d + "')";
                stmt.executeUpdate(sql);
            }
            ret = true;
        } catch (Exception e) {
            System.out.println(e.toString());
            throw new NullPointerException(e.toString());
        } finally {
            return ret;
        }
    }

    public static String[] getLoginStatus(String userAndToken) throws SQLException, ClassNotFoundException, AccessDeniedException, IOException {
        String[] ret = new String[2];
        Class.forName(DBDRIVER);
        try (Connection c = DriverManager.getConnection("jdbc:sqlite:" + ConfigProperties.getPropertyParam("sqlite"))) {
            try (Statement stmt = c.createStatement()) {
                String sql = "SELECT * FROM login WHERE user_name = '" + userAndToken + "'";
                try (ResultSet rs = stmt.executeQuery(sql)) {
                    while (rs.next()) {
                        ret[0] = rs.getString("role");
                        ret[1] = rs.getString("date");
                    }
                }
            }
        } catch (Exception e) {
            throw new NullPointerException(e.toString());
        }
        return ret;
    }

    public static boolean setLogout(String user) throws ClassNotFoundException, IOException {
        boolean ret = false;
        Class.forName(DBDRIVER);
        try (Connection c = DriverManager.getConnection("jdbc:sqlite:" + ConfigProperties.getPropertyParam("sqlite"))) {
            try (Statement stmt = c.createStatement()) {
                String sql = "DELETE FROM login WHERE user_name LIKE '" + user + ";%'";
                stmt.executeUpdate(sql);
            }
            ret = true;
        } catch (Exception e) {
            throw new NullPointerException(e.toString());
        } finally {
            return ret;
        }
    }

    // RETCAT
    public static boolean insertRetcatString(String vocabulary, String retcatString) throws ClassNotFoundException, IOException {
        boolean ret = false;
        Class.forName(DBDRIVER);
        try (Connection c = DriverManager.getConnection("jdbc:sqlite:" + ConfigProperties.getPropertyParam("sqlite"))) {
            try (Statement stmt = c.createStatement()) {
                String sql = "INSERT INTO retcat (vocabulary,retcat) VALUES ('" + vocabulary + "','" + retcatString + "')";
                stmt.executeUpdate(sql);
            }
            ret = true;
        } catch (Exception e) {
            throw new NullPointerException(e.toString());
        } finally {
            return ret;
        }
    }

    public static boolean deleteRetcatEntry(String vocabulary) throws ClassNotFoundException, IOException {
        boolean ret = false;
        Class.forName(DBDRIVER);
        try (Connection c = DriverManager.getConnection("jdbc:sqlite:" + ConfigProperties.getPropertyParam("sqlite"))) {
            try (Statement stmt = c.createStatement()) {
                String sql = "DELETE FROM retcat WHERE vocabulary = '" + vocabulary + "'";
                stmt.executeUpdate(sql);
            }
            ret = true;
        } catch (Exception e) {
            throw new NullPointerException(e.toString());
        } finally {
            return ret;
        }
    }

    public static String getRetcatByVocabulary(String vocabulary) throws SQLException, ClassNotFoundException, AccessDeniedException, IOException {
        String ret = null;
        Class.forName(DBDRIVER);
        try (Connection c = DriverManager.getConnection("jdbc:sqlite:" + ConfigProperties.getPropertyParam("sqlite"))) {
            try (Statement stmt = c.createStatement()) {
                String sql = "SELECT retcat FROM retcat WHERE vocabulary = '" + vocabulary + "'";
                try (ResultSet rs = stmt.executeQuery(sql)) {
                    while (rs.next()) {
                        ret = rs.getString("retcat");
                    }
                }
            }
        } catch (Exception e) {
            throw new NullPointerException(e.toString());
        }
        return ret;
    }

    // RETCAT LIST
    public static boolean insertRetcatStringForList(String vocabulary, String vocabID) throws ClassNotFoundException, IOException {
        boolean ret = false;
        Class.forName(DBDRIVER);
        try (Connection c = DriverManager.getConnection("jdbc:sqlite:" + ConfigProperties.getPropertyParam("sqlite"))) {
            try (Statement stmt = c.createStatement()) {
                String sql = "INSERT INTO retcatlist (vocabulary,list) VALUES ('" + vocabulary + "','" + vocabID + "')";
                stmt.executeUpdate(sql);
            }
            ret = true;
        } catch (Exception e) {
            throw new NullPointerException(e.toString());
        } finally {
            return ret;
        }
    }

    public static boolean deleteRetcatEntryForList(String vocabulary) throws ClassNotFoundException, IOException {
        boolean ret = false;
        Class.forName(DBDRIVER);
        try (Connection c = DriverManager.getConnection("jdbc:sqlite:" + ConfigProperties.getPropertyParam("sqlite"))) {
            try (Statement stmt = c.createStatement()) {
                String sql = "DELETE FROM retcatlist WHERE vocabulary = '" + vocabulary + "'";
                stmt.executeUpdate(sql);
            }
            ret = true;
        } catch (Exception e) {
            throw new NullPointerException(e.toString());
        } finally {
            return ret;
        }
    }

    public static String getRetcatByVocabularyForList(String vocabulary) throws SQLException, ClassNotFoundException, AccessDeniedException, IOException {
        String ret = null;
        Class.forName(DBDRIVER);
        try (Connection c = DriverManager.getConnection("jdbc:sqlite:" + ConfigProperties.getPropertyParam("sqlite"))) {
            try (Statement stmt = c.createStatement()) {
                String sql = "SELECT list FROM retcatlist WHERE vocabulary = '" + vocabulary + "'";
                try (ResultSet rs = stmt.executeQuery(sql)) {
                    while (rs.next()) {
                        ret = rs.getString("list");
                    }
                }
            }
        } catch (Exception e) {
            throw new NullPointerException(e.toString());
        }
        return ret;
    }

    public static void insertStatisticsForVocabuary(String vocabID, String lastModifyAction, int wayback, int translations, int descriptions, int linksexternal, int linksinternal, int linkscount, int labelscount) throws ClassNotFoundException, IOException, SQliteException {
        Class.forName(DBDRIVER);
        try (Connection c = DriverManager.getConnection("jdbc:sqlite:" + ConfigProperties.getPropertyParam("sqlite"))) {
            try (Statement stmt = c.createStatement()) {
                String sql = "INSERT INTO statistics "
                        + "(vocabulary,lastModifyAction,wayback,translations,descriptions,linksexternal,linksinternal,linkscount,labelscount) "
                        + "VALUES ('" + vocabID + "','" + lastModifyAction + "'," + wayback + "," + translations + "," + descriptions + "," + linksexternal + "," + linksinternal + "," + linkscount + "," + labelscount + ")";
                stmt.executeUpdate(sql);
            }
        } catch (Exception e) {
            throw new SQliteException("cannot insert statistics for vocabulary. " + e.toString());
        }
    }

    public static void deleteStatisticsForVocabuary(String vocabID) throws ClassNotFoundException, IOException, SQliteException {
        Class.forName(DBDRIVER);
        try (Connection c = DriverManager.getConnection("jdbc:sqlite:" + ConfigProperties.getPropertyParam("sqlite"))) {
            try (Statement stmt = c.createStatement()) {
                String sql = "DELETE FROM statistics WHERE vocabulary = '" + vocabID + "'";
                stmt.executeUpdate(sql);
            }
        } catch (Exception e) {
            throw new SQliteException("cannot delete statistics for vocabulary. " + e.toString());
        }
    }

    public static JSONObject getStatisticsForVocabuary(String vocabulary) throws SQLException, ClassNotFoundException, AccessDeniedException, IOException, SQliteException {
        JSONObject statistics = new JSONObject();
        JSONObject links = new JSONObject();
        JSONObject labels = new JSONObject();
        JSONObject descriptive = new JSONObject();
        Class.forName(DBDRIVER);
        try (Connection c = DriverManager.getConnection("jdbc:sqlite:" + ConfigProperties.getPropertyParam("sqlite"))) {
            try (Statement stmt = c.createStatement()) {
                String sql = "SELECT * FROM statistics WHERE vocabulary = '" + vocabulary + "'";
                try (ResultSet rs = stmt.executeQuery(sql)) {
                    while (rs.next()) {
                        links.put("external", Integer.parseInt(rs.getString("linksexternal")));
                        links.put("internal", Integer.parseInt(rs.getString("linksinternal")));
                        links.put("count", Integer.parseInt(rs.getString("linkscount")));
                        statistics.put("links", links);
                        labels.put("count", Integer.parseInt(rs.getString("labelscount")));
                        statistics.put("labels", labels);
                        descriptive.put("wayback", Integer.parseInt(rs.getString("wayback")));
                        descriptive.put("descriptions", Integer.parseInt(rs.getString("descriptions")));
                        descriptive.put("translations", Integer.parseInt(rs.getString("translations")));
                        statistics.put("descriptive", descriptive);
                        statistics.put("lastModifyAction", rs.getString("lastModifyAction"));
                    }
                }
            }
        } catch (Exception e) {
            throw new SQliteException("cannot get statistics for vocabulary. " + e.toString());
        }
        return statistics;
    }

}
