package nl.tbearfrobsie.dh15.webserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import nl.tbearfrobsie.dh15.webserver.util.Constant;
import nl.tbearfrobsie.dh15.webserver.util.Logger;

public class HTMLProvider {

	/**
	 * Generates a HTML 5 DOCTYPE,
	 * header and body start tag.
	 * 
	 * @return String
	 */
	public static String header(User user) {
		String returnHtml =  "<!DOCTYPE html>\n"
				+ "<html>\n"
				+ "<head>\n"
				+ "<meta charset=\"UTF-8\" />\n"
				+ "<link href=\"/bootstrap.min.css\" rel=\"stylesheet\">"
				+ "<link href=\"/dashboard.css\" rel=\"stylesheet\">"
				+ "</head>\n"
				+ "<body>\n"
				+ "<nav class=\"navbar navbar-inverse navbar-fixed-top\">"
				+ "<div class=\"container-fluid\">"
				+ "<div class=\"navbar-header\">"
				+ "<button type=\"button\" class=\"navbar-toggle collapsed\" data-toggle=\"collapse\" data-target=\"#navbar\" aria-expanded=\"false\" aria-controls=\"navbar\">"
				+ "<span class=\"sr-only\">Toggle navigation</span>"
				+ "<span class=\"icon-bar\"></span>"
				+ "<span class=\"icon-bar\"></span>"
				+ "<span class=\"icon-bar\"></span>"
				+ "</button>"
				+ "<a class=\"navbar-brand\" href=\"" + Constant.ADMIN_URI + "\">IDH15 Admin</a>"
				+ "</div>"
				+ "<div id=\"navbar\" class=\"navbar-collapse collapse\">"
				+ "</div>"
				+ "</div>"
				+ "</nav>"
				+ "<div class=\"container-fluid\">"
				+ "<div class=\"row\">"
				+ "<div class=\"col-sm-3 col-md-2 sidebar\">"
				+ "<ul class=\"nav nav-sidebar\">"
				+ "<li><a href=\"" + Constant.SETTINGS_URI + "\">Settings</a></li>"
				+ "<li><a href=\"" + Constant.URI_SHOWLOG + "\">Show log</a></li>";
		
		if(user.hasRole(User.ROLE_BEHEERDERS)) {		
			returnHtml += "<li><a href=\"" + Constant.URI_CLEARLOGS + "\">Clear logs</a></li>"
					+ "<li><a href=\"" + Constant.URI_USERS + "\">Show users</a></li>";
		}
		
		returnHtml += "<li><a href=\"" + Constant.URI_LOGOUT + "\">Log out</a></li>"
				+ "</ul>"
				+ "</div>"
				+ "<div class=\"col-sm-9 col-sm-offset-3 col-md-10 col-md-offset-2 main\">";

		return returnHtml;
	}
	
	/**
	 * Generates a HTML 5 DOCTYPE,
	 * header and body start tag.
	 * 
	 * @return String
	 */
	public static String header() {
		String returnHtml =  "<!DOCTYPE html>\n"
				+ "<html>\n"
				+ "<head>\n"
				+ "<meta charset=\"UTF-8\" />\n"
				+ "<link href=\"/bootstrap.min.css\" rel=\"stylesheet\">"
				+ "<link href=\"/dashboard.css\" rel=\"stylesheet\">"
				+ "</head>\n"
				+ "<body>\n"
				+ "<nav class=\"navbar navbar-inverse navbar-fixed-top\">"
				+ "<div class=\"container-fluid\">"
				+ "<div class=\"navbar-header\">"
				+ "<button type=\"button\" class=\"navbar-toggle collapsed\" data-toggle=\"collapse\" data-target=\"#navbar\" aria-expanded=\"false\" aria-controls=\"navbar\">"
				+ "<span class=\"sr-only\">Toggle navigation</span>"
				+ "<span class=\"icon-bar\"></span>"
				+ "<span class=\"icon-bar\"></span>"
				+ "<span class=\"icon-bar\"></span>"
				+ "</button>"
				+ "<a class=\"navbar-brand\" href=\"" + Constant.ADMIN_URI + "\">IDH15 Admin</a>"
				+ "</div>"
				+ "<div id=\"navbar\" class=\"navbar-collapse collapse\">"
				+ "</div>"
				+ "</div>"
				+ "</nav>"
				+ "<div class=\"container-fluid\">"
				+ "<div class=\"row\">"
				+ "<div class=\"col-sm-3 col-md-2 sidebar\">"
				+ "</div>"
				+ "<div class=\"col-sm-9 col-sm-offset-3 col-md-10 col-md-offset-2 main\">";

		return returnHtml;
	}
	
	public static String clearLogs(User user)
	{
		String returnHtml = HTMLProvider.header(user);
		returnHtml += Constant.MSG_LOGS_CLEARED;
		returnHtml += HTMLProvider.tail();
		
		return returnHtml;
	}

	/**
	 * Closes the body and html tag.
	 *  
	 * @return String
	 */
	public static String tail() {
		return "\n</div></body>\n</html>";
	}

	/**
	 * Outputs the login form.
	 * 
	 * @return String
	 */
	public static String getLoginForm(User user) {
		String admin = header();
		admin += "<form method=\"post\" action=\"" +
				Constant.ADMIN_URI +
				"\">\n" +
				"<table>\n" +
				"<thead>\n" +
				"    <tr><th>Login</th><th class=\"right\"></th></tr>\n" +
				"</thead>\n" +
				"<tbody>\n" +
				"    <tr><td>Username:</td><td><input name=\"username\" value=\"\" type=\"text\"></td></tr>\n" +
				"    <tr><td>Password:</td><td><input name=\"password\" value=\"\" type=\"password\" autocomplete=\"off\"></td></tr>\n" +
				"    <tr><td></td>\n" +
				"        <td class=\"right\"><input value=\"OK\" type=\"submit\"></td>\n" +
				"    </tr>\n" + 
				addCsrfField(user) +
				"</tbody>\n" +
				"</table>\n" +
				"</form>";
		return admin + tail();
	}

	/**
	 * Get the form the manage the server configuration.
	 * 
	 * @param User user
	 * @return String
	 */
	public static String getManageForm(User user) {
		String admin = header(user);
		admin +=  "<form method=\"post\" action=\"" +
				Constant.SETTINGS_URI +
				"\">\n" +
				"<table>\n" +
				"<thead>\n" +
				"    <tr><th>SuperServer</th><th class=\"right\">Control Panel</th></tr>\n" +
				"</thead>\n" +
				"<tbody>\n" +
				"    <tr><td>Web port:</td><td><input name=\"port\" value=\"" +
				""+ ConfigPropertyValues.get(ConfigPropertyValues.CONFIG_KEY_PORT)+"" +
				"\" ";
		if(user.hasRole(User.ROLE_ONDERSTEUNERS)) {
			admin += "disabled=\"disabled\"";
		}
		admin += "type=\"text\"></td></tr>\n" +
				"    <tr><td>Webroot:</td><td><input name=\"docroot\" value=\"" +
				""+ ConfigPropertyValues.get(ConfigPropertyValues.CONFIG_KEY_DOCROOT)+"" +
				"\" ";
		if(user.hasRole(User.ROLE_ONDERSTEUNERS)) {
			admin += "disabled=\"disabled\"";
		}
		admin += "type=\"text\"></td></tr>\n" +
				"    <tr><td>Default page:</td><td><input name=\"defaultpage\" value=\"" +
				""+ ConfigPropertyValues.get(ConfigPropertyValues.CONFIG_KEY_DEFAULTPAGE)+"" +
				"\" ";
		if(user.hasRole(User.ROLE_ONDERSTEUNERS)) {
			admin += "disabled=\"disabled\"";
		}
		admin += "type=\"text\"></td></tr>\n" +
				"    <tr><td>Directory browsing</td><td><input name=\"directorybrowsing\" type=\"checkbox\"";
		if(ConfigPropertyValues.get(ConfigPropertyValues.CONFIG_KEY_DIRECTORYBROWSING).equals(ConfigPropertyValues.CONFIG_VALUE_STR_TRUE)) {
			admin += " checked=\"checked\"";
		}

		if(user.hasRole(User.ROLE_ONDERSTEUNERS)) {
			admin += "disabled=\"disabled\"";
		}

		admin += "></td></tr>\n" +
				"    <tr><td></td>\n" +
				"        <td class=\"right\"><input value=\"OK\"";
		if(user.hasRole(User.ROLE_ONDERSTEUNERS)) {
			admin += "disabled=\"disabled\"";
		}
		admin += " type=\"submit\"></td>\n" +
				"    </tr>\n";
		
		admin += addCsrfField(user);
		admin += " </tbody>\n" +
				"</table>\n" +
				"</form>";

		return admin + tail();
	}

	/**
	 * Generates a form with which
	 * a user can be created.
	 * 
	 * @return String
	 */
	public static String getNewUserForm(User user) {
		return header(user) + "<form method=\"post\" action=\"" +
				Constant.URI_USER_NEW +
				"\">\n" +
				"<table>\n" +
				"<thead>\n" +
				"    <tr><th>Nieuwe gebruiker</th><th class=\"right\"></th></tr>\n" +
				"</thead>\n" +
				"<tbody>\n" +
				"    <tr><td>Username:</td><td><input name=\"username\" value=\"\" type=\"text\"></td></tr>\n" +
				"    <tr><td>Password:</td><td><input name=\"password\" value=\"\" type=\"password\" autocomplete=\"off\"></td></tr>\n" +
				"	 <tr><td>Role: </td><td><select name=\"role\"><option value=\""+User.ROLE_BEHEERDERS+"\">"+User.ROLE_BEHEERDERS+"</option><option value=\""+User.ROLE_ONDERSTEUNERS+"\">"+User.ROLE_ONDERSTEUNERS+"</option></select>\n" +
				"    <tr><td></td>\n" +
				"        <td class=\"right\"><input value=\"Opslaan\" type=\"submit\"></td>\n" +
				"    </tr>\n" +
				addCsrfField(user) +
				"</tbody>\n" +
				"</table>\n" +
				"</form>" + tail();
	}

	/**
	 * Generates a table which lists
	 * all the users currently available in the system.
	 * 
	 * @return String
	 */
	public static String listUserContent(User currentUser) {
		MySQLAccess msa = new MySQLAccess();
		ArrayList<User> users = msa.readUsers();

		msa.close();

		String usertabel =  header(currentUser) + "<a href=\""+Constant.URI_USER_NEW+"\">Gebruiker toevoegen</a>"
				+ "<table>"
				+ "<tr>"
				+ "<td>"
				+ "Gebruikersnaam"
				+ "</td>"
				+ "<td>"
				+ "Role"
				+ "</td>"
				+ "<td>"
				+ "Verwijderen"
				+ "</td>"
				+ "</tr>";
		if(users.size() == 0) {
			usertabel += ""
					+ "<tr>"
					+ "<td>Geen gebruikers gevonden!</td>"
					+ "</tr>";
		} else {
			Iterator<User> userIt = users.iterator();
			while(userIt.hasNext()) {
				User user = userIt.next();
				usertabel += ""
						+ "<tr>"
						+ "<td>"+user.getUsername()+"</td>"
						+ "<td>"+user.getRole()+"</td>"
						+ "<td><a href=\""+Constant.URI_USER_DELETE+Constant.URI_USER_DELETE_ID+user.getId()+"\">Verwijderen</a></td>"
						+ "</tr>";
			}

		}

		usertabel += "</table>" + tail();

		return usertabel;
	}

	// TODO
	// memory management + styling
	/**
	 * Opens the access log file, and adds
	 * some basic html.
	 * 
	 * @return String
	 * @throws IOException
	 */
	public static String getLog(User currentUser) throws IOException {
		String retVal = header(currentUser) + "<table>"
				+ "<tbody>";

		FileInputStream fstream = new FileInputStream(Logger.LOG_FILE_ACCESS_EXTENDED);
		BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

		String strLine;

		while ((strLine = br.readLine()) != null)   {
			retVal += "<tr><td>" + strLine + "</td></tr>";
		}

		br.close();

		retVal += "</tbody>"
				+ "</table>" + tail();

		return retVal;
	}

	/**
	 * Build and send directory structure to client for the given uri.
	 * 
	 * @param String uri
	 * @return String
	 */
	public static String listFolderContent(String uri) {
		String path = ConfigPropertyValues.get(ConfigPropertyValues.CONFIG_KEY_DOCROOT) + Constant.URI_DELIMITER + uri;
		File f = new File(path);
		List<File> list = Arrays.asList(f.listFiles());
		Iterator<File> fileIterator = list.iterator();

		String fileListingHtml = "<h1>Index of " + uri + "</h1>";
		fileListingHtml += "<table>";
		fileListingHtml += "<thead style=\"text-align:left;\">";
		fileListingHtml += "<tr>";
		fileListingHtml += "<th style=\"padding:10px\">filename</th>";
		fileListingHtml += "<th style=\"padding:10px\">lastmodified</th>";
		fileListingHtml += "<th style=\"padding:10px\">size</th>";
		fileListingHtml += "</tr>";
		fileListingHtml += "</thead>";
		fileListingHtml += "<tbody>";

		if (!uri.equals(Constant.URI_DELIMITER)) {
			// Parent uri opbouwen
			ArrayList<String> uriParts = new ArrayList<String>(Arrays.asList(uri.split(Constant.URI_DELIMITER)));

			if ((uriParts.size() -1) > 0) {
				uriParts.remove(uriParts.size()-1);
			}

			Iterator<String> uriIterator = uriParts.iterator();
			String newUri = Constant.URI_DELIMITER;

			while(uriIterator.hasNext()) {
				String uriPart = uriIterator.next();

				if (!uriPart.equals(Constant.EMPTY_STR)) {
					newUri += uriPart + Constant.URI_DELIMITER;	
				}
			}			

			fileListingHtml += "<tr>";
			fileListingHtml += "<td style=\"padding:10px\" colspan=\"3\"><a href=\""+newUri+"\">Parent Directory</a></td>";
			fileListingHtml += "</tr>";
		}

		// Date formatter voor de lastmodified date van de file
		SimpleDateFormat sdf = new SimpleDateFormat(Constant.DEFAULT_DATE_FORMAT);

		while(fileIterator.hasNext()) {
			File file = fileIterator.next();
			String filePath = uri;

			if (uri.equals(Constant.URI_DELIMITER)) {
				filePath = uri + file.getName();
			} else {
				filePath = uri + Constant.URI_DELIMITER +file.getName();
			}

			fileListingHtml += "<tr>";
			fileListingHtml += "<td style=\"padding:10px\"><a href=\""+ filePath +"\">"+file.getName()+"</a></td>";
			fileListingHtml += "<td style=\"padding:10px\">"+sdf.format(file.lastModified())+"</td>";
			fileListingHtml += "<td style=\"padding:10px\">"+file.length()+"</td>";
			fileListingHtml += "</tr>";
		}

		fileListingHtml += "</tbody>";
		fileListingHtml += "</table>";

		return fileListingHtml;
	}

	private static String addCsrfField(User user) {
		MySQLAccess msa = new MySQLAccess();
		String token = msa.createToken(user.getCookieId());
		msa.close();

		return "<input type=\"hidden\" name=\"csrf\" value=\""+token+"\" />";
	}
}

