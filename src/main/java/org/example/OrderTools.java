package org.example;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class OrderTools {

    private final JdbcTemplate jdbc;

    public OrderTools(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Tool(description = """
            Run a SQL SELECT query on the orders database.
            Only SELECT queries allowed.
            Table: orders(id, customer_name, city, amount, status, created_at)
            Returns results as a list of rows.
            """)
    public String runQuery(String sql) {
        if (!sql.trim().toUpperCase().startsWith("SELECT")) {
            return "ERROR: Only SELECT queries are allowed.";
        }

        try {
            List<Map<String, Object>> results = jdbc.queryForList(sql);
            if (results.isEmpty()) {
                return "No results found.";
            }

            StringBuilder sb = new StringBuilder();
            sb.append("Results (" + results.size() + " rows):\n");
            for (Map<String, Object> row : results) {
                sb.append(row.toString()).append("\n");
            }
            return sb.toString();

        } catch (Exception e) {
            return "Query error: " + e.getMessage();
        }
    }
}
