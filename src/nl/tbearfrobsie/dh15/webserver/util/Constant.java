package nl.tbearfrobsie.dh15.webserver.util;

public abstract class Constant {
	public static final Integer BUFFERSIZE = 128;
	public static final String SERVER_OPENEDSOCKET = "Socket opened: ";
	public static final String SERVER_CLOSINGSOCKET = "Closing socket: ";
	public static final String SERVER_STDOUT_SERVER = "[SERVER] ";
	public static final String SERVER_STDOUT_SEND = "[SEND]   ";
	public static final String SERVER_STDOUT_RECV = "[RECV]   ";
	public static final String SERVER_STDOUT_ERROR = "[ERROR]  ";

	public static final String DEFAULT_DATE_FORMAT = "MM-dd-yyyy HH:mm:ss";

	public static final String ERROR_CLIENT_CLOSED_CONNECTION = "Error : Client closed connection.";
	public static final String ERROR_CLIENT_FILENOTEXISTS = "File does not exist.";

	public static final String ADMIN_URI = "/admin";
	public static final String SETTINGS_URI = "/settings";
	public static final String URI_SHOWLOG = "/showlog";
	public static final String URI_CLEARLOGS = "/clearlogs";
	public static final String URI_LOGOUT = "/logout";
	public static final String URI_USERS = "/users";
	public static final String URI_USER_NEW = "/users/new";
	public static final String URI_USER_DELETE = "/users/delete";
	public static final String GET_ID = "id";
	public static final String URI_USER_DELETE_ID = "?"+GET_ID+"=";

	public static final String EMPTY_STR = "";

	public static final String MSG_PROTOCOL_GET = "GET";
	public static final String MSG_PROTOCOL_POST = "POST";
	public static final String MSG_PROTOCOL_HEAD = "HEAD";
	public static final String MSG_PROTOCOL_OPTIONS = "OPTIONS";
	public static final String MSG_PROTOCOL_DELETE = "DELETE";
	public static final String MSG_PROTOCOL_PUT = "PUT";
	public static final String MSG_PROTOCOL_PATCH = "PATCH";

	public static final String MSG_PROTOCOL_HEADER_HTTP = "HTTP/1.0 ";
	public static final String MSG_PROTOCOL_HEADER_CONTENTTYPE = "Content-Type: ";
	public static final String MSG_PROTOCOL_HEADER_CONTENTLENGTH = "Content-Length: ";
	public static final String MSG_PROTOCOL_HEADER_LOCATION = "Location: ";
	public static final String MSG_PROTOCOL_HEADER_NOSNIFF = "X-Content-Type-Options: nosniff";
	public static final String MSG_PROTOCOL_HEADER_CLICKJACK = "X-Frame-Options: deny";
	public static final String MSG_PROTOCOL_HEADER_PRAGMA = "Pragma: no-cache";
	public static final String MSG_PROTOCOL_HEADER_NOCACHE = "Cache-control: no-cache, no-store, must-revalidate, private";
	public static final String COOKIENAME = "UserCookieId";
	public static final String MSG_PROTOCOL_HEADER_COOKIE = "Set-Cookie: "+COOKIENAME+"=";
	public static final String MSG_PROTOCOL_HEADER_COOKIE_TAIL = "; Path=/;";
	public static final String MSG_PROTOCOL_REQUEST_HEADER_COOKIE = "Cookie: "+COOKIENAME+"=";


	public static final String MSG_URI_ROOT = "/";
	public static final String MSG_URI_PARENTFOLDER = "../";
	public static final String MSG_PROTOCOL_DEFAULTMIMETYPE = "text/html";
	public static final String MSG_SOCKINFO_DELIMITER_START = "[";
	public static final String MSG_SOCKINFO_DELIMITER_END = "] ";
	public static final String MSG_LOGS_CLEARED = "Logs cleared.";

	public static final String URI_DELIMITER = "/";
	public static final String URI_SPLIT_DELIMITER = "&";
	public static final String URI_SPLIT_DELIMITER_VALUE = "=";
	public static final String URI_SPLIT_PORT = ":";
	
	public static final String LOCALHOST = "https://localhost";

	public static final String STATUSCODE_200_STR = "200 Ok";
	public static final String STATUSCODE_203_STR = "203 No Content";
	public static final String STATUSCODE_301_STR = "301 Moved Permanently";
	public static final String STATUSCODE_302_STR = "302 Found";
	public static final String STATUSCODE_307_STR = "307 Temporary Redirect";
	public static final String STATUSCODE_400_STR = "400 Bad Request";
	public static final String STATUSCODE_403_STR = "403 Forbidden";
	public static final String STATUSCODE_404_STR = "404 Not Found";

	public static final String POST_USERNAME = "username";
	public static final String POST_PASSWORD = "password";
	public static final String POST_ROLE = "role";
	
	public static final String CSS_EXTENTION = ".css";
	public static final String CONTENTTYPE_CSS = "text/css";
	
	public static final String SQL_READUSER = "SELECT * FROM user WHERE username = ? ;";
	public static final String SQL_READUSERBYCOOKIE = "SELECT * FROM user WHERE cookieId = ? ;";
	public static final String SQL_DELETEUSER = "DELETE FROM user WHERE id = ? ;";
	public static final String SQL_READALLUSERS = "SELECT * FROM user;";
	public static final String SQL_UPDATEUSER = "UPDATE user SET cookieId = ? WHERE id = ?;";
	public static final String SQL_CREATEUSER = "INSERT INTO user (username, password, role) VALUES (?,?,?);";
}
