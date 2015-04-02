package nl.tbearfrobsie.dh15.webserver;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import nl.tbearfrobsie.dh15.webserver.util.Constant;

public class MySQLAccess {
	
	/** Socket instance */
	private Connection connect = null;
	
	/** Statement which holds a query */
	private PreparedStatement preparedStatement = null;
	
	/** Database resultset */
	private ResultSet resultSet = null;
	
	/** Random needed for cookie id generation */
	private SecureRandom random = new SecureRandom();

	/**
	 * Constructor.
	 * Tries to connect to a databse
	 * based on values in the config.
	 */
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

	/**
	 * Retrieves a user from the database
	 * based on the given username.
	 * 
	 * @param String username
	 * @return User
	 */
	public User readUser(String username) {
		try {
			preparedStatement = connect.prepareStatement(Constant.SQL_READUSER);
			preparedStatement.setString(1, username);
			resultSet = preparedStatement.executeQuery();
			if(resultSet.next()){
				return writeResultSet(resultSet);
			} else {
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Retrieves a user from the database
	 * based on the given cookie.
	 * 
	 * @param String cookie
	 * @return User
	 */
	public User readUserByCookie(String cookie) {
		try {
			preparedStatement = connect.prepareStatement(Constant.SQL_READUSERBYCOOKIE);
			preparedStatement.setString(1, cookie);
			resultSet = preparedStatement.executeQuery();
			if(resultSet.next()){
				return writeResultSet(resultSet);
			} else {
				return null;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Deletes a user from the database.
	 * 
	 * @param int id
	 * @return void
	 */
	public void deleteUser(int id) {
		try {
			preparedStatement = connect.prepareStatement(Constant.SQL_DELETEUSER);
			preparedStatement.setInt(1, id);
			preparedStatement.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Retrieves all available users
	 * from the databse.
	 * 
	 * @return ArrayList<User>
	 */
	public ArrayList<User> readUsers() {
		try {
			preparedStatement = connect.prepareStatement(Constant.SQL_READALLUSERS);
			resultSet = preparedStatement.executeQuery();
			ArrayList<User> users = new ArrayList<User>();
			while(resultSet.next()) {
				users.add(writeResultSet(resultSet));
			}
			return users;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new ArrayList<User>();
	}

	/**
	 * Updates a user in the database.
	 * 
	 * @param User user
	 * @return void
	 */
	public void storeUser(User user) {
		try {
			preparedStatement = connect.prepareStatement(Constant.SQL_UPDATEUSER);
			preparedStatement.setString(1, user.getCookieId());
			preparedStatement.setInt(2, user.getId());
			preparedStatement.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Creates a new user.
	 * 
	 * @param User user
	 * @return void
	 */
	public void createUser(User user) {
		try {
			preparedStatement = connect.prepareStatement(Constant.SQL_CREATEUSER);
			preparedStatement.setString(1, user.getUsername());
			preparedStatement.setString(2, user.getPassword());
			preparedStatement.setString(3, user.getRole());
			preparedStatement.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Creates a token.
	 * 
	 * @param String cookieId
	 * @return void
	 */
	public String createToken(String cookieId) {
		try {
			String token = new BigInteger(130, random).toString(32);
			preparedStatement = connect.prepareStatement(Constant.SQL_CREATETOKEN);
			preparedStatement.setString(1, cookieId);
			preparedStatement.setString(2, token);
			preparedStatement.executeUpdate();
			return token;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Valid token.
	 * 
	 * @param String cookieId
	 * @param String token
	 * @return void
	 */
	public boolean validToken(String cookieId, String token) {
		try {
			preparedStatement = connect.prepareStatement(Constant.SQL_READTOKEN);
			preparedStatement.setString(1, cookieId);
			resultSet = preparedStatement.executeQuery();
			while(resultSet.next()) {
				if(resultSet.getString("token").equals(token)) {
					preparedStatement = connect.prepareStatement(Constant.SQL_DELETETOKEN);
					preparedStatement.setString(1, cookieId);
					preparedStatement.setString(2, token);
					preparedStatement.executeUpdate();
					return true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Save resultset in the database.
	 * 
	 * @param ResultSet resultSet
	 * @return User
	 * @throws SQLException
	 */
	private User writeResultSet(ResultSet resultSet) throws SQLException {
		// ResultSet is initially before the first data set
		int id = resultSet.getInt("id");
		String user = resultSet.getString("username");
		String password = resultSet.getString("password");
		String role = resultSet.getString("role");
		String cookieId = resultSet.getString("cookieId");

		return new User(id, user, password, role, cookieId);
	}

	/**
	 * Close a ResultSet object.
	 * @return void
	 */
	public void close() {
		try {
			if (resultSet != null) {
				resultSet.close();
			}

			if (connect != null) {
				connect.close();
			}
		} catch (Exception e) {
			// TODO
		}
	}
}
