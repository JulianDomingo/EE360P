/**
 * Julian Domingo : jad5348
 * Alec Bargas : apb973
 */

import java.net.*;
import java.io.*;
import java.util.Scanner;

public class Client {
	private static String sendUDP(String hostName, int portNumberUDP, String command) {
		byte[] receivingBuffer = new byte[1024];
		DatagramPacket sendingPacket;
		DatagramPacket receivingPacket;
		DatagramSocket dataSocket = null;
		
		try {
			InetAddress iNetAddress = InetAddress.getByName(hostName);
			dataSocket = new DatagramSocket();
			byte[] sendingBuffer = new byte[command.length()];
			sendingBuffer = command.getBytes();
			sendingPacket = new DatagramPacket(sendingBuffer, sendingBuffer.length, iNetAddress, portNumberUDP);
			dataSocket.send(sendingPacket);
			receivingPacket = new DatagramPacket(recevingBuffer, receivingBuffer.length);
			dataSocket.receive(receivingPacket);
			return new String(receivingPacket.getData(), 0, receivingPacket.getLength());
		} 
		catch(UnknownHostException e) {
			e.printStackTrace();
		} 
		catch(SocketException e) {
			e.printStackTrace();
		} 
		catch(IOException e) {
			e.printStackTrace();
		} 
		finally {
			dataSocket.close();
		}
		
		return null;
	}
  
	private static String sendTCP(String hostName, int portNumberTCP, String command) {
		String response;  
		Scanner scanner;
		PrintStream printStream;

		try {  
			Socket clientSocket = new Socket(hostName, portNumberTCP);  
			scanner = new Scanner(clientSocket.getInputStream());
			printStream = new PrintStream(clientSocket.getOutputStream());
			printStream.println(command);
			printStream.flush();
			response = scanner.nextLine();
			clientSocket.close();
			return response;
		} 
		catch(IOException e) {
			e.printStackTrace();
		}

		return null;
	} 
  
	private static String send(String hostAddress, int portNumberTCP, int portNumberUDP, String command, String protocol) {
		if (protocol.equals("U")) {
	  		return sendUDP(hostAddress, portNumberUDP, command);
	  	} 
	  	else if (protocol.equals("T")) {
	  		return sendTCP(hostAddress, portNumberTCP, command);
	  	}
	}
  
	public static void main(String[] args) {
	    String hostAddress;
	    int portNumberTCP;
	    int portNumberUDP;

	    hostAddress = args[0];
	    portNumberTCP = Integer.parseInt(args[1]);
	    portNumberUDP = Integer.parseInt(args[2]);

	    Scanner scanner = new Scanner(System.in);

	    while (sc.hasNextLine()) {
			String command = scanner.nextLine();
			String[] arguments = command.split(" ");

			String serverResponse = send(hostAddress, portNumberTCP, portNumberUDP, command, arguments[arguments.length - 1]);
			System.out.println(serverResponse);
	    }
	}

