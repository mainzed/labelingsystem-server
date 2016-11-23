package database;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class newperson extends HttpServlet {

	protected void processRequest(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException, SQLException, ClassNotFoundException, ParseException, NoSuchAlgorithmException {
		response.setContentType("application/json;charset=UTF-8");
		response.setHeader("Access-Control-Allow-Origin", "*");
		response.setCharacterEncoding("UTF-8");
		try (PrintWriter out = response.getWriter()) {
			StringBuilder buffer = new StringBuilder();
			BufferedReader reader = request.getReader();
			String line;
			while ((line = reader.readLine()) != null) {
				buffer.append(line);
			}
			JSONObject jsonObject = (JSONObject) new JSONParser().parse(buffer.toString());
			SQlite.insertPerson(jsonObject);
		} catch (Exception e) {
			response.sendError(500, e.toString());
		}
	}

	// <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
	/**
	 * Handles the HTTP <code>GET</code> method.
	 *
	 * @param request servlet request
	 * @param response servlet response
	 * @throws ServletException if a servlet-specific error occurs
	 * @throws IOException if an I/O error occurs
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		/*try {
			processRequest(request, response);
		} catch (SQLException ex) {
			Logger.getLogger(search.class.getName()).log(Level.SEVERE, null, ex);
		} catch (ClassNotFoundException ex) {
			Logger.getLogger(search.class.getName()).log(Level.SEVERE, null, ex);
		}*/
	}

	/**
	 * Handles the HTTP <code>POST</code> method.
	 *
	 * @param request servlet request
	 * @param response servlet response
	 * @throws ServletException if a servlet-specific error occurs
	 * @throws IOException if an I/O error occurs
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		try {
			processRequest(request, response);
		} catch (SQLException ex) {
			Logger.getLogger(search.class.getName()).log(Level.SEVERE, null, ex);
		} catch (ClassNotFoundException ex) {
			Logger.getLogger(search.class.getName()).log(Level.SEVERE, null, ex);
		} catch (ParseException ex) {
			Logger.getLogger(newperson.class.getName()).log(Level.SEVERE, null, ex);
		} catch (NoSuchAlgorithmException ex) {
			Logger.getLogger(newperson.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	/**
	 * Returns a short description of the servlet.
	 *
	 * @return a String containing servlet description
	 */
	@Override
	public String getServletInfo() {
		return "Short description";
	}// </editor-fold>

}
