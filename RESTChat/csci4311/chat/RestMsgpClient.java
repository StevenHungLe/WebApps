/**
 * The REST version of TextMsgpClient
 * handles the encoding, decoding, sending and receipt of REST-protocol messages
 */
package csci4311.chatExtra;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;


public class RestMsgpClient{
	
	// Instance variables \\
 	// the DataOutputStream through which to send requests and messages
 	// the request to be send
 	private String request;
	// the number of messages received
	private int numRcvdMsg;
 	
 	private String userName;
 	
 	private HttpClient  client;
 	
 	private HttpGet httpRequest;
 	
 	private HttpResponse response;
 	
 	private BufferedReader reader;
 	
 	private JsonReader jsReader;
 	
 	private JsonObject jsObject;
 	
	private JsonArray jsArray;
	
	private PoolingHttpClientConnectionManager connManager ;
 	
 	// constructor
 	public RestMsgpClient(String userName) throws Exception
 	{
 		this.userName = userName;
 		this.request = "";
 		numRcvdMsg = 0;
 		connManager = new PoolingHttpClientConnectionManager();
 		
 		httpRequest = null;
 		response = null;
 		reader = null;
    	jsReader = Json.createReader(reader);
 	}

 	/**
     * Encodes a user join request.
     *
     * @param user  user name
     * @param group group name
     * @return      the current users of the group
     */
    public ArrayList<String> join(String user, String group) throws Exception
    {
    	ArrayList<String> users = new ArrayList<String>();
    	
    	this.connManager = new PoolingHttpClientConnectionManager();
    	client = HttpClientBuilder.create().setConnectionManager(connManager).build();
    	HttpPost post = new HttpPost("http://localhost:8311/group/"+group);
    	StringEntity input = new StringEntity("user="+user);
    	post.setEntity(input);

    	response = client.execute(post);
    	reader = new BufferedReader (new InputStreamReader(response.getEntity().getContent()));
    	jsReader = Json.createReader(reader);
    	
    	jsObject = jsReader.readObject();
    	if (jsObject.toString().equals("{}"))
    		return null;
    	jsArray = jsObject.getJsonArray("users");
    	for ( int i=0; i<jsArray.size();i++ )
    	{
    		users.add( jsArray.getString(i));
    	}
      	
    	this.connManager.shutdown();
    	//groups = new ArrayList<String> ((String[]) jsReader.readArray().getValuesAs(String));
      	return users;
    }

    /**
     * Encodes a user leave request.
     *
     * @param user  user name
     * @param group group name
     * @return      status code
     */
    public int leave(String user, String group) throws Exception
    {
    	
    	this.connManager = new PoolingHttpClientConnectionManager();
    	client = HttpClientBuilder.create().setConnectionManager(connManager).build();
    	HttpDelete delete = new HttpDelete("http://localhost:8311/group/"+group+"/"+user);

    	response = client.execute(delete);
      	
    	this.connManager.shutdown();
   
      	return response.getStatusLine().getStatusCode();
	}


    /**
     * Requests the list of groups.
     *
     * @return      existing groups; null if none
     */
    public ArrayList<String> groups() throws Exception
    {
    	ArrayList<String> groups = new ArrayList<String>();
    	
    	this.connManager = new PoolingHttpClientConnectionManager();
    	client = HttpClientBuilder.create().setConnectionManager(connManager).build();
    	httpRequest = new HttpGet("http://localhost:8311/groups");
    	response = client.execute(httpRequest);
    	reader = new BufferedReader (new InputStreamReader(response.getEntity().getContent()));
    	jsReader = Json.createReader(reader);
    	
    	jsObject = jsReader.readObject();
    	if (jsObject.toString().equals("{}"))
    		return null;
    	
    	jsArray = jsObject.getJsonArray("groups");
    	for ( int i=0; i<jsArray.size();i++ )
    	{
    		groups.add( jsArray.getString(i));
    	}
      	
    	this.connManager.shutdown();
    	//groups = new ArrayList<String> ((String[]) jsReader.readArray().getValuesAs(String));
      	return groups;

    }

    /**
     * Requests the list of users in a given group 
     * or the list of users in the entire system when groupName is not provided.
     *
     * @param   group   group name / or null to request users of the entire system
     * @return          list of users; null if none
     */
    public ArrayList<String> users(String group) throws Exception
    {
    	ArrayList<String> users = new ArrayList<String>();
    	
    	this.connManager = new PoolingHttpClientConnectionManager();
    	client = HttpClientBuilder.create().setConnectionManager(connManager).build();
    	
    	// get users of the entire system
    	if ( group == null)
    		httpRequest = new HttpGet("http://localhost:8311/users");
    	// get users of a group
    	else
    		httpRequest = new HttpGet("http://localhost:8311/group/"+group);
    	
    	response = client.execute(httpRequest);
    	reader = new BufferedReader (new InputStreamReader(response.getEntity().getContent()));
    	jsReader = Json.createReader(reader);
    	
    	jsObject = jsReader.readObject();
    	if (jsObject.toString().equals("{}"))
    		return null;
    	jsArray = jsObject.getJsonArray("users");
    	for ( int i=0; i<jsArray.size();i++ )
    	{
    		users.add( jsArray.getString(i));
    	}
      	
    	this.connManager.shutdown();
    	//groups = new ArrayList<String> ((String[]) jsReader.readArray().getValuesAs(String));
      	return users;
    }

    /**
     * Requests the history of chat message of a group or a user
     *
     * @param   group   group name/ or username preceded by @
     * @return          list of all messages sent to the group or the user; null of none
     */
    public List<MsgpMessage> history(String target) throws Exception
    {
    	List<MsgpMessage> history = new ArrayList<MsgpMessage>();
    	
    	this.connManager = new PoolingHttpClientConnectionManager();
    	client = HttpClientBuilder.create().setConnectionManager(connManager).build();
    	httpRequest = new HttpGet("http://localhost:8311/messages/"+target);
    	response = client.execute(httpRequest);
    	reader = new BufferedReader (new InputStreamReader(response.getEntity().getContent()));
    	jsReader = Json.createReader(reader);
    	
    	jsObject = jsReader.readObject();
    	if (jsObject.toString().equals("{}"))
    		return null;
    	jsArray = jsObject.getJsonArray("messages");
    	for ( int i=0; i<jsArray.size();i++ )
    	{
    		history.add( this.decodeMessage(jsArray.getString(i)));
    	}
      	
    	this.connManager.shutdown();
      	return history;
    }


    /**
     * Encodes the sending of a message.
     *
     * @param message   message content
     * @return reply code, as per the spec
     */

    public int send(MsgpMessage msg) throws Exception
    {
    	request = "msgp send\nfrom: "+msg.getFrom()+"\n";
    	for ( String t: msg.getTo())
    	{
    		request += ("to: "+ t +"\n");
    	}
    	request += "\n"+msg.getMessage()+"\n\n";
    	
    	
    	this.connManager = new PoolingHttpClientConnectionManager();
    	client = HttpClientBuilder.create().setConnectionManager(connManager).build();
    	HttpPost post = new HttpPost("http://localhost:8311/message");
    	StringEntity input = new StringEntity(request);
    	post.setEntity(input);
    	
    	response = client.execute(post);

      	
    	this.connManager.shutdown();
    	//groups = new ArrayList<String> ((String[]) jsReader.readArray().getValuesAs(String));
      	return response.getStatusLine().getStatusCode();
    }

	/**
	 * helper method
	 * decodes an incoming message
	 * 
	 * @param msd 	the message to be decoded
	 * @return 		the MsgpMessage object that represents the decoded message 
	 **/
    private MsgpMessage decodeMessage(String msg)
    {
        // chain of msgp-specific procecures to decode a message into sender and message:

        // get rid of the header, get straight to the sender line
        msg = msg.substring( msg.indexOf("from") );

        // split the above string in two by delimiter "\n"
        // the first part is the sender line, save this
		String sender = msg.split("\n",2)[0];

		// split the sender line by delimiter " "
		// the second part is the sender, save this
		sender = sender.split(" ")[1];

		// split the message in two by delimiter "\n\n"
		// the second part is the message itself, save this
		String message = msg.split("\n\n",2)[1];

		// the message part may still contain the trailing new-line character
		// this is not needed, get rid of it
		if ( message.contains("\n") )
			message = message.substring(0,message.indexOf("\n"));

		// return the message in a MsgpMessage object, recipients are not needed here
		return new MsgpMessage(sender,null,message);
    }


    /** 
     * the message-polling method called by RESTUserAgent 
     * every two seconds to get new message on behalf of the user
     **/
    public List<MsgpMessage> getNewMessages()
    {
    	List<MsgpMessage> messages = new ArrayList<MsgpMessage>();
    	
    	try
		{
    		PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();
	    	HttpClient client = HttpClientBuilder.create().setConnectionManager(connManager).build();
	    	HttpGet httpRequest = new HttpGet("http://localhost:8311/messages/@"+userName);
	    	HttpResponse response = client.execute(httpRequest);
	    	BufferedReader reader = new BufferedReader (new InputStreamReader(response.getEntity().getContent()));
	    	jsReader = Json.createReader(reader);
	    	
	    	this.connManager.shutdown();
	    	
	    	jsObject = jsReader.readObject();
	    	
	    	if (jsObject.toString().equals("{}")) {} // no action
	    	else
	    	{
	    		jsArray = jsObject.getJsonArray("messages");
		    	for ( int i=this.numRcvdMsg; i<jsArray.size();i++ )
		    	{
		    		messages.add( this.decodeMessage(jsArray.getString(i)));
		    		numRcvdMsg++;
		    	}
	    	}	
	    	reader.close();
	    	jsReader.close();
		}
		catch ( IOException e )
		{
			e.printStackTrace();
		}	
    	return messages;
    	
    }
	
}
