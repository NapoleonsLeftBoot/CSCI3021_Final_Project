package com.mycompany.holidaily.dao;

import com.mycompany.holidaily.util.DBUtil;
import static com.mycompany.holidaily.util.DBUtil.getConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class WordDAO {

    private static final String DICTIONARY_SQL
            = "SELECT COUNT(*) FROM words WHERE word = ?";
    private static final String BLOCKED_SQL
            = "SELECT COUNT(*) FROM blocked_words WHERE word = ?";

    // True if the word is in the word table
    public boolean isValidWord(String word) throws SQLException {
        return exists(DICTIONARY_SQL, word);
    }

    // True if the word is in the blocked words table
    public boolean isBlockedWord(String word) throws SQLException {
        return exists(BLOCKED_SQL, word);
    }

    // True if word exists in table
    // so there has to be more than 0 of it in the table
    private boolean exists(String sql, String word) throws SQLException {
        if (word == null) {
            return false;
        }
        try (Connection conn = DBUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, word.trim().toUpperCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    if (rs.getInt(1) > 0) {
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }
            }
        }
    }
    
    
    public List<String> getAllWords() throws SQLException {
    List<String> words = new ArrayList<>();

    String sql = "SELECT word FROM words";

    try (Connection conn = getConnection();
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(sql)) {

        while (rs.next()) {
            words.add(rs.getString("word"));
        }
    }

    return words;
}
}
