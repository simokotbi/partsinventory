package com.partsinventory.service;

import com.partsinventory.helper.DbConnection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginService {

    public static boolean checkCredentials(String user, String pass) throws SQLException {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        String query = DbConnection.load("LOGIN");
        try {
            preparedStatement = DbConnection.getConnection().prepareStatement(query);
            preparedStatement.setString(1, user);
            preparedStatement.setString(2, pass);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        } finally {
            preparedStatement.close();
            resultSet.close();
        }
    }
}
