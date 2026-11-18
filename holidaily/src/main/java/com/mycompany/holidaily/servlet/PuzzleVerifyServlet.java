/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package com.mycompany.holidaily.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import com.mycompany.holidaily.util.CacheList;
import java.util.HashSet;


@WebServlet(name = "PuzzleVerifyServlet", urlPatterns = {"/api/tools/PuzzleVerify"})
public class PuzzleVerifyServlet extends HttpServlet {
    
    
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

            //validates input
            String start = clean(request.getParameter("start"));
            String goal  = clean(request.getParameter("goal"));
            if (!isFiveLetters(start) || !isFiveLetters(goal)) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"success\":false,"
                        + "\"error\":\"start and end must each be exactly 5 letters\"}");
                return;
            }

            //check if ladder is possible
            boolean ladder = isWordLadder(start, goal);
            out.print("{\"success\":true,"
                    + "\"result\":" + ladder + "}");

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"success\":false,"
                    + "\"error\":\" error\"}");
            log("PuzzleVerifyServlet error", e);
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
    
    private boolean isWordLadder (String start, String end){
        
        if (start.equals(end)) {
            return true;
        }
        
        Set<String> front = new HashSet<>();
        Set<String> back = new HashSet<>();
        Set<String> visited = new HashSet<>();
        
        front.add(start);
        back.add(end);
        visited.add(start);
        visited.add(end);
        
        while(!front.isEmpty() && !back.isEmpty()){
            if(front.size() > back.size()){
                Set<String> temp = front;
                front = back;
                back = temp;
            }
            
            Set<String> nextFront = new HashSet<>();
            
            for(String word: front){
                char[] chars = word.toCharArray();
                
                for(int i = 0; i < 5; i++){
                    char savedChar = chars[i];
                    for(char j = 'A'; j <= 'Z'; j++){
                        if( j == savedChar){
                            continue;
                        }
                        
                        chars[i] = j;
                        String candidate = new String(chars);
                        
                        if(back.contains(candidate)){
                            return true;
                        }
                        
                        if (CacheList.contains(candidate)&& !visited.contains(candidate)){
                            visited.add(candidate);
                            nextFront.add(candidate);
                        }
                    }
                chars[i] = savedChar;
                }
            }
            
            front = nextFront;
        }
        return false;
    }
   

}
