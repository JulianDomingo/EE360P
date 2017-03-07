/* 
 * Julian Domingo : jad5348
 * Alec Bargas : apb973
 */

import java.util.Scanner;

public class Client {
    private ArrayList<InetSocketAddress> servers; 

    public static void main (String[] args) {
        Scanner scanner = new Scanner(System.in);   
        int serverInstances = sc.nextInt();

        servers = new ArrayList<InetSocketAddress>(serverInstances); 

        for (int server = 0; server < serverInstances; server++) { 
            addNextServerFrom(scanner);
        }

        while (sc.hasNextLine()) {
            String command = scanner.nextLine();
            String serverResponse = execute(command);           
            System.out.println(serverResponse); 
        }
    }

    private static String send(InetSocketAddress server, String command) {
        String responseOfTCPServer;
        PrintStream printStream;
        Scanner scanner;

        try {
            Socket clientSocket = new Socket();
            clientSocket.connect(server, 100);
            scanner = new Scanner(clientSocket.getInputStream());
            printStream = new PrintStream(clientSocket.getOutputStream());
            printStream.println(command);
            printStream.flush();
            responseOfTCPServer = scanner.nextLine();
            clientSocket.close();
            return responseOfTCPServer;
        }
        catch (IOException e) {
            e.printStackTrace();
        }        
        return null;
    }    
    
    private static String execute(String command) {
        String[] tokens = command.split(" ");
        return (isValidCommand(tokens[0]) ? sendToTCPServer(command) : "ERROR: No such command";
        }
    }
        
    private static boolean isValidCommand(String commandArgument) {
        // "(?i)" denotes case insensitivity.
        return commandArgument.matches("(?i)purchase|cancel|list|search");
    } 
    
    private static void addNextServerFrom(Scanner scanner) {
        String[] serverInformation = scanner.nextLine();
        String IPAddress = serverInformation[0];
        int portNumber = Integer.parseInt(serverInformation[1]); 
        servers.add(new InetSocketAddress(IPAddress, portNumber);
    }   
    
    private static void sendToTCPServer(String command) {
        return send(servers.get(0), command);
    }   
}
