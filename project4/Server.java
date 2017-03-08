/*
 * Julian Domingo : jad5348
 * Alec Bargas : apb973
 */

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.net.*;
import java.io.*;

public class Server {
    private static ArrayList<InetSocketAddress> servers;
    private static ArrayList<Integer> timedOutServers;
    private static ExecutorService executorService;
    
    private static ArrayList<Item> inventory;
    private static ArrayList<User> clients;
    
    private static int serverID;

    private int serverInstances;
    private AtomicInteger requestID;

    private static PriorityQueue<Integer> pendingQueue;
  
    public static void main (String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        serverID = scanner.nextInt();
        serverInstances = scanner.nextInt();
        String inventoryPath = scanner.next();

        inventory = new ArrayList<Item>();
        clients = new ArrayList<User();
        timedOutServers = new ArrayList<Integer>();        
        servers = new ArrayList<InetSocketAddress>(serverInstances);
        pendingQueue = new PriorityQueue<Integer>();

        executorService = new Executors.newCachedThreadPool();

        System.out.println("[DEBUG] my ID: " + serverID);
        System.out.println("[DEBUG] serverInstances: " + serverInstances);
        System.out.println("[DEBUG] inventory path: " + inventoryPath);

        addServers();    
        parse(inventoryPath);

        requestID = new AtomicInteger(serverInstances);

        es.submit(new clientListener());
        es.submit(new serverListener());
    }         


    private static void requestCriticalSection() {
        Integer processID = requestID.getAndIncrement();
        pendingQueue.add(processID);
        String request = "Request:" + serverID
        send(request);
        waitUntilReadyFor(processID);
    }

    private static void waitUntilReadyFor(int processID) {
        while (timedOutServers.contains(pendingQueue.peek())) { pendingQueue.poll(); }
        while (pendingQueue.peek() != processID);
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
        User user = findUserThroughName(userName);
        
        if (isNewCustomer(user)) {
            clients.add(new User(userName));
            user = clients.get(clients.size() - 1);
        }

        if (!existsInInventory(productName)) {
            return "Not Available - We do not sell this product.";
        }
        else if (!inventoryHasEnoughOf(productName, quantity)) {
            return "Not Available - Not enough items.";
        }

        Order newOrder = new Order(productName, quantity);
        user.addOrder(newOrder);

        Item purchasedItem = findItem(productName);
        purchasedItem.purchaseQuantityOf(quantity);
    
        return "Your order has been placed, " + orderID + " " + userName + " " + productName + " " + quantity + ".";        
    }

    private static String cancel(int orderID) {
        User user = findUserThroughID(orderID);
            
        if (isNewCustomer(user)) {
            return ordreID + " not found, no such order.";
        }

        Order cancelledOrder = user.getOrder(orderID);

        removeItemFrom(cancelledOrder);
        user.removeOrder(orderID);

        return "Order " + orderID + " is cancelled.";        
    }

    private static String list() {
        String inventoryString = "";

        for (Item item : inventory) {
            inventoryString += item.getItemName();
            inventoryString += " ";
            inventoryString += Integer.toString(item.getCurrentQuantity());
            inventoryString += "$";
        }

        return inventoryString;
    }

    private static String search(String userName) {
        User user = findUserThroughName(userName);

        if (isNewCustomer(user)) {
            return userName + " is not an existing customer.";
        }

        if (!user.hasPlacedOrders()) {
            return "No order found for " + userName + ".";
        }

        String orderList = "";

        for (int order = 0; order < user.getOrderHistory().size(); order++) {
            orderList += Integer.toString(user.getOrderHistory().get(order).getID());
            orderList += ", ";
            orderList += user.getOrderHistory().get(order).getProductName();
            orderList += ", ";
            orderList += Integer.toString(user.getOrderHistory().get(order).getQuantity());
            orderList += "$";
        }

        return orderList;
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

    private static void addServers(Scanner scanner) {
        for (int server = 0; server < serverInstances; server++) {
            addNextServerFrom(scanner);
            System.out.println("Address for server " + server + ": " + servers.get(server).getAddress();
        }
    }

    public class ServerListener implements Runnable {
        public static void run() {
            
        }
    }

    public class ClientListener implements Runnable {
        public static void run() {

        }
    }    
}
