/**
 * interface for ChatServer which handles the maintenaince of the chat system's data,
 * such as users, OutputStreams and Groups
 */

package csci4311.chatExtra;

import java.util.*;
import java.io.DataOutputStream;

public interface MessageServer
{
	//** query methods **\\

	// getter for groupTable
	public Hashtable<String,Group> getGroupTable();

	// getter for userTable
	public Hashtable<String, DataOutputStream> getUserTable();

	// gets the list of users in a group
	public ArrayList<String> getMembership( String groupName );

	// gets the list of DataOutputStream for a given list of users
	public ArrayList<DataOutputStream> getOutStreamList( ArrayList<String> userList );

	// checks whether a group exists
	public boolean existsGroup( String groupName );

	// checks whether a user is already in System
	public boolean existsUser( String userName );

	// checks whether a user is a member of a group
	public boolean isMember( String userName );


	//** command methods **\\

	// adds a group to the system
	public void addGroup( String groupName);

	// adds a userName and DataOutputStream Mapping to the userTable
	public void addUserToSystem( String userName, DataOutputStream outStream );

	// adds a user to a group
	public void addUserToGroup ( String userName, String groupName );

	/**
	 * removes a user from a group
	 * also removes the group if its last member is removed
	 **/
	public void removeUserFromGroup ( String userName, String groupName );

	/**
	 * adds a message to a group's history
	 * 
	 * @param message 	the message to be added
	 * @param groupName the name of the group to add history
	 **/
	public void addHistory ( String message, String groupName );
}
