package database;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class SQlite {

    private static final String DBDRIVER = "org.sqlite.JDBC";
	//private static final String DB = "C:/tmp/persons.sqlite";
	private static final String DB = "/opt/db/persons.sqlite";

    public static void insertPerson(JSONObject jsonObject) throws ClassNotFoundException, IOException, NoSuchAlgorithmException {
        Class.forName(DBDRIVER);
        try (Connection c = DriverManager.getConnection("jdbc:sqlite:" + DB)) {
            try (Statement stmt = c.createStatement()) {
                String sql = "INSERT INTO persons (id,firstName,lastName,affilliation) VALUES ('" + jsonObject.get("id").toString() + "','" + jsonObject.get("firstName").toString() + "','" + jsonObject.get("lastName").toString() + "','" + jsonObject.get("affilliation").toString() + "')";
                stmt.executeUpdate(sql);
            }
        } catch (Exception e) {
            throw new NullPointerException(e.toString());
        }
	}

    public static JSONObject getPerson(String id) throws SQLException, ClassNotFoundException, IOException {
        JSONObject person = new JSONObject();
        Class.forName(DBDRIVER);
        try (Connection c = DriverManager.getConnection("jdbc:sqlite:" + DB)) {
            try (Statement stmt = c.createStatement()) {
                String sql = "SELECT * FROM persons WHERE id = '" + id + "'";
                try (ResultSet rs = stmt.executeQuery(sql)) {
                    while (rs.next()) {
                        person.put("id", rs.getString("id"));
						person.put("firstName", rs.getString("firstName"));
						person.put("lastName", rs.getString("lastName"));
						person.put("affilliation", rs.getString("affilliation"));
                    }
                }
            }
        } catch (Exception e) {
            throw new NullPointerException("cannot get statistics for vocabulary. " + e.toString());
        }
        return person;
    }
	
	public static JSONArray searchPerson(String query) throws SQLException, ClassNotFoundException, IOException {
        JSONArray persons = new JSONArray();
        Class.forName(DBDRIVER);
        try (Connection c = DriverManager.getConnection("jdbc:sqlite:" + DB)) {
            try (Statement stmt = c.createStatement()) {
                String sql = "SELECT * FROM persons WHERE firstName LIKE '%" + query + "%' OR lastName LIKE '%" + query +"%' OR affilliation LIKE '%" + query +"%'";
                try (ResultSet rs = stmt.executeQuery(sql)) {
                    while (rs.next()) {
                        JSONObject person = new JSONObject();
						person.put("id", rs.getString("id"));
						person.put("firstName", rs.getString("firstName"));
						person.put("lastName", rs.getString("lastName"));
						person.put("affilliation", rs.getString("affilliation"));
						persons.add(person);
                    }
                }
            }
        } catch (Exception e) {
            throw new NullPointerException("cannot get statistics for vocabulary. " + e.toString());
        }
        return persons;
    }

}
