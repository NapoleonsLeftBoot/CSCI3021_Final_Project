
package com.mycompany.holidaily.servlet;

import com.mycompany.holidaily.dao.WordDAO;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet(name = "ProfanityCheckServlet", urlPatterns = {"/api/tools/profanity"})
public class ProfanityCheckServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        handle(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        handle(request, response);
    }

    private void handle(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        try {
            //login check
            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("userId") == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                out.print("{\"success\":false,"
                        + "\"error\":\"Not logged in\"}");
                return;
            }

            //validate input
            String start = clean(request.getParameter("start"));
            String goal  = clean(request.getParameter("goal"));
            if (!isFiveLetters(start) || !isFiveLetters(goal)) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"success\":false,"
                        + "\"error\":\"start and end must each be exactly 5 letters\"}");
                return;
            }

            //check if either word is in blocked words table
            WordDAO dao = new WordDAO();
            boolean clean = !dao.isBlockedWord(start) && !dao.isBlockedWord(goal);
            out.print("{\"success\":true,"
                    + "\"result\":" + clean + "}");

        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"success\":false,"
                    + "\"error\":\"Database error\"}");
            log("ProfanityCheckServlet DB error", e);
        } finally {
            out.close();
        }
    }

    private String clean(String s) {
        if (s == null) {
            return null;
        }
        else {
            return s.trim().toUpperCase();
        }
    }

    private boolean isFiveLetters(String s) {
        if (s != null) {
            if (s.matches("[A-Z]{5}")) {
                return true;
            } 
            else {
                return false;
            }
        } 
        else {
            return false;
        }
    }
}
