package com.mycompany.holidaily.dao;

import com.mycompany.holidaily.model.Puzzle;
import com.mycompany.holidaily.util.DBUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

public class PuzzleDAO {

    public Integer findWordId(String word) throws SQLException {
        return findId("SELECT id FROM words WHERE word = ?", word);
    }

    public Integer findThemeId(String themeName) throws SQLException {
        return findId("SELECT id FROM themes WHERE name = ?", themeName);
    }
    
    //the setInt functions just assigns each ? to a specific value
    public boolean puzzleExists(int wordIdA, int wordIdB) throws SQLException {
        String sql = "SELECT COUNT(*) FROM puzzles "
                + "WHERE (start_word_id = ? AND end_word_id = ?) "
                + "OR (start_word_id = ? AND end_word_id = ?)";
        try (Connection conn = DBUtil.getConnection(); 
            PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, wordIdA);
            ps.setInt(2, wordIdB);
            ps.setInt(3, wordIdB);
            ps.setInt(4, wordIdA);
            try (ResultSet rs = ps.executeQuery()) {
                //puzzle exists when the returned count is more than 0
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }
    
    public int insert(Puzzle p) throws SQLException {
        String sql = "INSERT INTO puzzles "
                + "(start_word_id, end_word_id, theme_id, user_id, difficulty_score, ladder_length) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection(); 
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, p.getStartWordId());
            ps.setInt(2, p.getEndWordId());
            ps.setInt(3, p.getThemeId());
            if (p.getUserId() == null) {
                ps.setNull(4, Types.INTEGER);
            } else {
                ps.setInt(4, p.getUserId());
            }
            ps.setInt(5, p.getDifficultyScore());
            ps.setInt(6, p.getLadderLength());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
                throw new SQLException("Insert succeeded but no id was returned");
            }
        }
    }

    private Integer findId(String sql, String value) throws SQLException {
        try (Connection conn = DBUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, value);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                } else {
                    return null;
                }
            }
        }
    }
}
