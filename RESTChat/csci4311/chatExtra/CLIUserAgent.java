/**
 * inplements: UserAgent
 * the main class of the client side of the program
 * handles interactions with the user (input, output) via command-line interface
 *
 * contains: TextMsgpClient
 * maintains an object of TextMsgpClient to delegate the handling of msgp protocol
 */

package csci4311.chatExtra;

import java.util.*;
import java.io.*;
import java.net.*;

public class CLIUserAgent implements UserAgent
{
	//** instance variables **\\
	// the list of recipients to send messages to 
	private ArrayList<String> recipients;
	// the message to send
	private String messageOut;
	// the user name
	private String user;
	// the port number
	private int port;
	// the msgclient to take care of message protocols
	private TextMsgpClient msgp;
	
	/**
	 * constructor
	 * 
	 * @param user 			The name of the user
	 * @param server		The host name of the server
	 * @param port 			The 
	 **/ 
	public CLIUserAgent (String user, String server, int port) throws Exception
	{
		this.user = user;
		messageOut= "";
		recipients = new ArrayList<String>();
		
		// creates a socket connecting with server at the specified port
		Socket clientSocket = new Socket(server,port);
		System.out.println("Established a connection between user "+user+" and server "+server+".");

		// initializes a new MsgpClient instance to handle msgp protocol
		msgp = new TextMsgpClient( this, clientSocket );  
		
		// automatic join request so that the server has record of user's name...
		// right after he/she logged in
		msgp.join(user,"ReservedGroup");
		// after the deed is done, automatically leave the group,
		// this leaves the user at starting point like nothing happened
		msgp.leave(user,"ReservedGroup");

		// initializes a Scanner to read user's terminal input
		Scanner input = new Scanner( System.in );
		
		// The command read from user's input
		String command = "";
		
		// The array of splitted parts of the command
		String[] commandParts = null; 

		// The reply code received as a response of requests
		int replyCode = 0;

		
		// The infinite loop to read and process user's inputs and display corresponding results
		while(true)
		{
			// prints the specified prompt
			System.out.print("\n@"+user+" >>");
			
			// read user's input
			command = input.nextLine();

			/**
			 * if-else chains to process and produce results for commands
			 **/
			
			// case: NOT a send command
			if ( !command.startsWith("send") )
			{
				// split the command into parts
				commandParts = command.split(" ");
			
				// case: join command: join <group>
				if ( commandParts[0].equals("join") )
				{
					/**
					 * call method join in TextMsgpClient to produce msgp-protocol request
					 * @pass in: the user name
					 * @pass in: the group to join
					 * @receive: the reply code
					 **/
					replyCode = msgp.join( user, commandParts[1] );

					// user already has a group
					if ( replyCode == 201 )
						System.out.println(user+" is already a member of a group");
					// valid case
					else
					{
						/**
						 * call method users in TextMsgpClient to query the group's membership
						 * @pass in: the group to request membership for
						 * @receive: the ArrayList of users in the group
						 **/
						 ArrayList<String> users = (ArrayList<String>)msgp.users(commandParts[1]);
						 
						 int userCount = users == null ? 0 : users.size(); 

						// display the success message: the group and its current number of members	
						System.out.println("joined #"+commandParts[1]+" with "+userCount+" current member");					 
					}
						

				}

				// case: leave command: leave <group>
				else if ( commandParts[0].equals("leave") )
				{
					/**
					 * call method leave in TextMsgpClient to produce msgp-protocol request
					 * @pass in: the user name
					 * @pass in: the group to leave
					 * @receive: the reply code
					 **/
					replyCode = msgp.leave( user, commandParts[1] );
					
					switch (replyCode)
					{
						case 200:
						System.out.println(user+" successfully left group "+commandParts[1]);
						break;
						case 201:
						System.out.println(user+" is not a member of the group");
						break;
						case 400:
						System.out.println("group "+commandParts[1]+" does not even exist!!!");
						break;
						default:
						System.out.println("I don't know what is going on ?!?");
					}
				}

				// case: groups command: groups
				else if ( commandParts[0].equals("groups") )
				{
					/**
					 * call method groups in TextMsgpClient to produce msgp-protocol request
					 * @receive: the ArrayList of active groups
					 **/
					ArrayList<String> groups = (ArrayList<String>)msgp.groups();

					// declare an arraylist to save the membership of a group
					// used to display number of members
					ArrayList<String> users;
					
					// if the returned ArrayList is not null,
					// loops through it to print the list of active groups
					if (groups != null)
					{
						for ( String g: groups)
						{
							/**
							 * call method users in TextMsgpClient to query the group's membership
							 * @pass in: the group to request membership for
							 * @receive: the ArrayList of users in the group
							 **/
							users = (ArrayList<String>)msgp.users( g );
							
							int userCount = users == null ? 0 : users.size(); 

							// display the group's name and its number of members
							System.out.println("#"+ g +" has "+userCount+" members");
						}
					}
				}

				// case: users command: users <group>
				else if ( commandParts[0].equals("users") )
				{
					/**
					 * call method users in TextMsgpClient to produce msgp-protocol request
					 * @pass in: the group to request membership for
					 * @receive: the ArrayList of users in the group
					 **/
					ArrayList<String> users = (ArrayList<String>)msgp.users(commandParts[1]);

					// if the returned ArrayList is not null,
					// loops through it to print the membership of the group
					if (users != null)
					{
						for ( String u: users)
						{
							System.out.println("@"+u);
						}
					}
				}

				// case: history command
				else if ( commandParts[0].equals("history") )
				{
					/**
					 * call method history in TextMsgpClient to produce msgp-protocol request
					 * @pass in: the group to request history for
					 * @receive: the ArrayList of messages in the history
					 **/
					ArrayList<MsgpMessage> history = (ArrayList<MsgpMessage>)msgp.history( commandParts[1] );

					// call helper method printHistory to display the history
					this.printHistory( history );
				}
					
			}

			// case: send command	
			else
			{
				// calls helper method to extract the recipient list and the message from the send command
				extractSendCommand( command );

				/**
				 * call method send in TextMsgpClient to produce msgp-protocol request
				 * @pass in: the message to be sent, encoded in a MsgpMessage object
				 * @receive: the reply code
				 **/
				replyCode = msgp.send(new MsgpMessage(user,recipients,messageOut));
				if (replyCode == 400)
				{
					System.out.println("one or more recipients do not exist!!!");
				}
			}
			
		}// end of command processing while loop
	}


	// the main method
	public static void main( String args[]) throws Exception
	{
		// checks the number of terminal arguments
		if (args.length < 1 || args.length > 3)
		{
			System.out.println("Wrong number of arguments!!!");
			System.exit(0);
		}
		
		// save terminal arguments into corresponding variables
		String user = args[0];
		String server = args.length > 1 ? args[1] : "localhost";
		int port = args.length == 3 ? Integer.parseInt(args[2]) : 4311 ;
		
		// initializes a new CLIUserAgent object
		CLIUserAgent userAgent = new CLIUserAgent(user, server, port);
	}// end of main method




	// helper method to split the send command into the recipient list and the message
	private void extractSendCommand(String command)
	{
		// clear the recipient list that persists from previous call
		recipients.clear();
		
		// the string array to hold the splitted substrings
		// initially split the command in two to discard the String "send" 
		String subStrings[] = command.split(" ",2);
		
		do 
		{
			// split the second subString in two
			subStrings = subStrings[1].split(" ",2);

			// the first subString should be a recipient, add it to the list
			recipients.add( subStrings[0] );
		}
		// continue until there is more recipient to extract
		while( subStrings[1].startsWith("@") || subStrings[1].startsWith("#") );

		// the subString that doesn't start with '@' or '#' is the message
		messageOut = subStrings[1];
		
	}// end of method extractSendCommand


	/**
	 * delivers the incoming message to the user
	 *
	 * @param the message to be delivered, encoded by a MsgpMessage object
	 **/
	public void deliver(MsgpMessage message)
	{
		System.out.println("["+message.getFrom()+"] "+message.getMessage() );

		// if not a message received from self, reprint the prompt
		if ( !message.getFrom().equals( user ) )
			System.out.print("\n@"+user+" >>");
	}

	/**
	 * displays the chat history of a group
	 *
	 * @param history the ArrayList of messages in the history
	 **/
	public void printHistory(ArrayList<MsgpMessage> history)
	{
		for (MsgpMessage message : history )
		{
			System.out.println("["+message.getFrom()+"] "+message.getMessage() );
		} 	
	}

}
