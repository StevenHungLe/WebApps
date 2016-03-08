// 
// CSCI 4311 Example Code: TCP Netcat Server
// Hung Le
//
package csci4311.nc;

import java.io.*;
import java.net.*;

public class NetcatServer {
	public static void main(String argv[]) throws Exception {
	
		Socket connectionSocket = null;
		
		// Create welcoming socket using specified port number or 6789 by default
		int portNumber = argv.length > 0 ? Integer.parseInt(argv[0]) : 6789 ;
	   	ServerSocket welcomeSocket = new ServerSocket( portNumber, 0);

		// Create a byte array to hold the bytes read from standard input or from the socket
			byte[] byteArray = new byte[999999];

		//while(true){  // a while loop can be added here and span toward the end of the main method,
				// to simulate the behavior of the welcomeSocket that waits for a connection and repeat the whole process.
				// It is omited here because there is no specified condition to stop the loop.
			
		// Wait for some client to connect and create new socket for connection
		if( connectionSocket == null) {
			connectionSocket = welcomeSocket.accept();
		}
	
		if ( System.in.available() > 0 ) // UPLOAD MODE: standard input has been redirected
		{ 
			System.out.println("Server upload mode activated.");
	    		//while(true) {

			// Create (buffered) input stream to read from standard input
			BufferedInputStream inputFromStandard = new BufferedInputStream( System.in );

			// Create output stream attached to connection socket
			DataOutputStream outToClient = new DataOutputStream( 
				connectionSocket.getOutputStream());

			// Read input bytes from standard input and write to socket to upload
			int bytesRead = 0;
			int totalBytesRead = 0;
			while (  System.in.available() > 0 ) // while there is more bytes to be read
			{
				// Read from standard input
				bytesRead = inputFromStandard.read(byteArray,0,byteArray.length);
				
				// Write to socket
				outToClient.write(byteArray,0,bytesRead);

				// Keep a count of the total bytes read
				totalBytesRead += bytesRead;
			}
			
			System.out.println("Server successfully sent a total of " + bytesRead + " bytes.");
			
		} // end of if statement for upload mode

		else // DOWNLOAD MODE: standard input has not been redirected 
		{
			// Create input stream attached to socket
			DataInputStream inFromClient = new DataInputStream( 
				connectionSocket.getInputStream());

			// Read input bytes from socket and write to standard output
			int bytesRead = 0;
			while ( bytesRead != -1 ) // while there is more bytes to be read
			{
				// read from socket
				bytesRead = inFromClient.read(byteArray,0,999999);

				// write to standard output
				System.out.write(byteArray,0, bytesRead >= 0 ? bytesRead : 0  );
			}
		} // end of else statement for download mode

		// Close the connection socket
		connectionSocket.close();
		connectionSocket = null;
		
	} // end main
} // end class
