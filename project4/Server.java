/*
 * Julian Domingo : jad5348
 * Alec Bargas : apb973
 */

import java.util.Scanner;

public class Server {
    private ArrayList<InetSocketAddress> servers;

    public static void main (String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        int serverID = scanner.nextInt();
        int serverInstances = scanner.nextInt();
        String inventoryPath = scanner.next();

        servers = new ArrayList<InetSocketAddress>(serverInstances);

        System.out.println("[DEBUG] my ID: " + serverID);
        System.out.println("[DEBUG] serverInstances: " + serverInstances);
        System.out.println("[DEBUG] inventory path: " + inventoryPath);

        for (int server = 0; server < serverInstances; server++) {
            addNextServerFrom(scanner);
            System.out.println("Address for server " + server + ": " + servers.get(server).getAddress();
        }
    
        try {
            while (true) {
                        
            }
            // TODO: start server socket to communicate with clients and other servers

            // TODO: parse the inventory file

            // TODO: handle request from client
        }
    }         

    private static void addNextServerFrom(Scanner scanner) {
        String[] serverInformation = scanner.nextLine().split(":");
        String IPAddress = serverInformation[0];
        int portNumber = Integer.parseInt(serverInformation[1]);
        servers.add(new InetSocketAddress(IPAddress, portNumber));
    }
}
