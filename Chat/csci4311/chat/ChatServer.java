/**
 * the main process of the server side of the program
 * maintains the information needed for the operation of the server:
 * 		a table of groups in the system
 * 		a table of users in the system
 * responsible for the sending of a message to recipients
 */

package csci4311.chat;

import java.util.*;
import java.io.*;
import java.net.*;

public class ChatServer {

	//** instance variables **\\
	
	// the Hashtable of active groups and their corresponding Group object
	private Hashtable<String,Group> groupTable;
	
	// the Hashtable of active users and their corresponding DataOutputStream (for the sending of messages)
	private Hashtable<String, DataOutputStream> userTable;

	// the array list that contains the recipients of a message
	ArrayList<String> recipientUsers ;

	// the array list that contains the groups to receive a message
	ArrayList<String> recipientGroups ;

	// the array list of DataOutputStreams through which to send a message 
	ArrayList<DataOutputStream> outStreamList;

	
	/**
	 * constructor
	 * 
	 * @param welcomeSocket the socket to listen to connection request 
	 **/ 
	public ChatServer(ServerSocket welcomeSocket) throws Exception
	{
		Socket connectionSocket = null;
		recipientUsers = new ArrayList<String>();
		recipientGroups = new ArrayList<String>();
		outStreamList = new ArrayList<DataOutputStream>();
		groupTable = new Hashtable<String,Group>();
		userTable = new Hashtable<String, DataOutputStream>();

		// While loop to handle arbitrary sequence of clients making requests
    	while(true) 
    	{
    		// accept a new client connection
			connectionSocket = welcomeSocket.accept();
			System.out.println( "Client Made Connection");

			// create a new service thread to handle communication with the newly connected client
			ServiceThread thread = new ServiceThread( this, connectionSocket);
			thread.start();
    	} // end while; loop back to accept a new client connection
	}



	/**
	 * main method
	 **/ 
	public static void main(String argv[]) throws Exception 
	{
		// read the port number from command-line arguments
		// if not provided, default to 4311
		int port = argv.length > 0 ? Integer.parseInt(argv[0]) : 4311 ;
		 
		// Create the welcoming socket to listen to connection request
   		ServerSocket welcomeSocket = new ServerSocket( port, 0);
    	System.out.println("Server Ready for Connection");

		// creates a new ChatServer object to handle the rest
		ChatServer server = new ChatServer(welcomeSocket);
		
	} // end main



	//** query methods **\\

	// getter for groupTable
	public Hashtable<String,Group> getGroupTable()
	{
		return groupTable;
	}

	// getter for userTable
	public Hashtable<String, DataOutputStream> getUserTable()
	{
		return userTable;
	}

	// checks whether a group exists
	public boolean existsGroup( String groupName )
	{
		return groupTable.containsKey( groupName );
	}

	// checks whether a user is already in System
	public boolean existsUser( String userName )
	{
		return userTable.containsKey( userName );
	}

	// checks whether a user is a member of a group
	public boolean isMember( String userName )
	{
		for ( Group g : groupTable.values() )
		{
			if ( g.contains( userName ) )
				return true;
		}
		return false;
	}



	//** command methods **\\

	// adds a group to the system
	public void addGroup( String groupName)
	{
		groupTable.put(groupName, new Group(groupName) );
		System.out.println("A new group is created: "+groupName);
	}

	// adds a userName and DataOutputStream Mapping to the userTable
	public void addUserToSystem( String userName, DataOutputStream outStream )
	{
		userTable.put( userName, outStream );
	}
	
	// adds a user to a group
	public void addUserToGroup ( String userName, String groupName )
	{
		Group temp = groupTable.get( groupName );
		temp.addMember( userName );
		System.out.println("User "+userName+" is added to group "+groupName);
	} 

	// removes a user from a group
	public void removeUserFromGroup ( String userName, String groupName )
	{
		Group temp = groupTable.get( groupName );
		temp.removeMember( userName );
		System.out.println("User "+userName+" is removed from the group "+groupName);
		
		/**
		 * checks if the group is empty after the user is removed
		 * there is no point to keep an empty group
		 * if empty, the group will be removed from the system
		 **/
		if ( temp.getSize() == 0 )
		{
			groupTable.remove(groupName);
			System.out.println("The last user has left. Group "+groupName+" is removed from the system");
			temp = null;
		}
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
		 **/
		for ( String u: recipientUsers )
		{
			// if the user does not exist, return Error
			if ( !this.existsUser( u ) )
				return 400;
		}
	
		/**
		 * loop for: 	checking existence of the recipient groups
		 *
		 * if exists:   add message to a group's history
		 *				add all users in the group to the recipient user list 							
		 **/
		for ( String g: recipientGroups )
		{
			// if the group does not exist, return Error
			if ( !this.existsGroup( g ) )
				return 400;
				
			// if it does...
			else
			{	
				// add the message to the group's history
				groupTable.get(g).getHistory().add( message );

				// add all users in the group to the recipient user list
				for ( String us : groupTable.get(g).getMembers() )
				{
					// avoid duplicates
					if ( !recipientUsers.contains( us ) )
					{
						recipientUsers.add( us );
					}
				}
			}
		}

		// get DataOutputStream of all recipient users
		for ( String rus: recipientUsers )
		{
			outStreamList.add( userTable.get( rus ) );
		}

		// send the message to all recipient users
		for ( DataOutputStream d: outStreamList )
		{
			try { d.writeUTF( message ); }
			catch ( IOException e) { e.printStackTrace(); } 
		}

		// return success code after the sending of message
		return 200;
	}

} // end class ChatServer
