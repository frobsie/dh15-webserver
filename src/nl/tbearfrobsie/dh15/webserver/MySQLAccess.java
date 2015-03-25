package nl.tbearfrobsie.dh15.webserver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MySQLAccess {
	private Connection connect = null;
	private PreparedStatement preparedStatement = null;
	private ResultSet resultSet = null;

	public MySQLAccess() {

		try {
			// This will load the MySQL driver, each DB has its own driver
			Class.forName("com.mysql.jdbc.Driver");
			
			String host = ConfigPropertyValues.get(ConfigPropertyValues.CONFIG_DATABASE_HOST);
			String name = ConfigPropertyValues.get(ConfigPropertyValues.CONFIG_DATABASE_NAME);
			String username = ConfigPropertyValues.get(ConfigPropertyValues.CONFIG_DATABASE_USERNAME);
			String password = ConfigPropertyValues.get(ConfigPropertyValues.CONFIG_DATABASE_PASSWORD);
			// Setup the connection with the DB
			connect = DriverManager
					.getConnection("jdbc:mysql://"+host+"/"+name+"?"
							+ "user="+username+"&password="+password);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public User readUser(String username) {
		try {
			preparedStatement = connect.prepareStatement("SELECT * FROM user WHERE username = ? ;");
			preparedStatement.setString(1, username);
			resultSet = preparedStatement.executeQuery();
			return writeResultSet(resultSet);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;

	}
	
	public User readUserByCookie(String cookie) {
		try {
			preparedStatement = connect.prepareStatement("SELECT * FROM user WHERE cookieId = ? ;");
			preparedStatement.setString(1, cookie);
			resultSet = preparedStatement.executeQuery();
			return writeResultSet(resultSet);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;

	}
	
	public void storeUser(User user) {
		try {
			preparedStatement = connect.prepareStatement("UPDATE user SET cookieId = ? WHERE id = ?;");
			preparedStatement.setString(1, user.getCookieId());
			preparedStatement.setInt(2, user.getId());
			preparedStatement.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private User writeResultSet(ResultSet resultSet) throws SQLException {
		// ResultSet is initially before the first data set
		if(resultSet.next()){
			int id = resultSet.getInt("id");
			String user = resultSet.getString("username");
			String password = resultSet.getString("password");
			String role = resultSet.getString("role");
			String cookieId = resultSet.getString("cookieId");
			
			return new User(id, user, password, role, cookieId);
		} else {
			return null;
		}
	}

	// You need to close the resultSet
	public void close() {
		try {
			if (resultSet != null) {
				resultSet.close();
			}

			if (connect != null) {
				connect.close();
			}
		} catch (Exception e) {

		}
	}

} 