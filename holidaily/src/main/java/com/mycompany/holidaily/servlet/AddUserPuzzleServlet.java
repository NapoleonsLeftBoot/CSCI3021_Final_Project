package com.mycompany.holidaily.servlet;

import com.mycompany.holidaily.dao.PuzzleDAO;
import com.mycompany.holidaily.model.Puzzle;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet(name = "AddUserPuzzleServlet", urlPatterns = {"/api/tools/addpuzzle"})
public class AddUserPuzzleServlet extends HttpServlet {

    // This tool changes data, so it only accepts POST.
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        try {
            //login check
            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("userId") == null) {
                fail(response, out, HttpServletResponse.SC_UNAUTHORIZED, "Not logged in");
                return;
            }

            //vaalidate input
            String start = clean(request.getParameter("start"));
            String goal = clean(request.getParameter("goal"));
            String theme = request.getParameter("theme");
            
            if (theme == null) {
                theme = null;
            }
            else {
                theme = theme.trim();
            }

            if (!isFiveLetters(start) || !isFiveLetters(goal)) {
                fail(response, out, 400, "start and goal must each be exactly 5 letters");
                return;
            }
            if (start.equals(goal)) {
                fail(response, out, 400, "start and goal must be different words");
                return;
            }
            if (theme == null || !theme.matches("[A-Za-z ]{1,50}")) {
                fail(response, out, 400, "theme is required (letters and spaces only)");
                return;
            }

            //gather elements for adding puzzle
            PuzzleDAO dao = new PuzzleDAO();
            Integer startId = dao.findWordId(start);
            Integer goalId = dao.findWordId(goal);
            // this is gonna be a redundant check
            // but good to have anyways
            if (startId == null || goalId == null) {
                fail(response, out, 422, "Both words must be in the dictionary");
                return;
            }
            Integer themeId = dao.findThemeId(theme);
            if (themeId == null) {
                fail(response, out, 422, "Unknown theme");
                return;
            }
            if (dao.puzzleExists(startId, goalId)) {
                fail(response, out, 409, "This puzzle already exists");
                return;
            }
            int steps = (int) (Math.random() * ((10 - 5) + 1)) + 5; // placeholder until solving exists
            int difficulty = Math.min(100, steps * 10);   // placeholder until calculateScore exists
            Integer userId = parseUserId(session.getAttribute("userId"));
            int newId = dao.insert(new Puzzle(startId, goalId, themeId, userId, difficulty, steps));

            out.print("{\"success\":true,"
                    + "\"puzzleId\":" + newId + "}");

        } catch (SQLException e) {
            fail(response, out, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error");
            log("AddUserPuzzleServlet DB error", e);
        } finally {
            out.close();
        }
    }

    private void fail(HttpServletResponse response, PrintWriter out, int status, String message) {
        response.setStatus(status);
        out.print("{\"success\":false,"
                + "\"error\":\"" + message + "\"}");
    }

    private Integer parseUserId(Object attr) {
        try {
            return Integer.valueOf(String.valueOf(attr));
        } catch (NumberFormatException e) {
            return null;
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
