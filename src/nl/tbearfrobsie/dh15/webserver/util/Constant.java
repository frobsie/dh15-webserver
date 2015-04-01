package nl.tbearfrobsie.dh15.webserver.util;

public class Constant {
	public static Integer BUFFERSIZE = 128;
	public static String SERVER_OPENEDSOCKET = "Socket opened: ";
	public static String SERVER_CLOSINGSOCKET = "Closing socket: ";
	public static String SERVER_STDOUT_SERVER = "[SERVER] ";
	public static String SERVER_STDOUT_SEND = "[SEND]   ";
	public static String SERVER_STDOUT_RECV = "[RECV]   ";
	public static String SERVER_STDOUT_ERROR = "[ERROR]  ";

	public static String DEFAULT_DATE_FORMAT = "MM-dd-yyyy HH:mm:ss";

	public static String ERROR_CLIENT_CLOSED_CONNECTION = "Error : Client closed connection.";
	public static String ERROR_CLIENT_FILENOTEXISTS = "File does not exist.";

	public static String ADMIN_URI = "/adminerino";
	public static String URI_SHOWLOG = "/showlog";
	public static String URI_CLEARLOGS = "/clearlogs";
	public static String URI_LOGOUT = "/logout";
	public static String URI_USERS = "/users";
	public static String URI_USER_NEW = "/users/new";
	public static String URI_USER_DELETE = "/users/delete/";

	public static String EMPTY_STR = "";

	public static String MSG_PROTOCOL_GET = "GET";
	public static String MSG_PROTOCOL_POST = "POST";

	public static String MSG_PROTOCOL_HEADER_HTTP = "HTTP/1.0 ";
	public static String MSG_PROTOCOL_HEADER_CONTENTTYPE = "Content-Type: ";
	public static String MSG_PROTOCOL_HEADER_CONTENTLENGTH = "Content-Length: ";
	public static String MSG_PROTOCOL_HEADER_LOCATION = "Location: ";
	public static String MSG_PROTOCOL_HEADER_NOSNIFF = "X-Content-Type-Options: nosniff";
	public static String MSG_PROTOCOL_HEADER_CLICKJACK = "X-Frame-Options: deny";
	public static String COOKIENAME = "UserCookieId";
	public static String MSG_PROTOCOL_HEADER_COOKIE = "Set-Cookie: "+COOKIENAME+"=";
	public static String MSG_PROTOCOL_HEADER_COOKIE_TAIL = "; Path=/;";
	public static String MSG_PROTOCOL_REQUEST_HEADER_COOKIE = "Cookie: "+COOKIENAME+"=";


	public static String MSG_URI_ROOT = "/";
	public static String MSG_URI_PARENTFOLDER = "../";
	public static String MSG_PROTOCOL_DEFAULTMIMETYPE = "text/html";
	public static String MSG_SOCKINFO_DELIMITER_START = "[";
	public static String MSG_SOCKINFO_DELIMITER_END = "] ";
	public static String MSG_LOGS_CLEARED = "Logs cleared.";

	public static String URI_DELIMITER = "/";
	public static String URI_SPLIT_DELIMITER = "&";
	public static String URI_SPLIT_DELIMITER_VALUE = "=";

	public static String STATUSCODE_200_STR = "200 Ok";
	public static String STATUSCODE_203_STR = "203 No Content";
	public static String STATUSCODE_301_STR = "301 Moved Permanently";
	public static String STATUSCODE_307_STR = "307 Temporary Redirect";
	public static String STATUSCODE_400_STR = "400 Bad Request";
	public static String STATUSCODE_403_STR = "403 Forbidden";
	public static String STATUSCODE_404_STR = "404 Not Found";

	public static String POST_USERNAME = "username";
	public static String POST_PASSWORD = "password";
	public static String POST_ROLE = "role";
}
