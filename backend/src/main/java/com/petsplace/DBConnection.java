package com.petsplace;

import java.sql.*;

public class DBConnection {
    private static final String URL  = "jdbc:mysql://127.0.0.1:3306/petsplace";
    private static final String USER = "root";
    private static final String PASS = "";   // XAMPP default is empty

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }

    public static void main(String[] args) {
        try (Connection conn = getConnection()) {
            System.out.println("Connected to MySQL!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}