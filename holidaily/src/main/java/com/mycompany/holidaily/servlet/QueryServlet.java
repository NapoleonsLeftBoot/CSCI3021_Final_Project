/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package com.mycompany.holidaily.servlet;

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

@WebServlet(name = "QueryServlet", urlPatterns = {"/QueryServlet"})
public class QueryServlet extends HttpServlet {

    private static final String URL  = "jdbc:mysql://localhost:3306/holidaily";
    private static final String USER = "durn";
    private static final String PASS = "Charmander123564";

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head><title>Puzzles</title></head>");
            out.println("<body>");
            out.println("<h1>Puzzles</h1>");
            out.println("<table border='1'>");
            out.println("<tr><th>ID</th><th>Start</th><th>End</th><th>Theme ID</th>"
                      + "<th>Difficulty</th><th>Ladder Length</th></tr>");

            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
                     Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT * FROM puzzles")) {

                    while (rs.next()) {
                        out.println("<tr>"
                            + "<td>" + rs.getInt("id") + "</td>"
                            + "<td>" + rs.getString("start_word") + "</td>"
                            + "<td>" + rs.getString("end_word") + "</td>"
                            + "<td>" + rs.getInt("theme_id") + "</td>"
                            + "<td>" + rs.getInt("difficulty_score") + "</td>"
                            + "<td>" + rs.getInt("ladder_length") + "</td>"
                            + "</tr>");
                    }
                }
            } catch (ClassNotFoundException | SQLException e) {
                out.println("<tr><td colspan='6'>Database error: " + e.getMessage() + "</td></tr>");
            }

            out.println("</table>");
            out.println("</body>");
            out.println("</html>");
        }
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