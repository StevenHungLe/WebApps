/**
 * the class represent the REST server
 * listens to and process REST requests using curl
 */

package csci4311.chatExtra;

import javax.json.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.Executors;

import com.sun.net.httpserver.*;

public class RestServer {
	//** instance variables **\\
	// the ChatServer object that maintains the chat system's data
	private ChatServer server;
	
	/*public static void main(String[] args) throws IOException {
		
	}*/

	public RestServer( ChatServer chatServer, int restPort) throws IOException 
	{
		this.server = chatServer;

		InetSocketAddress addr = new InetSocketAddress(restPort);
		HttpServer httpServer = HttpServer.create(addr, 0);
	
		httpServer.createContext( "/users", new UsersHandler(server));	
		httpServer.createContext( "/groups", new GroupsHandler(server));
		httpServer.createContext( "/group/", new GroupHandler(server));	
		httpServer.createContext( "/messages/", new MessagesHandler(server));
		httpServer.createContext( "/message", new MessageHandler(server));	
		httpServer.setExecutor( Executors.newCachedThreadPool());
		httpServer.start();
		System.out.println("RestServer is listening on port "+ restPort);
	}
}



// Handler for context /users
// GET /users
@SuppressWarnings("unchecked")
class UsersHandler implements HttpHandler {
	private ChatServer server; 

	public UsersHandler( ChatServer server )
	{
		this.server = server;
	}
	public void handle( HttpExchange exchange) throws IOException {
		
		// send the responseHeader with the response code
		Headers responseHeaders = exchange.getResponseHeaders();
		responseHeaders.set( "Content-Type", "text/plain");
		exchange.sendResponseHeaders( 200, 0);

		// the printstream to send response
		PrintStream response = new PrintStream( exchange.getResponseBody());
		
		// use JsonObjectBuilder to build a Json-encoded response
		JsonObjectBuilder jsObjectBuilder = Json.createObjectBuilder();
		
		// get the users list from ChatServer, use it to create Json response
		Set<String> users = server.getUsers();
		if (!users.isEmpty())
		{
			JsonArrayBuilder jsArrayBuilder = Json.createArrayBuilder();
			for ( String u : users )
			{
				jsArrayBuilder.add(u);
			}
			jsObjectBuilder.add("users",jsArrayBuilder.build());
		}
		
		// send the response
		response.println( jsObjectBuilder.build());
		response.close();
	}	
}


// Handler for context /groups
// GET /groups
@SuppressWarnings("unchecked")
class GroupsHandler implements HttpHandler {
	private ChatServer server; 
	public GroupsHandler ( ChatServer server )
	{
		this.server = server;
	}
	public void handle( HttpExchange exchange) throws IOException {
		
		// send the responseHeader with the response code
		Headers responseHeaders = exchange.getResponseHeaders();
		responseHeaders.set( "Content-Type", "text/plain");
		exchange.sendResponseHeaders( 200, 0);

		// the printstream to send response
		PrintStream response = new PrintStream( exchange.getResponseBody());
		
		// use JsonObjectBuilder to build a Json-encoded response
		JsonObjectBuilder jsObjectBuilder = Json.createObjectBuilder();
		
		// get the group list from ChatServer, use it to create Json response
		Set<String> groups = server.getGroups();
		if (!groups.isEmpty())
		{
			JsonArrayBuilder jsArrayBuilder = Json.createArrayBuilder();
			for ( String g : groups )
			{
				jsArrayBuilder.add(g);
			}
			jsObjectBuilder.add("groups",jsArrayBuilder.build());
		}
		
		// send the response
		response.println( jsObjectBuilder.build() );
		response.close();
	}		
}



// Handler for context /group/
// GET group/<group-id>
// POST group/<group-id>
// DELETE group/<group-id>/<user-id>
class GroupHandler implements HttpHandler {
	private ChatServer server; 
	public GroupHandler ( ChatServer server )
	{
		this.server = server;
	}
	public void handle( HttpExchange exchange) throws IOException {
		String requestMethod = exchange.getRequestMethod();
		
		// the name of the group to be queried
		String groupName = "";
		
		// get the URI of the request
		String uri = exchange.getRequestURI().toString();
				
		// the printstream to send response
		PrintStream response = new PrintStream( exchange.getResponseBody());
				
		// send the responseHeader with the response code
		Headers responseHeaders = exchange.getResponseHeaders();
		responseHeaders.set( "Content-Type", "text/plain");
		
		// case: POST request
		if( requestMethod.equalsIgnoreCase( "POST")) {
			
			// get the groupName from the uri
			groupName = uri.substring(7);
			
			// get the userName from the request body
			BufferedReader body = new BufferedReader( new InputStreamReader( exchange.getRequestBody()));
			String bodyLine = body.readLine();
			String userName = bodyLine.substring(5);
			
			// if the user is not yet added to the system, add him/her
			if ( !server.existsUser(userName) ) 
				server.addUserToSystem(userName,null);
			 
			// if the group does not yet exist, create it
			if ( !server.existsGroup(groupName) )
				server.addGroup( groupName );
			
			// add the user to the group
			server.addUserToGroup(userName,groupName);
		}
		// case: DELETE request
		else if (requestMethod.equalsIgnoreCase( "DELETE"))
		{
			// get the groupName and userName from the uri
			uri = uri.substring(7); // cut off the leading "group/"
			groupName = uri.split("/")[0];
			String userName = uri.split("/")[1];
			
			// case: no such group
			if ( !server.existsGroup(groupName))
			{	
				exchange.sendResponseHeaders( 400, 0);
				response.close();
				return;
			}
			// case: no such user in group
			else if (!server.isMemberOfGroup(userName, groupName))
			{
				exchange.sendResponseHeaders( 401, 0);
				response.close();
				return;
			}
			// valid case
			else
			{
				// remove the user from the group
				server.removeUserFromGroup(userName, groupName);
			}
		}
		// case: GET request
		else
		{
			// get the groupName from the uri
			groupName = uri.substring(7);
						
			// in case of GET requests, if the group doesn't exist, return code 400 and end handling
			if ( !server.existsGroup(groupName))
			{	
				exchange.sendResponseHeaders( 400, 0);
				response.close();
				return;
			}
		}
		
		/**
		 * if not end prematurely due to errors, carry on with response code 200

		 * starting from here downward, all code applies for GET, POST, and DELETE requests of context /group/
		 * they all need to return the membership of the group after all
		 **/
		exchange.sendResponseHeaders( 200, 0);
		
		// use JsonObjectBuilder to build a Json-encoded response
		JsonObjectBuilder jsObjectBuilder = Json.createObjectBuilder();
		
		// get the user list of the group from ChatServer, use it to create Json response
		ArrayList<String> users = server.getMembership(groupName);
		if (!users.isEmpty())
		{
			JsonArrayBuilder jsArrayBuilder = Json.createArrayBuilder();
			for ( String u : users )
			{
				jsArrayBuilder.add(u);
			}
			jsObjectBuilder.add("users",jsArrayBuilder.build());
		}
		
		// send the response
		response.println( jsObjectBuilder.build() );
		response.close();
	}		
}



// Handler for context /messages/
// Handle GET /messages/<group_id|user_id>
class MessagesHandler implements HttpHandler {
	private ChatServer server; 
	public MessagesHandler ( ChatServer server )
	{
		this.server = server;
	}
	public void handle( HttpExchange exchange) throws IOException {

		// send the responseHeader with the response code
		Headers responseHeaders = exchange.getResponseHeaders();
		responseHeaders.set( "Content-Type", "text/plain");
		exchange.sendResponseHeaders( 200, 0);

		// the printstream to send response
		PrintStream response = new PrintStream( exchange.getResponseBody());
		
		// use JsonObjectBuilder to build a Json-encoded response
		JsonObjectBuilder jsObjectBuilder = Json.createObjectBuilder();
		
		// get the target of the request from the URI
		String target = exchange.getRequestURI().toString().substring(10); // cut off /messages/
		
		List<String> messages;
		
		// get the message list from ChatServer, use it to create Json response
		// case: request history for a user
		if (target.startsWith("@"))
		{
			messages = server.getUserHistory( target.substring(1)); // cut off "@"
		}
		
		// case: request history for a group
		else
		{
			messages = server.getGroupHistory( target);
		}
		
		if (!messages.isEmpty())
		{
			JsonArrayBuilder jsArrayBuilder = Json.createArrayBuilder();
			for ( String m : messages )
			{
				jsArrayBuilder.add(m);
			}
			jsObjectBuilder.add("messages",jsArrayBuilder.build());
		}
		
		// send the response
		response.println( jsObjectBuilder.build() );
		response.close();
	}		
}


//Handler for context /message
// POST /message
class MessageHandler implements HttpHandler {
	private ChatServer server; 
	public MessageHandler ( ChatServer server )
	{
		this.server = server;
	}
	public void handle( HttpExchange exchange) throws IOException {
				
		// send the responseHeader with the response code
		Headers responseHeaders = exchange.getResponseHeaders();
		responseHeaders.set( "Content-Type", "text/plain");
		
		// the printstream to send response
		PrintStream response = new PrintStream( exchange.getResponseBody());
		
		// The msgp message
		String message = "";
		
		// get the message from the request body
		BufferedReader body = new BufferedReader( new InputStreamReader( exchange.getRequestBody()));
		String bodyLine;
		while ( (bodyLine = body.readLine()) != null)
		{
			// if recipient doesn't exist, response 402
			if (( bodyLine.startsWith("to: @") && !server.existsUser(bodyLine.substring(5))) 
			 || ( bodyLine.startsWith("to: #") && !server.existsGroup(bodyLine.substring(5))))
			{
				exchange.sendResponseHeaders( 402, 0);
				response.close();
				return;
			}
			
			// reconstruct the msgp message
			message += bodyLine+"\n";
		}
		
		// in case the message file is mistakenly encoded to have too many ending new line
		if( message.endsWith("\n\n\n"))
		{
			message = message.substring(0, message.length()-1);
		}
		
		// if no error happenned, proceed to response 200
		exchange.sendResponseHeaders( 200, 0);
		
		// construct a TextMsgpServer object to use it for sending msgp message
		TextMsgpServer tms = new TextMsgpServer(server);
		// call method send from TextMsgpServer to send the message
		tms.send(message);

		
		// send the response with empty Json object upon success
		response.println( Json.createObjectBuilder().build() );
		
		response.close();
	}		
}
