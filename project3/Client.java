/**
 * Julian Domingo : jad5348
 * Alec Bargas : apb973
 */

import java.net.*;
import java.io.*;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        String hostAddress;
        String currentProtocol;
        int portNumberTCP;
        int portNumberUDP;

        hostAddress = args[0];
        portNumberTCP = Integer.parseInt(args[1]);
        portNumberUDP = Integer.parseInt(args[2]);
        currentProtocol = "T";

        System.out.print("Enter a command: ");

        Scanner scanner = new Scanner(System.in);

        while (scanner.hasNextLine()) {
            String serverResponse = "";
            String command = scanner.nextLine();
            String[] arguments = command.split(" ");

            if (arguments[0].contains("setmode")) {
                currentProtocol = arguments[1];
                System.out.println("Current protocol for process is: " + currentProtocol.toUpperCase());
            }
            else {
                serverResponse = send(hostAddress, portNumberTCP, portNumberUDP, command, currentProtocol);
            }

            if (arguments[0].contains("list")) {
                serverResponse = reformatServerResponse(serverResponse);
            }

            System.out.println(serverResponse);
            System.out.print("Enter a command: ");
        }
    }

    private static String reformatServerResponse(String serverResponse) {
        String reformattedResponse = "";
        String[] items = serverResponse.split("\\$");

        for (String item : items) {
            reformattedResponse += item;
            reformattedResponse += "\r\n";
        }

        return reformattedResponse;
    }

    private static String sendUDP(String hostName, int portNumberUDP, String command) {
        byte[] receivingBuffer = new byte[1024];
        DatagramPacket sendingPacket;
        DatagramPacket receivingPacket;
        DatagramSocket dataSocket = null;

        try {
            InetAddress iNetAddress = InetAddress.getByName(hostName);
            dataSocket = new DatagramSocket();
            byte[] sendingBuffer = command.getBytes();
            sendingPacket = new DatagramPacket(sendingBuffer, sendingBuffer.length, iNetAddress, portNumberUDP);
            dataSocket.send(sendingPacket);
            receivingPacket = new DatagramPacket(receivingBuffer, receivingBuffer.length);
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
        String responseOfTCPServer;
        Scanner scanner;
        PrintStream printStream;

        try {
            Socket clientSocket = new Socket(hostName, portNumberTCP);
            scanner = new Scanner(clientSocket.getInputStream());
            printStream = new PrintStream(clientSocket.getOutputStream());
            printStream.println(command);
            printStream.flush();
            responseOfTCPServer = scanner.nextLine();
            clientSocket.close();
            return responseOfTCPServer;
        }
        catch(IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static String send(String hostAddress, int portNumberTCP, int portNumberUDP, String command, String currentProtocol) {
        if (currentProtocol.toUpperCase().equals("U")) {
            return sendUDP(hostAddress, portNumberUDP, command);
        }
        return sendTCP(hostAddress, portNumberTCP, command);
    }
}
