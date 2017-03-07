/*
 * Julian Domingo : jad5348
 * Alec Bargas : apb973
 */

import java.util.Scanner;

public class Server {
    private ArrayList<ServerWrapper> servers = new ArrayList<ServerWrapper>();

    public static void main (String[] args) {
        Scanner scanner = new Scanner(System.in);
        int serverID = scanner.nextInt();
        int serverInstances = scanner.nextInt();
        String inventoryPath = scanner.next();

        System.out.println("[DEBUG] my ID: " + serverID);
        System.out.println("[DEBUG] serverInstances: " + serverInstances);
        System.out.println("[DEBUG] inventory path: " + inventoryPath);

        for (int server = 0; server < serverInstances; server++) {
            addNextServerFrom(scanner);
            System.out.println("Address for server " + server + ": " + servers.get(server).getIPAddress());
        }
  
        while (true) {
        
        }
        // TODO: start server socket to communicate with clients and other servers

        // TODO: parse the inventory file

        // TODO: handle request from client
    }

    private static void addNextServerFrom(Scanner scanner) {
        String[] serverInformation = scanner.next().split(":");
        String IPAddress = serverInformation[0];
        String portNumber = serverInformation[1];
        servers.add(new ServerWrapper(IPAddress, portNumber));
    }

    public class ServerWrapper {
        private String IPAddress;
        private String portNumber;

        public ServerWrapper(String IPAddress, String portNumber) {
            this.IPAddress = IPAddress;
            this.portNumber = portNumber;
        }
    
        public static String getIPAddress() {
            return this.IPAddress;
        }

        public static String getPortNumber() {
            return this.portNumber;
        }
    }        
}
