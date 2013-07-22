package urlock;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;

@SuppressWarnings("serial")
public class Waitlock extends HttpServlet {
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		resp.setContentType("text/html");
		resp.getWriter().println("<style>body {min-width: 350px;overflow-x: hidden;}</style>");
		
		String url = req.getParameter("url");
		String email = req.getParameter("email");
		
		Entity wait = new Entity("Waits");
		wait.setProperty("Email", email);
		wait.setProperty("URL", url);
		
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		
		datastore.put(wait);
		
		resp.getWriter().println("You will receive a notification when the URL becomes free");
	}
}
