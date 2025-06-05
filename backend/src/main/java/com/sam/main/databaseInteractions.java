package com.sam.main;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

public class databaseInteractions {

    // Update these with your actual PostgreSQL connection details
    private static final String DB_URL      = "jdbc:postgresql://localhost:5432/your_database";
    private static final String DB_USER     = "your_username";
    private static final String DB_PASSWORD = "your_password";

    /**
     * Inserts the given sentiment object into the Stocks table.
     * Assumes schema:
     *
     * CREATE TABLE IF NOT EXISTS Stocks (
     *   StockSymbol         text      NOT NULL,
     *   CompanyName         text      NOT NULL,
     *   Sentiment           decimal   NOT NULL,
     *   SentimentTimestamp  timestamp NOT NULL,
     *   URLS                JSONB     NOT NULL,
     *   LLMAnalysis         text,
     *   PRIMARY KEY (StockSymbol, SentimentTimestamp)
     * );
     */
    public void addSentiment(sentiment s) {
        String sql =
            "INSERT INTO Stocks (" +
            "  StockSymbol,         " +
            "  CompanyName,         " +
            "  Sentiment,           " +
            "  SentimentTimestamp,  " +
            "  URLS,                " +
            "  LLMAnalysis          " +
            ") VALUES (?, ?, ?, ?, ?, ?)";

        try (
            Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setString(1, s.stockSymbol);
            ps.setString(2, s.companyName);
            ps.setBigDecimal(3, java.math.BigDecimal.valueOf(s.sentimentValue));
            ps.setTimestamp(4, new Timestamp(s.sentimentTimestamp.getTime()));

            // Build JSON array string from url1, url2, url3
            String urlsJson = "[\""
                + s.url1.replace("\"", "\\\"") + "\",\""
                + s.url2.replace("\"", "\\\"") + "\",\""
                + s.url3.replace("\"", "\\\"") + "\"]";

            ps.setObject(5, new org.postgresql.util.PGobject() {{
                setType("jsonb");
                setValue(urlsJson);
            }});

            ps.setString(6, s.llmAnalysis);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns a list of sentiment objects for the given stockSymbol,
     * ordered by SentimentTimestamp descending (latest first).
     */
    public ArrayList<sentiment> getLatestSentimentsByStockSymbol(String stockSymbol) {
        ArrayList<sentiment> results = new ArrayList<>();
        String sql =
            "SELECT StockSymbol, CompanyName, Sentiment, SentimentTimestamp, URLS, LLMAnalysis " +
            "FROM Stocks " +
            "WHERE StockSymbol = ? " +
            "ORDER BY SentimentTimestamp DESC";

        try (
            Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setString(1, stockSymbol);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String symbol = rs.getString("StockSymbol");
                    String company = rs.getString("CompanyName");
                    double sentimentValue = rs.getBigDecimal("Sentiment").doubleValue();

                    Timestamp ts = rs.getTimestamp("SentimentTimestamp");
                    Date sentimentDate = new Date(ts.getTime());

                    // Parse the JSONB "URLS" column (assumed format: ["url1","url2","url3"])
                    String urlsJson = rs.getString("URLS");
                    // Remove leading/trailing brackets and split by comma
                    String[] urlArray = urlsJson
                        .substring(1, urlsJson.length() - 1)  // strip [ and ]
                        .replace("\"", "")                   // remove quotes
                        .split(",");

                    // Ensure we have at least three entries (or pad with empty strings)
                    String u1 = urlArray.length > 0 ? urlArray[0] : "";
                    String u2 = urlArray.length > 1 ? urlArray[1] : "";
                    String u3 = urlArray.length > 2 ? urlArray[2] : "";

                    String analysis = rs.getString("LLMAnalysis");

                    sentiment s = new sentiment(symbol, company, sentimentValue, sentimentDate, u1, u2, u3, analysis);
                    results.add(s);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return results;
    }
}
