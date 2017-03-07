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
            String[] tokens = parseNextCommandFrom(scanner);
            send
            System.out.println("ERROR: No such command");
        }
    }
    
    private static void addNextServerFrom(Scanner scanner) {
        String[] serverInformation = scanner.nextLine();
        String IPAddress = serverInformation[0];
        int portNumber = Integer.parseInt(serverInformation[1]); 
        servers.add(new InetSocketAddress(IPAddress, portNumber);
    }   
    
}
