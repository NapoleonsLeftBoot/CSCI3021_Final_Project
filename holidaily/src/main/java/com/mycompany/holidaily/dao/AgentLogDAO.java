package com.mycompany.holidaily.dao;

import com.mycompany.holidaily.util.DBUtil;
import java.sql.*;

public class AgentLogDAO {

    public void logToolCall(
            int userId,
            String toolName,
            String input,
            String output) throws SQLException {

        String sql = "INSERT INTO agent_log "
                + "(user_id, tool_name, input, output) "
                + "VALUES (?, ?, ?, ?)";

        try (Connection conn = DBUtil.getConnection()) {


            try (PreparedStatement ps =
                    conn.prepareStatement(sql)) {


                ps.setInt(1, userId);
                ps.setString(2, toolName);
                ps.setString(3, input);
                ps.setString(4, output);

                ps.executeUpdate();
            }
        }
    }

    private Integer findUserId(
            Connection conn, String username)
            throws SQLException {

        if (username == null) {
            return null;
        }

        String sql = "SELECT id FROM users "
                + "WHERE username = ?";

        try (PreparedStatement ps =
                conn.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        }

        return null;
    }
}
