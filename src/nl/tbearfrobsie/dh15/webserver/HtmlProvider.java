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

public class HtmlProvider {

	public static String header() {
		return "<!DOCTYPE html>\n<html>\n<head>\n<meta charset=\"UTF-8\" />\n</head>\n<body>\n";
	}
	public static String tail() {
		return "\n</body>\n</html>";
	}

	/**
	 * Get the form the manage the server configuration
	 * @return
	 */
	public static String getManageForm(User user) {
		String admin = header();
		if(user.isLoggedIn()) {
			admin +=  "<form method=\"post\" action=\"" +
					Constant.ADMIN_URI +
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
					"    <tr><td><a href=\"" + Constant.URI_SHOWLOG + "\">Show log</a></td>\n" +
					"        <td class=\"right\"><input value=\"OK\"";
			if(user.hasRole(User.ROLE_ONDERSTEUNERS)) {
				admin += "disabled=\"disabled\"";
			}
			admin += " type=\"submit\"></td>\n" +
					"    </tr>\n";
			if(user.hasRole(User.ROLE_BEHEERDERS)) {		
				admin += "    <tr><td><a href=\"" + Constant.URI_CLEARLOGS + "\">Clear logs</a></td></tr>\n";
				admin += "    <tr><td><a href=\"" + Constant.URI_USERS + "\">Users</a></td></tr>\n";
			}

			admin += "    <tr><td><a href=\"" + Constant.URI_LOGOUT + "\">Logout</a></td></tr>\n";
			admin += " </tbody>\n" +
					"</table>\n" +
					"</form>";
		} else {
			admin += "<form method=\"post\" action=\"" +
					Constant.ADMIN_URI +
					"\">\n" +
					"<table>\n" +
					"<thead>\n" +
					"    <tr><th>Login</th><th class=\"right\"></th></tr>\n" +
					"</thead>\n" +
					"<tbody>\n" +
					"    <tr><td>Username:</td><td><input name=\"username\" value=\"\" type=\"text\"></td></tr>\n" +
					"    <tr><td>Password:</td><td><input name=\"password\" value=\"\" type=\"password\"></td></tr>\n" +
					"    <tr><td></td>\n" +
					"        <td class=\"right\"><input value=\"OK\" type=\"submit\"></td>\n" +
					"    </tr>\n" +
					"</tbody>\n" +
					"</table>\n" +
					"</form>";
		}
		return admin + tail();
	}

	public static String getNewUserForm() {
		return header() + "<form method=\"post\" action=\"" +
				Constant.URI_USER_NEW +
				"\">\n" +
				"<table>\n" +
				"<thead>\n" +
				"    <tr><th>Nieuwe gebruiker</th><th class=\"right\"></th></tr>\n" +
				"</thead>\n" +
				"<tbody>\n" +
				"    <tr><td>Username:</td><td><input name=\"username\" value=\"\" type=\"text\"></td></tr>\n" +
				"    <tr><td>Password:</td><td><input name=\"password\" value=\"\" type=\"password\"></td></tr>\n" +
				"	 <tr><td>Role: </td><td><select name=\"role\"><option value=\""+User.ROLE_BEHEERDERS+"\">"+User.ROLE_BEHEERDERS+"</option><option value=\""+User.ROLE_ONDERSTEUNERS+"\">"+User.ROLE_ONDERSTEUNERS+"</option></select>\n" +
				"    <tr><td></td>\n" +
				"        <td class=\"right\"><input value=\"Opslaan\" type=\"submit\"></td>\n" +
				"    </tr>\n" +
				"</tbody>\n" +
				"</table>\n" +
				"</form>" + tail();
	}

	public static String listUserContent() {
		MySQLAccess msa = new MySQLAccess();
		ArrayList<User> users = msa.readUsers();

		msa.close();

		String usertabel =  header() + "<a href=\""+Constant.URI_USER_NEW+"\">Gebruiker toevoegen</a>"
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
						+ "<td><a href=\""+Constant.URI_USER_DELETE+user.getId()+"\">Verwijderen</a></td>"
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
	public static String getLog() throws IOException {
		String retVal = header() + "<table>"
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
	 * Build and send directory structure to client for the given uri
	 * @param uri
	 * @return
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
}
