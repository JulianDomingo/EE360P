/* 
 * Julian Domingo : jad5348
 * Alec Bargas : apb973
 */

import java.util.*;
import java.net.*;
import java.io.*;

public class Client {
    private ArrayList<InetSocketAddress> servers; 
    private int serverInstances;

    public static void main (String[] args) {
        Client client = new Client();
        Scanner scanner = new Scanner(System.in);   
        client.serverInstances = scanner.nextInt();
        scanner.nextLine();
        
        client.servers = new ArrayList<InetSocketAddress>(client.serverInstances); 

        for (int server = 0; server < client.serverInstances; server++) { 
            client.addNextServerFrom(scanner);
        }

        while (scanner.hasNextLine()) {
            String command = scanner.nextLine();
            String serverResponse = client.execute(command); 
            System.out.println(serverResponse); 
        }
        scanner.close();
    }

    private String send(InetSocketAddress server, String command) {
        String responseOfTCPServer;
        PrintStream printStream;
        Scanner scanner;
        Socket clientSocket;

        while (true) {
            try {
                clientSocket = new Socket(servers.get(0).getHostName(),servers.get(0).getPort());
                clientSocket.setSoTimeout(100);
                scanner = new Scanner(clientSocket.getInputStream());
                printStream = new PrintStream(clientSocket.getOutputStream());
                printStream.println(command);
                printStream.flush();
                do
                {
                	responseOfTCPServer = scanner.nextLine();
                }while(responseOfTCPServer.equals("alive"));
                scanner.close();
                clientSocket.close();
                return responseOfTCPServer;
            }
            catch(NoSuchElementException e)
            {
            	deprecateServer();
            }
            catch(SocketTimeoutException e)
            {
            	deprecateServer();
            }
            catch(ConnectException e)
            {
            	deprecateServer();
            }
            catch (IOException e) {
                e.printStackTrace();               
            }
        }
    }  

    private void deprecateServer() {
        servers.remove(0);
        serverInstances--;
    }  
    
    private String execute(String command) {
        String[] tokens = command.split(" ");
        return (isValidCommand(tokens[0])) ? sendToTCPServer(command) : "ERROR: No such command";        
    }
        
    private boolean isValidCommand(String commandArgument) {
        // "(?i)" denotes case insensitivity.
        return commandArgument.matches("(?i)purchase|cancel|list|search");
    } 
    
    private void addNextServerFrom(Scanner scanner) {
        String[] serverInformation = scanner.nextLine().split(":");
        String IPAddress = serverInformation[0];
        int portNumber = Integer.parseInt(serverInformation[1]); 
        servers.add(new InetSocketAddress(IPAddress, portNumber));
    }   
    
    private String sendToTCPServer(String command) {
        // Assumed majority of servers will not crash, so "severs.get(0)" will never fail.
        String serverResponse = send(servers.get(0), command);
        if (command.split(" ")[0].matches("(?i)list|search")) {
            serverResponse = reformatServerResponse(serverResponse);
        }
        return serverResponse;
    }   

    private String reformatServerResponse(String serverResponse) {
        String reformattedResponse = "";
        String[] items = serverResponse.split("\\$");

        for (String item : items) {
            reformattedResponse += item;
            reformattedResponse += "\r\n";
        }

        return reformattedResponse;
    }
}
