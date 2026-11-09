package com.mycompany.holidaily.servlet;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//right now just just
//uses get to get user info for temp login
@WebServlet(name = "LoginServlet", urlPatterns = {"/api/login"})
public class LoginServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String user = request.getParameter("user");
        response.setContentType("application/json;charset=UTF-8");
        if (user == null || !user.matches("[A-Za-z0-9_]{1,50}")) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().print("{\"success\":false,"
                    + "\"error\":\"Invalid user\"}");
            return;
        }
        request.getSession(true).setAttribute("userId", user);
        response.getWriter().print("{\"success\":true}");
    }
}
