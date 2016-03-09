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
	
	// the HashMap of active groups and their corresponding Group object
	private HashMap<String,Group> groupTable;
	
	// the HashMap of active users and their corresponding DataOutputStream (for the sending of messages)
	private HashMap<String, DataOutputStream> userTable;
	
	// the HashMap of active users and their corresponding message History
	private HashMap<String, ArrayList<String>> userHistoryTable;

	// the RestServer object to handle Rest requests
	private RestServer restServer;
	
	

	
	/**
	 * constructor
	 * 
	 * @param welcomeSocket the socket to listen to connection request 
	 **/ 
	public ChatServer(ServerSocket welcomeSocket, int restPort) throws Exception
	{
		Socket connectionSocket = null;
		groupTable = new HashMap<String,Group>();
		userTable = new HashMap<String, DataOutputStream>();
		userHistoryTable = new HashMap<String, ArrayList<String>>();

		// the RestServer object to handle Rest requests
		restServer = new RestServer( this, restPort );
		
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
		
		// read the rest port number from command-line arguments
		// if not provided, default to 8311
		int restPort = argv.length > 1 ? Integer.parseInt(argv[1]) : 8311 ;
		 
		// Create the welcoming socket to listen to connection request
   		ServerSocket welcomeSocket = new ServerSocket( port, 0);
    	System.out.println("Server Ready for Connection");

		// creates a new ChatServer object to handle the rest
		new ChatServer(welcomeSocket, restPort);
		
		
	} // end main



	//** query methods **\\

	// getter for groupTable
	public HashMap<String,Group> getGroupTable()
	{
		return groupTable;
	}

	// getter for userTable
	public HashMap<String, DataOutputStream> getUserTable()
	{
		return userTable;
	}
	
	// getter for the list of logged-in users
	public Set getUsers()
	{
		return userTable.keySet();
	}
	
	// getter for the list of logged-in groups
	public Set getGroups()
	{
		return groupTable.keySet();
	}

	
	// gets the list of users in a group
	public ArrayList<String> getMembership( String groupName )
	{
		return groupTable.get( groupName ).getMembers();
	}

	// gets the list of messages sent to a group or a user
	public ArrayList<String> getGroupHistory( String groupName )
	{
		return groupTable.get(groupName).getHistory();
	}
	
	// gets the list of messages sent to a group or a user
	public ArrayList<String> getUserHistory( String userName )
	{
		return userHistoryTable.get(userName);
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
	
	// checks whether a user is a member of a group
	public boolean isMemberOfGroup( String userName, String groupName )
	{	
		return groupTable.get(groupName).contains(userName);
	}



	//** command methods **\\

	// adds a group to the system
	public void addGroup( String groupName)
	{
		groupTable.put(groupName, new Group(groupName) );
		// if groupName is ReservedGroup, then this is the initialization procedure to add the user record to the system
		if ( groupName.equals("ReservedGroup"))
		{
			System.out.println("ReservedGroup is created as a initialization procedure to add the user record to the system");
		}
		else
		{
			System.out.println("A new group is created: "+groupName);
		}
	}

	// adds a userName and DataOutputStream Mapping to the userTable
	// also add the userName and their history mapping to userHistoryTable
	public void addUserToSystem( String userName, DataOutputStream outStream )
	{
		System.out.println("User "+userName+" is online !!!");
		userTable.put( userName, outStream );
		userHistoryTable.put(userName, new ArrayList<String>());
	}
	
	// remove a userName and DataOutputStream Mapping from the userTable
	public void removeUserFromSystem( String userName )
	{
		userTable.remove(userName);
		userHistoryTable.remove(userName);
	}
	
	// add a message to a user's history
	public void addUserHistory( String userName, String message )
	{
		userHistoryTable.get(userName).add( message );
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
	 * 
	 * return: true if the group is empty after removal, otherwise false
	 **/
	public void removeUserFromGroup ( String userName, String groupName )
	{
		Group temp = groupTable.get( groupName );
		temp.removeMember( userName );
		System.out.println("User "+userName+" is removed from the group "+groupName);
		
		// if groupName is ReservedGroup, then this is just the initialization procedure to add the user record to the system
		// remove this group upon completion of the procedure
		if ( groupName.equals("ReservedGroup"))
		{
			groupTable.remove(groupName);
			System.out.println(groupName+" is removed from the system");
		}
		
		/** 
		 * checks if the group is empty after the user is removed
		 * there is no point to keep an empty group
		 * if empty, the group will be removed from the system
		 *
		if ( temp.getSize() == 0 )
		{
			groupTable.remove(groupName);
			System.out.println("The last user has left. Group "+groupName+" is removed from the system");
			temp = null;
		}
		*
		* NOTE: This block of code is commented out to conform to the requirements of PA3
		* which requires the return of membership of a group even though empty
		*/
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
