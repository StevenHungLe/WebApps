/**
 * the main process of the server side of the program
 * implements: MessageServer
 * maintains the information needed for the operation of the server:
 * 		a table of groups in the system
 * 		a table of users in the system and their output streams
 */

package csci4311.chatExtra;

import java.util.*;
import java.io.*;
import java.net.*;

public class ChatServer implements MessageServer {

	//** instance variables **\\
	
	// the Hashtable of active groups and their corresponding Group object
	private Hashtable<String,Group> groupTable;
	
	// the Hashtable of active users and their corresponding DataOutputStream (for the sending of messages)
	private Hashtable<String, DataOutputStream> userTable;

	
	/**
	 * constructor
	 * 
	 * @param welcomeSocket the socket to listen to connection request 
	 **/ 
	public ChatServer(ServerSocket welcomeSocket) throws Exception
	{
		Socket connectionSocket = null;
		groupTable = new Hashtable<String,Group>();
		userTable = new Hashtable<String, DataOutputStream>();

		// While loop to handle arbitrary sequence of clients making requests
    	while(true) 
    	{
    		// accept a new client connection
			connectionSocket = welcomeSocket.accept();
			System.out.println( "Client Made Connection");

			// create a new TextMsgpServer thread to handle communication with the newly connected client
			TextMsgpServer thread = new TextMsgpServer( this, connectionSocket);
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

	
	// gets the list of users in a group
	public ArrayList<String> getMembership( String groupName )
	{
		return groupTable.get( groupName ).getMembers();
	}

	// gets the list of DataOutputStream for a given list of users
	public ArrayList<DataOutputStream> getOutStreamList( ArrayList<String> userList )
	{
		ArrayList<DataOutputStream> outStreamList = new ArrayList<DataOutputStream>();
		for ( String u: userList )
		{
			outStreamList.add( userTable.get( u ) );
		}
		return outStreamList;
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

	/**
	 * removes a user from a group
	 * also removes the group if its last member is removed
	 **/
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
	 * adds a message to a group's history
	 * 
	 * @param message 	the message to be added
	 * @param groupName the name of the group to add history
	 **/
	public void addHistory ( String message, String groupName )
	{
		groupTable.get( groupName ).getHistory().add( message );
	}

} // end class ChatServer
