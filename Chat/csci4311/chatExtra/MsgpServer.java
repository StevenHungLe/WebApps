/**
 * interface for TextMsgpServer 
 * handles the sending and receipt of correctly encoded msgp mesages
 */

package csci4311.chatExtra;

public interface MsgpServer
{
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
	public String join(String user, String group);


	/** 
	 * Method to handle leave request
	 * remove a user from a group
	 * 
	 * @param user	the name of the user that is leaving
	 * @param group the name of the group to leave from
	 * @return 		the generated reply which includes Response code and message
	 **/
	public String leave(String user, String group);


	/** 
	 * Method to handle groups request
	 * 
	 * @return the generated reply, which contains the list of groups
	 **/
	public String groups();

	/** 
	 * Method to handle users request
	 *
	 * @param group  	the name of the group to request membership for 
	 * @return 			the generated reply, which contains the membership of the requested group
	 **/
	public String users(String group);


	/** 
	 * Method to handle history request
	 *
	 * @param group 	the name of the group to request history for
	 * @return			the generated reply, which includes the history of the requested group
	 **/
	public String history(String group);


	/** 
	 * Method to handle send request
	 *
	 * @param request 	the request received from the client
	 * @return 			the generated reply which includes Response code and message
	 **/
	public String send(String request);


	/**
	 * send a message to a specified list of recipient users and groups
	 * messages sent to a group will be added to its history
	 *
	 * @param recipients 	The array of recipients of the message
	 * @param message 		The message to be sent
	 * @return 				reply code : {200,201,400}
	 **/
	public int sendMessage(String[] recipients, String message);

}
