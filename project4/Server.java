/*
 * Julian Domingo : jad5348
 * Alec Bargas : apb973
 */

import java.util.Scanner;

public class Server {
    private static ArrayList<InetSocketAddress> servers;
    private static ExecutorService executorService;
    
    private static ArrayList<Item> inventory;
    private static ArrayList<User> clients;

    public static void main (String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        int serverID = scanner.nextInt();
        int serverInstances = scanner.nextInt();
        String inventoryPath = scanner.next();

        inventory = new ArrayList<Item>();
        clients = new ArrayList<User.();        
        servers = new ArrayList<InetSocketAddress>(serverInstances);

        executorService = new Executors.newCachedThreadPool();

        System.out.println("[DEBUG] my ID: " + serverID);
        System.out.println("[DEBUG] serverInstances: " + serverInstances);
        System.out.println("[DEBUG] inventory path: " + inventoryPath);

        for (int server = 0; server < serverInstances; server++) {
            addNextServerFrom(scanner);
            System.out.println("Address for server " + server + ": " + servers.get(server).getAddress();
        }
    
        parse(inventoryPath);

            // TODO: start server socket to communicate with clients and other servers

            // TODO: parse the inventory file

            // TODO: handle request from client
    }         

    private static String execute(String command) {
        String tokens[] = command.split(" ");

        switch (tokens[0]) {
            case "purchase":
                return purchase(tokens[1], tokens[2], Integer.parseInt(tokens[3]));
            case "cancel":
                return cancel(Integer.parseInt(tokens[1]));
            case "list":
                return list();
            case "search":
                return search(tokens[1]);
            default:
                return "ERROR: No such command"; 
        }
    }

    private static String purchase(String userName, String productName, int quantity) {
       
    }
           
    private static void addNextServerFrom(Scanner scanner) {
        String[] serverInformation = scanner.nextLine().split(":");
        String IPAddress = serverInformation[0];
        int portNumber = Integer.parseInt(serverInformation[1]);
        servers.add(new InetSocketAddress(IPAddress, portNumber));
    }

    private static void parse(String path) {
        try {
            addItemsToInventoryFrom(path);
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void addItemsToInventoryFrom(String path) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new FileReader(path));
        String inputLine = bufferedReader.readLine();
        
        while (inputLine != null) {
            String[] arguments = inputLine.split(" ");
            Item item = formItemFrom(arguments);
            inventory.add(item);
            inputLine = bufferedReader.readLine();
        }
        
        bufferedReader.close();        
    }

    private static Item formItemFrom(String[] arguments) {
        return new Item(arguments[0], Integer.parseInt(arguments[1]));
    }        
}
