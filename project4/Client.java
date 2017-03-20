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
        int serverInstances = scanner.nextInt();

        servers = new ArrayList<InetSocketAddress>(serverInstances); 

        for (int server = 0; server < serverInstances; server++) { 
            addNextServerFrom(scanner);
        }

        while (scanner.hasNextLine()) {
            String command = scanner.nextLine();
            String serverResponse = execute(command); 

            System.out.println(serverResponse); 
        }
    }

    private static String send(InetSocketAddress server, String command) {
        String responseOfTCPServer;
        PrintStream printStream;
        Scanner scanner;

        while (true) {
            try {
                Socket clientSocket = new Socket();
                clientSocket.connect(server, 100);
                clientSocket.setSoTimeout(100);
                scanner = new Scanner(clientSocket.getInputStream());
                printStream = new PrintStream(clientSocket.getOutputStream());
                printStream.println(command);
                printStream.flush();
                responseOfTCPServer = scanner.nextLine();
                clientSocket.close();
                return responseOfTCPServer;
            }
            catch (SocketTimeoutException e) {
                deprecateServer();
            }
            catch (ConnectException e) {
                deprecateServer();
            }
            catch (IOException e) {
                e.printStackTrace();               
            }   
        }
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
        String[] serverInformation = scanner.nextLine().split(":");
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
