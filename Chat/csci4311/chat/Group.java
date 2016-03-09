/**
 * Encodes a chat group in the system of groups maintained by the chat server
 */

package csci4311.chat;

import java.util.*;

public class Group
{
	// instance variables \\
	
	// the name of the group
	private String name;
	// the ArrayList that saves the group's members' names
	private ArrayList<String> members;
	// the LinkedList that saves the group's chat history
	private ArrayList<String> history;

	
	// constructor
	public Group(String name)
	{	
		this.name = name; 
		members = new ArrayList<String>();
		history = new ArrayList<String>();
	}


	//** query methods **\\
	
	// getter for group's name
	public String getName()
	{
		return this.name;
	}
	// getter for group's member list
	public ArrayList<String> getMembers()
	{
		return this.members;
	}

	// getter for group's size
	public int getSize()
	{
		return this.members.size();
	}
	
	// getter for group's history
	public ArrayList<String> getHistory()
	{
		return this.history;
	}
	
	// checks whether the Group contains a given user
	public boolean contains( String user )
	{
		return members.contains( user );
	}



	//** command methods **\\

	// add a member to the group
	public void addMember ( String user )
	{
		members.add( user );
	} 

	// remove a member from the group
	public void removeMember ( String user )
	{
		members.remove( user );
	}

	// add a message to the group's history
	public void addHistory( String message )
	{
		history.add( message );
	}

	// clear the history of the group
	public void clearHistory( String message )
	{
		history.clear();
	}
}
