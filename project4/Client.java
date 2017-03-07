/* 
 * Julian Domingo : jad5348
 * Alec Bargas : apb973
 */

import java.util.Scanner;

public class Client {
	private ArrayList<Server> servers;
    
    public static void main (String[] args) {
        Scanner scanner = new Scanner(System.in);   
        int numberOfServers = sc.nextInt();
        for (int server = 0; server < numberOfServers; server++) { 
            addNextServerFrom(scanner);
        }

        while (sc.hasNextLine()) {
            String cmd = sc.nextLine();
            String[] tokens = cmd.split(" ");

            if (tokens[0].equals("purchase")) {
            
            } else if (tokens[0].equals("cancel")) {
            
            } else if (tokens[0].equals("search")) {
            
            } else if (tokens[0].equals("list")) {
            
            } else {
                System.out.println("ERROR: No such command");
            }
        }
    }
    
    private static void addNextServerFrom(Scanner scanner) {
        String[] serverInformation = scanner.nextLine();
        String IPAddress = serverInformation[0];
        String portNumber = serverInformatino[1];
        servers.add(new Server(IPAddress, portNumber));
    }   
    
    public class Server {
        private String IPAddress;
        private String portNumber;

        public Server(String IPAddress, String portNumber) {
            this.IPAddress = IPAddress;
            this.portNumber = portNumber;
        }

        public String getIPAddress() {
            return this.IPAddress;
        }

        public String getPortNumber() {
            return this.portNumber;
        }
    }        
}
