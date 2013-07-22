package urlock;

import java.io.IOException;
import javax.servlet.http.*;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.PreparedQuery.TooManyResultsException;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;

@SuppressWarnings("serial")
public class UrlockServlet extends HttpServlet {
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		resp.setContentType("text/html");
		resp.getWriter().println("<style>body {min-width: 350px;overflow-x: hidden;}</style>");
		//resp.getWriter().println("Hello, world");
		
		String url = req.getParameter("url");
		String user = req.getParameter("user");
				
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Filter urlfilter = new FilterPredicate("URL",FilterOperator.EQUAL,url);
		Filter userfilter = new FilterPredicate("User",FilterOperator.EQUAL,user);
		Filter findfilter = CompositeFilterOperator.and(urlfilter,userfilter);
		Query query1 = new Query("Locks").setFilter(findfilter);
		Query query2 = new Query("Locks").setFilter(urlfilter);
		PreparedQuery pq1 = datastore.prepare(query1);
		PreparedQuery pq2 = datastore.prepare(query2);
		
		try
		{                                                                             
			Entity userhas = pq1.asSingleEntity();
			Entity someonehas = pq2.asSingleEntity(); 
			if(userhas == null && someonehas != null)
			{
				//User will have to wait
				resp.getWriter().println("URL is locked<br/> We can notify you when it is freed if you like.<br/>");
				resp.getWriter().println("<form action=\"http://httpurlock.appspot.com/waitlock\" method=\"POST\">");
				resp.getWriter().println("Your email: <input type=\"text\" name=\"email\" id=\"email\">");
				resp.getWriter().println(" <input type=\"hidden\" name=\"url\" id=\"url\" value=\""+url+"\">");
				resp.getWriter().println("<input type=\"submit\" value=\"Notify Me\">");
			}
			else if(userhas != null)
			{
				//User will want to release
				//resp.getWriter().println("Release URL?");
				resp.getWriter().println("<form action=\"http://httpurlock.appspot.com/releaselock\" method=\"POST\">");
				resp.getWriter().println("<input type=\"hidden\" name=\"user\" id=\"user\" value=\""+user+"\">");
				resp.getWriter().println("<input type=\"hidden\" name=\"url\" id=\"url\" value=\""+url+"\">");
				resp.getWriter().println("<input type=\"submit\" value=\"Release URL\">");
			}
			else if(userhas == null && someonehas == null)
			{
				//User can have the url
				Entity lock = new Entity("Locks");
				lock.setProperty("URL", url);
				lock.setProperty("User", user);
				datastore.put(lock);
				resp.getWriter().println("You have the URL!!");
			}
			else
			{
				resp.getWriter().println("Whats happening?");
			}
			
		}
		catch(TooManyResultsException ex)
		{
			//Datastore inconsistency Problem
			resp.getWriter().println("Datastore inconsistent! Error!");
		}
	}
}
