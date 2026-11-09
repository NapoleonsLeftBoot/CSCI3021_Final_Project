//testing file to check database is working
package com.mycompany.holidaily.servlet;

import com.mycompany.holidaily.dao.WordDAO;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@ WebServlet 

        (name = "QueryServlet", urlPatterns = {"/QueryServlet"})
public class QueryServlet extends HttpServlet {

        protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
            response.setContentType("text/html;charset=UTF-8");
            PrintWriter out = response.getWriter();
            out.println("<!DOCTYPE html>");
            out.println("<html><body>");
            out.println("<h1>WordDAO test</h1>");
            try {
                WordDAO dao = new WordDAO();
                out.println("<p>GHOST valid: " + dao.isValidWord("GHOST") + "</p>");
                out.println("<p>ZZZZZ valid: " + dao.isValidWord("ZZZZZ") + "</p>");
                out.println("<p>XXXXX blocked: " + dao.isBlockedWord("XXXXX") + "</p>");
            } catch (SQLException e) {
                out.println("<p>Database error: " + e.getMessage() + "</p>");
            }
            out.println("</body></html>");
        }
  

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    public String getServletInfo() {
        return "Queries the puzzles table";
    }
}
