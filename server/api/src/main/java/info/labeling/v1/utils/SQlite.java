package info.labeling.v1.utils;

import de.i3mainz.ls.rdfutils.exceptions.AccessDeniedException;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.UUID;

public class SQlite {

	private static final String DBDRIVER = "org.sqlite.JDBC";

	public static String getUserInfoAndCheckPassword(String user, String password) throws SQLException, ClassNotFoundException, AccessDeniedException, IOException {
		String ret = null;
		String db_password = "";
		String activation_token = "";
		String role = "";
		Class.forName(DBDRIVER);
		URL url = SQlite.class.getClassLoader().getResource(PropertiesLocal.getPropertyParam("sqlite"));
		try (Connection c = DriverManager.getConnection("jdbc:sqlite:"+url)) {
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
			String echo = salt + Funcs.SHA1(salt + password);
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

	public static boolean insertUser(String user_name, String pwd, String activationToken, String email, String role) throws ClassNotFoundException, IOException {
		boolean ret = false;
		Class.forName(DBDRIVER);
		URL url = SQlite.class.getClassLoader().getResource(PropertiesLocal.getPropertyParam("sqlite"));
		try (Connection c = DriverManager.getConnection("jdbc:sqlite:"+url)) {
			try (Statement stmt = c.createStatement()) {
				String sql = "INSERT INTO users (user_name,pwd,activation_token,role) VALUES ('" + user_name + "','" + pwd + "','" + activationToken + "'," + role + "')";
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
		URL url = SQlite.class.getClassLoader().getResource(PropertiesLocal.getPropertyParam("sqlite"));
		try (Connection c = DriverManager.getConnection("jdbc:sqlite:"+url)) {
			try (Statement stmt = c.createStatement()) {
				String sql = "UPDATE users SET activation_token = '"+activationToken+"' WHERE user_name = '" + user_name + "';";
				stmt.executeUpdate(sql);
			}
			ret = true;
		} catch (Exception e) {
			throw new NullPointerException(e.toString());
		} finally {
			return ret;
		}
	}

}
