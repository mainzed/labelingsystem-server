package info.labeling.v1.utils;

import info.labeling.exceptions.AccessDeniedException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.UUID;

public class SQlite {

	private static final String DBDRIVER = "org.sqlite.JDBC";

	/**
	 * ******
	 * USERS * *******
	 */
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

	/**
	 * ********
	 * RETCATS * ********
	 */
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
	
	// LOGIN //
	
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
	
	public static String[] getLoginStatus(String user) throws SQLException, ClassNotFoundException, AccessDeniedException, IOException {
		String[] ret = new String[2];
		Class.forName(DBDRIVER);
		try (Connection c = DriverManager.getConnection("jdbc:sqlite:" + ConfigProperties.getPropertyParam("sqlite"))) {
			try (Statement stmt = c.createStatement()) {
				String sql = "SELECT * FROM login WHERE user_name = '" + user + "'";
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
				String sql = "DELETE FROM login WHERE user_name = '" + user + "'";
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
