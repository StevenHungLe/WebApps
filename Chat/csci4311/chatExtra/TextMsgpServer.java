/**
 * implements: MsgpServer
 * responsible for receiving and processing requests, producing and sending responses in msgp protocol
 *
 * extends: Thread
 * represents a thread which deals exclusively with a separate client connection's requests
 */

package csci4311.chatExtra;

import java.util.*;
import java.io.*;
import java.net.*;

public class TextMsgpServer extends Thread implements MsgpServer{

	//** instance variables **\\

	// the server object that created this thread
	private ChatServer server;

	// the connection socket that the thread is to communicate with
	private Socket connectionSocket;

	// the input and output streams of the connection socket 
	private DataInputStream inStream;
	private DataOutputStream outStream;

	// the request received from client
	private String request;

	// the reply generated to response to client
	private String reply;

	// the array list that contains the recipients of a message
	ArrayList<String> recipientUsers ;

	// the array list that contains the groups to receive a message
	ArrayList<String> recipientGroups ;

	// the array list of DataOutputStreams through which to send a message 
	ArrayList<DataOutputStream> outStreamList;


	/**
	 * constructor
	 * 
	 * @param server 			The server object that created this thread
	 * @param connectionSocket	The connection socket for this thread to communicate with
	 **/ 
	public TextMsgpServer( ChatServer server, Socket connectionSocket) 
	{
		this.server = server;
		this.connectionSocket = connectionSocket;
		request = "";
		reply = "";
		recipientUsers = new ArrayList<String>();
		recipientGroups = new ArrayList<String>();
		outStreamList = new ArrayList<DataOutputStream>();

		// create input and output stream attached to connection socket
		try
		{
			inStream = new DataInputStream( connectionSocket.getInputStream());
			outStream = new DataOutputStream( connectionSocket.getOutputStream());
		}
		catch( IOException e )
		{
			e.printStackTrace();
		}
	}


	// method called when the thread starts
	public void run() 
	{
		// try-catch block for reading client request from DataInputStream
		try 
		{
			while( true ) 
			{
				// read the client request
				request = inStream.readUTF();
				System.out.println("\nRequest: " + request);

				/**
				 * if-else chain to process different requests
				 * calls corresponding method to handle each request
				 **/

				// case: NOT send message request
				if ( !request.startsWith("msgp send") )
				{
					// split the request into parts
					String[] requestParts = request.split(" ");
				
					// case: msgp join <user> <group>
					if ( requestParts[1].equals("join") )
						reply = this.join( requestParts[2], requestParts[3] );

					// case: msgp leave <user> <group>
					else if ( requestParts[1].equals("leave") )
						reply = this.leave( requestParts[2], requestParts[3] );

					// case: msgp groups
					else if ( requestParts[1].equals("groups") )
						reply = this.groups();

					// case: msgp users <group>
					else if ( requestParts[1].equals("users") )
						reply = this.users( requestParts[2] );

					// case: msgp history <group>
					else if ( requestParts[1].equals("history") )
						reply = this.history( requestParts[2] );
				}
				
				// case: send message request
				else
				{
					reply = this.send(request);
				}

				// print out the generated reply to confirm proper response protocol
				System.out.println("Response: "+reply);

				// send the reply to client
				outStream.writeUTF(reply);
				reply = "";
			} 
		}
		catch( IOException ex)
		{
			//ex.printStackTrace();
			
			// instead of printing exception message, simply announce that the connection is disconnected
			System.out.println(connectionSocket+" disconnected");
		}
		
	}


	/** 
	 * Method to handle join request
	 * add a user to a group
	 * if the group does not exist, creates the group
	 * add the user and his/her DataOutputStream mapping if not yet added
	 *
	 * @param	user 	the name of the user to join in a group
	 * @param 	group	the name of the group for the user to join
	 * @return 			the generated reply which includes Response code and message
	 **/
	public String join(String user, String group)
	{
		
		// if the user's DataOutputStream is not added to the system, add him/her
		if ( !server.existsUser(user) ) 
			server.addUserToSystem(user,outStream);

		// if the user is not a member of any group, add him/her to the group
		if ( !server.isMember(user) ) 
		{
			// if the group does not yet exist, create it
			if ( !server.existsGroup(group) )
				server.addGroup( group );
			
			server.addUserToGroup(user,group);

			return "msgp 200 OK";
		}
		// if the user is already a member of a group, return 201
		else
		{
			return "msgp 201 No result";
		}
	}


	/** 
	 * Method to handle leave request
	 * remove a user from a group
	 * 
	 * @param user	the name of the user that is leaving
	 * @param group the name of the group to leave from
	 * @return 		the generated reply which includes Response code and message
	 **/
	public String leave(String user, String group)
	{
		// if the group doesn't exist, return 400
		if ( !server.existsGroup(group) )
			return "msgp 400 Error";
			
		// if the user is not in the group, return 201
		else if ( !server.getGroupTable().get(group).contains(user) )
			return "msgp 201 No result";
			
		// valid case, proceeds to remove the user from the group
		else
		{
			server.removeUserFromGroup(user, group);
			return "msgp 200 OK";
		}
	}


	/** 
	 * Method to handle groups request
	 * 
	 * @return the generated reply, which contains the list of groups
	 **/
	public String groups()
	{
		// if no groups, no results
		if ( server.getGroupTable().isEmpty() )
			return "msgp 201 No result";

		// generate the reply
		else
		{	
			String response = "msgp 200 OK\n";
			// loop through the group objects and append their names to the reply 
			for ( Group g : server.getGroupTable().values())
			{
				response += g.getName()+"\n"; 
			}

			return response;
		}	
	}



	/** 
	 * Method to handle users request
	 *
	 * @param group  	the name of the group to request membership for 
	 * @return 			the generated reply, which contains the membership of the requested group
	 **/
	public String users(String group)
	{
		// if the group does not exist, return Error
		if ( !server.getGroupTable().containsKey(group) )
			return "msgp 400 Error";
		// if the group does not have any member, return No result
		if ( server.getGroupTable().get(group).getMembers().isEmpty() )
			return "msgp 201 No result";
		// valid case, return the membership of the group
		else
		{	
			String response = "msgp 200 OK\n";
			// loop through the membership list and append user names to the reply
			for ( String u : server.getGroupTable().get(group).getMembers() )
			{
				response += u+"\n"; 
			}

			return response;
		}
	
	}



	/** 
	 * Method to handle history request
	 *
	 * @param group 	the name of the group to request history for
	 * @return			the generated reply, which includes the history of the requested group
	 **/
	public String history(String group)
	{
		// if the group does not exist, return Error
		if ( !server.existsGroup( group ) )
	 	{
	 		return "msgp 400 Error";
	 	}
	 	// if the group's history is empty, return No result
	 	else if ( server.getGroupTable().get(group).getHistory().isEmpty() )
	 	{
	 		return "msgp 201 No result";
	 	}
	 	// valid case, return the reply with the requested history
	 	else
	 	{
	 		String response = "msgp 200 OK\n";
	 		// loop through the history to append messages to the reply
	 		for ( String h: server.getGroupTable().get(group).getHistory() )
	 		{
	 			response += h;
	 		}

	 		return response;
	 	}
	 	
	}


	/** 
	 * Method to handle send request
	 *
	 * @param request 	the request received from the client
	 * @return 			the generated reply which includes Response code and message
	 **/
	public String send(String request)
	{
		// chain of msgp-specific procedures to extract the list of recipients from the request:
		/** 
		 * msgp send
		 * from: <user>
		 * to: @<user>|#<group>
		 * to: ...
		 * <cr><lf>
		 * <single line message>
		 * <cr><lf>
		 **/

		// split the request in two by delimitter "\n\n" and save the first part in 'temp',
		// this gets rid of the message, which comes after a white space 
		String temp = request.split("\n\n",2)[0];

		// obtain a substring that starts with "to:",
		// this get rid of the everything preceding and exposes the recipient list
		String subString = temp.substring(temp.indexOf("to:"));

		// split the substring by delimiter "\n"
		// this gives an array of every recipient line: 'to: <recipient>'
		String[] subStrings = subString.split("\n");

		// creates an array to hold the recipients
		String[] recipients = new String[subStrings.length];

		// loop through the substrings array
		// cut off the "to: " part
		// this leaves the recipient, save it to the recipient array
		for( int i = 0; i < subStrings.length ; i++)
		{
			recipients[i] = subStrings[i].substring(4);
		}

		// call method sendMessage to send the decoded message to its recipients
		int replyCode = this.sendMessage( recipients , request );

		// checks the reply code and return reply messages accordingly
		if ( replyCode == 400)
			return "msgp 400 Error";
		else
			return "msgp 200 OK";
	}



	/**
	 * send a message to a specified list of recipient users and groups
	 * messages sent to a group will be added to its history
	 *
	 * @param recipients 	The array of recipients of the message
	 * @param message 		The message to be sent
	 * @return 				reply code : {200,201,400}
	 **/
	public int sendMessage(String[] recipients, String message)
	{
		// clear the recipient lists that persist from previous request
		recipientUsers.clear();
		recipientGroups.clear();
		outStreamList.clear();

		// analyze the recipient list to separate users from groups
		for( String r: recipients )
		{
			if ( r.startsWith("@"))
				recipientUsers.add( r.substring(1));
			else //if ( r.startsWith("#")
				recipientGroups.add( r.substring(1));
		}

		/**
		 * loop for checking existence of the recipient users
		 * calls server's method existsUser to do the checking
		 **/
		for ( String u: recipientUsers )
		{
			// if the user does not exist, return Error
			if ( !server.existsUser( u ) )
				return 400;
		}
	
		/**
		 * loop for: 	checking existence of the recipient groups
		 * calls server's method existsGroup to do the checking
		 *
		 * if exists:   add message to a group's history
		 *				add all users in the group to the recipient user list 							
		 **/
		for ( String g: recipientGroups )
		{
			// if the group does not exist, return Error
			if ( !server.existsGroup( g ) )
				return 400;
				
			// if it does...
			else
			{	
				// calling server's method addHistory to add the message to the group's history
				server.addHistory( message, g );

				// add all users in the group to the recipient user list
				// call server's method getMembership to get the list of users in the group
				for ( String us : server.getMembership( g ) )
				{
					// avoid duplicates
					if ( !recipientUsers.contains( us ) )
					{
						recipientUsers.add( us );
					}
				}
			}
		}

		// calling server's method getOutStreamList, get DataOutputStreams of all recipient users
		outStreamList = server.getOutStreamList( recipientUsers );

		// send the message to all recipient users
		for ( DataOutputStream d: outStreamList )
		{
			try { d.writeUTF( message ); }
			catch ( IOException e) { e.printStackTrace(); } 
		}

		// return success code after the sending of message
		return 200;
	}
}
