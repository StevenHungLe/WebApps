/**
 * implements: MsgpClient 
 * handles the encoding, decoding, sending and receipt of msgp-protocol messages
 *
 * extends: Thread
 * maintains a separate thread dedicated to listenning to incoming messages from the server 
 */

package csci4311.chatExtra;

import java.util.*;
import java.io.*;
import java.net.*;

public class TextMsgpClient extends Thread implements MsgpClient
 {	
 	// Instance variables \\
 	// the DataOutputStream through which to send requests and messages
 	private DataOutputStream dos;
 	// the DataInputStream through which to receive server replies
 	private DataInputStream dis;
 	// the request to be send
 	private String request;
 	// the server reply
 	private String reply;
 	// a variable to temporarily save the reply
 	private String replyBuffer;
 	// the user agent that created this
 	CLIUserAgent userAgent;
 	
 	// constructor
 	public TextMsgpClient(CLIUserAgent userAgent,Socket clientSocket) throws Exception
 	{
 		this.dos = new DataOutputStream( clientSocket.getOutputStream() ) ;
 		this.dis = new DataInputStream( clientSocket.getInputStream() ) ;
 		this.request = "";
 		this.reply = "";
 		this.userAgent = userAgent;

 		// starts a thread to solely deal with receiving incoming messages
		this.start();
 	}

 	/**
     * Encodes a user join request.
     *
     * @param user  user name
     * @param group group name
     * @return      reply code, as per the spec
     */
    public int join(String user, String group)
    {
    	request = "msgp join "+user+" "+group;
    	try
    	{
    		dos.writeUTF(request);

			this.getReply();
    	}
    	catch (IOException e)
    	{
    		e.printStackTrace();
    	}
    	finally
    	{
    		return Integer.parseInt( reply.split(" ",3)[1] );
    	}
    }

    /**
     * Encodes a user leave request.
     *
     * @param user  user name
     * @param group group name
     * @return      reply code, as per the spec
     */
    public int leave(String user, String group)
    {
    	request = "msgp leave "+user+" "+group;
    	try
    	{
    		dos.writeUTF(request);
		
    		this.getReply();
    	}
    	catch (IOException e)
    	{
    		e.printStackTrace();
    	}
    	finally
    	{
    		return Integer.parseInt( reply.split(" ",3)[1] );
    	}
	}


    /**
     * Requests the list of groups.
     *
     * @return      existing groups; null of none
     */
    public List<String> groups()
    {
    	ArrayList<String> groups = null;
    	request = "msgp groups";
    	try
    	{
    		dos.writeUTF(request);
		
    		this.getReply();
    	}
    	catch (IOException e)
    	{
    		e.printStackTrace();
    	}
    	finally
    	{
    		// if the response is not "No result"
    		if ( !reply.startsWith("msgp 201") )
    		{
    			// split the reply into group names using delimiter "\n" and save them to the array list 
				groups = new ArrayList<String>( Arrays.asList(reply.split("\n")));

				// remove the first string in the list, which is the protocol's response message
				groups.remove(0);
			}
  			return groups;
    	}
    }

    /**
     * Requests the list of users in a given group.
     *
     * @param   group   group name
     * @return          list of existing user in the group; null of none
     */
    public List<String> users(String group)
    {
    	ArrayList<String> users = null;
    	request = "msgp users "+group;
    	try
    	{
    		dos.writeUTF(request);
		
    		this.getReply();
    	}
    	catch (IOException e)
    	{
    		e.printStackTrace();
    	}
    	finally
    	{
			// no results: group empty
    		if ( reply.startsWith("msgp 201") )
    		{
    			System.out.println(reply);
    		}
    		// error: no such group
    		else if ( reply.startsWith("msgp 400") )
    		{
    			System.out.println(reply);
    		}
    		// valid result
    		else
    		{
    			// split the reply into user names using delimiter "\n" and save them to the array list
				users = new ArrayList<String>( Arrays.asList(reply.split("\n")));
				// remove the first string in the list, which is the protocol's response message
				users.remove(0);
			}

    		return users;
    	}
    }

    /**
     * Requests the history of chat message of a group.
     *
     * @param   group   group name
     * @return          list of all messages sent to the group; null of none
     */
    public List<MsgpMessage> history(String group)
    {
    	List<MsgpMessage> history = new ArrayList<MsgpMessage>();
    	request = "msgp history "+group;
    	try
    	{
    		dos.writeUTF(request);
		
    		this.getReply();

    	}
    	catch (IOException e)
    	{
    		e.printStackTrace();
    	}
    	finally
    	{
    		// prints reply message in cases of No result and Error
    		if ( reply.startsWith("msgp 201") || reply.startsWith("msgp 400") )
    			System.out.println( reply );

    		// valid case
    		else
    		{
    			// split the history into individual messages
				// also get rid of the not-needed header "msgp send" by including it in the split delimiter
				String[] messages = reply.split("\n\nmsgp send");

				// decode all messages in the list and save them to the ArrayList history
				for ( String m: messages )
				{
					history.add( decodeMessage( m ) );
				}
    		}
			
			
    		return history;
    	}
    }


    /**
     * Encodes the sending of a message.
     *
     * @param message   message content
     * @return reply code, as per the spec
     */

    public int send(MsgpMessage msg)
    {
    	request = "msgp send\nfrom: "+msg.getFrom()+"\n";
    	for ( String t: msg.getTo())
    	{
    		request += ("to: "+ t +"\n");
    	}
    	request += "\n"+msg.getMessage()+"\n\n";
    	
    	try
    	{
    		dos.writeUTF(request);
		
    		this.getReply();
    	}
    	catch (IOException e)
    	{
    		e.printStackTrace();
    	}
    	finally
    	{
    		//System.out.println(reply);
    		return Integer.parseInt( reply.split(" ",3)[1] );
    	} 
    }

    

	/**
	 * helper method 
	 * gets response message from the listenner thread
	 * saves it to instance variable reply
	 **/
    private void getReply()
    {
    	do
		{
			try
			{
				Thread.sleep(25);
			}
			catch( InterruptedException ie)
			{
				ie.printStackTrace();
			}
		}
		while( replyBuffer.equals("") );
		
		reply = replyBuffer;
		replyBuffer = "";
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
     * the message-listenner thread run this method
     * constantly listens to the input stream for messages and replies
     **/
	public void run()
	{
		// the incoming message
 		String messageIn;
 		
		try
		{
			while( true )
			{
				messageIn = dis.readUTF();

				// if receives a message, send it to user agent to display to the user
				if (messageIn.startsWith("msgp send"))
				{
					userAgent.deliver( decodeMessage( messageIn ) );
				}
				
				// otherwise, its a reply 
				// save it to the replyBuffer to be retrieved by whom it concerns 
				else
				{
					replyBuffer = messageIn;
				}
			}
		}
		catch ( IOException e )
		{
			e.printStackTrace();
		}	
	} // end of run method
	
 }// end of class
