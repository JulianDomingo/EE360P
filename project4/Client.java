/* 
 * Julian Domingo : jad5348
 * Alec Bargas : apb973
 */

import java.util.*;
import java.net.*;
import java.io.*;

public class Client {
    private static ArrayList<InetSocketAddress> servers; 
    private static int serverInstances;

    public static void main (String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter total number of servers: ");
        int serverInstances = scanner.nextInt();
        System.out.println("");

        servers = new ArrayList<InetSocketAddress>(serverInstances); 

        for (int server = 0; server < serverInstances; server++) { 
            addNextServerFrom(scanner);
        }

        while (scanner.hasNextLine()) {
            System.out.print("Enter a command: ");
            String command = scanner.next();
            String serverResponse = execute(command);
            System.out.println(serverResponse);
            System.out.println("");
        }
    }

    private static String send(InetSocketAddress server, String command) {
        String responseOfTCPServer;
        PrintWriter toServer;
        BufferedReader fromServer;
        Socket clientSocket;

        while (true) {
            try {
                clientSocket = new Socket(servers.get(0).getAddress(), servers.get(0).getPort());
                //clientSocket.connect(servers.get(0), 100);
                //clientSocket.setSoTimeout(100);
                fromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                toServer = new PrintWriter(clientSocket.getOutputStream());
                toServer.println(command);
                toServer.flush();

                if (!isTimedOut(fromServer, 100)) {
                    responseOfTCPServer = fromServer.readLine();
                    return responseOfTCPServer;
                }
                else {
                    deprecateServer();
                }

                clientSocket.close();
            }
            catch (SocketTimeoutException e) {
                deprecateServer();
            }
            catch (ConnectException e) {
                e.printStackTrace();
                deprecateServer();
            }
            catch (IOException e) {
                e.printStackTrace();               
            } 
        }
    }  

    private static boolean isTimedOut(BufferedReader fromServer, int timeOut) {
        long startTime = System.currentTimeMillis();
        try {
            while (!fromServer.ready()) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - startTime >= (long) timeOut) {
                    return true;
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static void deprecateServer() {
        servers.remove(0);
        serverInstances--;
    }  
    
    private static String execute(String command) {
        String[] tokens = command.split(" ");
        return (isValidCommand(tokens[0])) ? sendToTCPServer(command) : "ERROR: No such command";        
    }
        
    private static boolean isValidCommand(String commandArgument) {
        // "(?i)" denotes case insensitivity.
        return commandArgument.matches("(?i)purchase|cancel|list|search");
    } 
    
    private static void addNextServerFrom(Scanner scanner) {
        System.out.print("Enter a server in the form '<IP Address>:<Port Number>': ");
        String[] serverInformation = scanner.next().split(":");
        String IPAddress = serverInformation[0];
        int portNumber = Integer.parseInt(serverInformation[1]); 
        servers.add(new InetSocketAddress(IPAddress, portNumber));
    }   
    
    private static String sendToTCPServer(String command) {
        // Assumed majority of servers will not crash, so "severs.get(0)" will never fail.
        String serverResponse = send(servers.get(0), command);
        if (command.split(" ")[0].matches("(?i)list|search")) {
            serverResponse = reformatServerResponse(serverResponse);
        }
        return serverResponse;
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
}
