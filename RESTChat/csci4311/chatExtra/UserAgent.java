/**
 * interface for user agent which handles user's interaction which the program
 */

package csci4311.chatExtra;

public interface UserAgent {
    // Deliver a (remote) message to the user agent
    public void deliver(MsgpMessage message);
}
