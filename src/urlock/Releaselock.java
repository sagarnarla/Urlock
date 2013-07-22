package urlock;

import java.io.IOException;
import javax.servlet.http.*;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.PreparedQuery.TooManyResultsException;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;

import javax.mail.Message;
import java.util.Properties;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

@SuppressWarnings("serial")
public class Releaselock extends HttpServlet {
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		resp.setContentType("text/html");
		resp.getWriter().println("<style>body {min-width: 350px;overflow-x: hidden;}</style>");
		
		String user = req.getParameter("user");
		String url = req.getParameter("url");
		
		//Delete Locks Entity by locating it and calling its delete() method
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Filter urlfilter = new FilterPredicate("URL",FilterOperator.EQUAL,url);
		Filter userfilter = new FilterPredicate("User",FilterOperator.EQUAL,user);
		Filter findfilter = CompositeFilterOperator.and(urlfilter,userfilter);
		Query query1 = new Query("Locks").setFilter(findfilter);
		PreparedQuery pq1 = datastore.prepare(query1);
		
		try
		{
			Entity release = pq1.asSingleEntity();
			if(release != null)
			{
				datastore.delete(release.getKey());
			}
			else
			{
				resp.getWriter().println("Could not release lock! Entity not found");
				return;
			}
				
		}
		catch(TooManyResultsException ex)
		{
			//Datastore inconsistency Problem
			resp.getWriter().println("Datastore inconsistent! Error!");
		}
		
		//Send Email to all Waits
		Query querywaits = new Query("Waits").setFilter(urlfilter);
		PreparedQuery pq = datastore.prepare(querywaits);
		
		Iterable<Entity> results = pq.asIterable();
		
		if(results == null)
		{
			//resp.getWriter().println("Null results!");
			return;
		}

		Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);
        String msgBody = "The URL you were waiting on has been released! \nGo grab it!\n"+url;
        
			
			try {
	            Message msg = new MimeMessage(session);
	            msg.setFrom(new InternetAddress("ambika799@gmail.com", "Urlock Notifier"));
	            
	            for(Entity waiter : results)
	    		{
	    			msg.addRecipient(Message.RecipientType.TO,new InternetAddress((String)waiter.getProperty("Email"), "Mr. User"));
	    			datastore.delete(waiter.getKey());		
	    		}
	            	            
	            
	            msg.setSubject("URL Release notification");
	            msg.setText(msgBody);
	            Transport.send(msg);
	    
	        } catch (AddressException e) {
	            //
	        	resp.getWriter().println("Email address for Notification incorrect :(");
	        } catch (MessagingException e) {
	            //
	        	resp.getWriter().println("Problem with sending notification email");
	        } catch (Exception e) {
	        	resp.getWriter().println("Gotcha!");
	        }
				
		resp.getWriter().println("The Url is now free!");
	}

}
