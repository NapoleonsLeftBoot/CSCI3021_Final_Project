package com.mycompany.holidaily.util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class CacheList {
    
    private static final Set<String> cachedWords = Collections.synchronizedSet(new HashSet<>());
    private static boolean isInitialized = false;

    public static synchronized void initialize() {
        if (isInitialized) {
            return;
        }

        System.out.println("Caching Word Table");
        String sql = "SELECT word FROM words";
        
        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                String word = rs.getString("word");
                if (word != null) {
                    cachedWords.add(word.trim()); 
                }
            }
            
            isInitialized = true;
            System.out.println("Cache created of size: " + cachedWords.size() + " words!");
            
        } catch (SQLException e) {
            System.err.println("Failed to load words cache!");
            e.printStackTrace();
        }
    }

    public static boolean contains(String word) {
        if (word == null) return false;
        return cachedWords.contains(word.trim());
    }
    
    public static void printCache() {
    synchronized (cachedWords) {
        System.out.println("========== CACHED WORDS ==========");
        for (String word : cachedWords) {
            System.out.println(word);
        }
        System.out.println("========== END OF CACHE ==========");
        System.out.println("Total words: " + cachedWords.size());
    }
}
}
