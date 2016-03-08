// 
// CSCI 4311: UDP Netcat Server
// Hung Le
//
package csci4311.ncudp;

import java.io.*;
import java.net.*;

public class NetcatUDPServer {
	public static void main(String argv[]) throws Exception {
		
		// Create server socket bound to socket 678
	   	DatagramSocket serverSocket = new DatagramSocket(678);

		// get Ip Address for the client host
		InetAddress ipAddress = InetAddress.getByName("localhost");
		
		// Create a byte array to hold the bytes read from standard input or from the socket
		byte[] byteArray = new byte[1024];
	
		if ( System.in.available() > 0 ) // UPLOAD MODE: standard input has been redirected
		{ 
			System.out.println("Server upload mode activated.");

			// Create (buffered) input stream to read from standard input
			BufferedInputStream inputFromStandard = new BufferedInputStream( System.in );

			// Read input bytes from standard input and write to socket to upload
			int bytesRead = 0;
			int totalBytesRead = 0;
			do
			{
				// Read from standard input
				bytesRead = inputFromStandard.read(byteArray,0,byteArray.length);
				// Create datagram Packet to send
				DatagramPacket sendPacket = new DatagramPacket(byteArray,bytesRead,ipAddress,6789);
				// Send the packet
				serverSocket.send(sendPacket);
				// Keep a count of the total bytes read
				totalBytesRead += bytesRead;
			}
			while (  bytesRead == 1024 ); // the buffer is filled up => there is more bytes to be read
			
			System.out.println("Server successfully sent a total of " + totalBytesRead + " bytes.");
			
		} // end of if statement for upload mode

		else // DOWNLOAD MODE: standard input has not been redirected 
		{
			int bytesReceived = 0;
			do 
			{
				// Create a datagram packet to receive from client
				DatagramPacket receivePacket = new DatagramPacket(byteArray,byteArray.length);
				// receive from socket
				serverSocket.receive(receivePacket);
				// check the length of the data array received
				bytesReceived = receivePacket.getLength(); 
				// write to standard output
				System.out.write(byteArray,0, bytesReceived);
			}
			while ( bytesReceived == 1024 );  // the buffer is filled up => there is more bytes to be received
		} // end of else statement for download mode

		// Close the connection socket
		serverSocket.close();
		serverSocket = null;
		
	} // end main
} // end class
