/**
 * TCP NetcatServer - the server side of netcat, a simple file transfer program using TCP
 * 
 * The server works in two modes:
 * Download mode: read input from the tcp connection and write it to a redirected file
 * activate this mode by redirecting standard out to a file
 * Upload mode: read input from a redirected file and write it to the tcp connection
 * activate this mode by redirecting standard in from a file
 *  
 * author: Hung Le
 **/
//package com.stevenlesoft.nc;

import java.io.*;
import java.net.*;

public class NetcatServer {
	public static void main(String argv[]) throws Exception {
	
		Socket connectionSocket = null;
		
		// Create welcoming socket using specified port number or 6789 by default
		int portNumber = argv.length > 0 ? Integer.parseInt(argv[0]) : 6789 ;
	   	ServerSocket welcomeSocket = new ServerSocket( portNumber, 0);

		// Create a byte array to hold the bytes read from standard input or from the socket
			byte[] byteArray = new byte[1024];

		// loop endlessly accepting connection request
		while(true)
		{ 
			
			// Wait for some client to connect and create new socket for connection
			if( connectionSocket == null) {
				connectionSocket = welcomeSocket.accept();
			}
		
			if ( System.in.available() > 0 ) // UPLOAD MODE: standard input has been redirected
			{ 
	
				// Create (buffered) input stream to read from standard input
				BufferedInputStream inputFromStandard = new BufferedInputStream( System.in );
	
				// Create output stream attached to connection socket
				DataOutputStream outToClient = new DataOutputStream( 
					connectionSocket.getOutputStream());
	
				// Read input bytes from standard input and write to socket to upload
				int bytesRead = 0;
				int totalBytesRead = 0;
				while (  bytesRead != -1 ) // while there is more bytes to be read
				{
					// Read from standard input
					bytesRead = inputFromStandard.read(byteArray,0,byteArray.length);
					
					// write to socket, write the number of bytesread or 0 in case bystesRead = -1
					outToClient.write(byteArray,0,bytesRead >= 0 ? bytesRead : 0);
	
					// Keep a count of the total bytes read
					totalBytesRead += bytesRead;
				}
				
				System.out.println("Server successfully sent a total of " + totalBytesRead + " bytes.");
				
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
					bytesRead = inFromClient.read(byteArray,0,1024);
	
					// write to standard output, write the number of bytesread or 0 in case bystesRead = -1
					System.out.write(byteArray,0, bytesRead >= 0 ? bytesRead : 0  );
				}
				
				//close the file upon completion of transmission
				System.out.close();
				
			} // end of else statement for download mode
			
			// Close the connection socket
			connectionSocket.close();
			connectionSocket = null;
		}
		
	} // end main
} // end class
