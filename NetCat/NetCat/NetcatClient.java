// 
// CSCI 4311 Example Code: TCP Netcat Client
// Hung Le
//
package csci4311.nc;

import java.io.*;
import java.net.*;

public class NetcatClient {
	public static void main(String argv[]) throws Exception {
		Socket clientSocket = null;

		// Create client socket with connection to specified server's host name or localhost by default,
		// at specified port number or 6789 by default; 
		String hostName = argv.length > 0 ? argv[0] : "localhost" ;
		int portNumber = argv.length > 1 ? Integer.parseInt( argv[1] ) : 6789 ;
		clientSocket = new Socket( hostName, portNumber);	

		// Create a byte array to hold the bytes read from standard input or from the socket
			byte[] byteArray = new byte[999999];
			
		if ( System.in.available() == 0 ) // DOWNLOAD MODE: standard input has not been redirected
		{
			// Create input stream attached to socket
			DataInputStream inFromServer = new DataInputStream( 
				clientSocket.getInputStream());

			int bytesRead = 0;
			while ( bytesRead != -1 ) // while there is more bytes to be read
			{
				// read from socket
				bytesRead = inFromServer.read(byteArray,0,999999);

				// write to standard output
				System.out.write(byteArray,0, bytesRead >= 0 ? bytesRead : 0  );
			}
		}// end of if statement for download mode
		
		else // UPLOAD MODE: standard input has been redirected
		{
			System.out.println("Client upload mode activated.");

			// Create (buffered) input stream to read from standard input
			BufferedInputStream inputFromStandard = new BufferedInputStream( System.in );

			// Create output stream attached to connection socket
			DataOutputStream outToServer = new DataOutputStream( 
				clientSocket.getOutputStream());

			// Read input bytes from standard input and write to socket to upload
			int bytesRead = 0;
			int totalBytesRead = 0;
			while (  System.in.available() > 0 ) // while there is more bytes to be read
			{
				// Read from standard input
				bytesRead = inputFromStandard.read(byteArray,0,byteArray.length);
				
				// Write to socket
				outToServer.write(byteArray,0,bytesRead);

				// Keep a count of the total bytes read
				totalBytesRead += bytesRead;
			}
			
			System.out.println("Client successfully sent a total of " + bytesRead + " bytes.");
			
		}// end of else statement for upload mode

		// Close the client socket
		clientSocket.close();
		clientSocket = null;
	} // end main
} // end class
