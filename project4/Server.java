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

    private static int serverInstances;
    private static AtomicInteger requestID;

    private static PriorityQueue<Integer> pendingQueue;

    private static int[] serverMessages;
  
    public static void main (String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        serverID = scanner.nextInt();
        serverInstances = scanner.nextInt();
        String inventoryPath = scanner.next();

        inventory = new ArrayList<Item>();
        clients = new ArrayList<User>();
        timedOutServers = new ArrayList<Integer>();        
        servers = new ArrayList<InetSocketAddress>(serverInstances);
        pendingQueue = new PriorityQueue<Integer>();

        executorService = Executors.newCachedThreadPool();

        System.out.println("[DEBUG] my ID: " + serverID);
        System.out.println("[DEBUG] serverInstances: " + serverInstances);
        System.out.println("[DEBUG] inventory path: " + inventoryPath);

        addServers(scanner);   
        instantiateServerMessages();

        parse(inventoryPath);

        requestID = new AtomicInteger(serverInstances);

        executorService.submit(new ServerListener());
        executorService.submit(new ClientListener());
    }         

    static public class ServerCommunication implements Runnable {
        private String message;
        private int serverNumber;

        ServerCommunication(int serverNumber, String message) {
            this.serverNumber = serverNumber;
            this.message = message;
        }

        public void run() {
            String response;
            PrintStream printStream;
            Scanner scanner;
            
            try {
                Socket clientSocket = new Socket();
                clientSocket.connect(servers.get(serverNumber), 100);
                clientSocket.setSoTimeout(100);
                scanner = new Scanner(clientSocket.getInputStream());
                printStream = new PrintStream(clientSocket.getOutputStream());
                printStream.println(message);
                printStream.flush();
                response = scanner.nextLine();
                if (response != null) { return; }
            }
            catch (SocketTimeoutException e) {
                deprecateServer(serverNumber);
            }
            catch (ConnectException e) {
                deprecateServer(serverNumber);
            }
            catch (IOException e) {
                e.printStackTrace();
            }                
        }
    }

    private static void deprecateServer(int serverNumber) {
        timedOutServers.add(serverNumber);
        pendingQueue.remove(serverNumber);
    }

    private static void requestCriticalSection() {
        Integer processID = requestID.getAndIncrement();
        pendingQueue.add(processID);
        String request = "Request:" + serverID;
        send(request);
        waitUntilReadyFor(processID);
    }

    private static void releaseCriticalSection(String command) {
        String release = "Release: " + command + ":" + serverID;
        send(command);
        pendingQueue.poll();
    }

    private static void send(String message) {
        for (int server = 0; server < serverInstances; server++) {
            if (server != serverID && !timedOutServers.contains(server)) {
                executorService.submit(new ServerCommunication(server, message));
            }
        }
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
    
        return "Your order has been placed, " + newOrder.getID() + " " + userName + " " + productName + " " + quantity + ".";        
    }

    private static String cancel(int orderID) {
        User user = findUserThroughID(orderID);
            
        if (isNewCustomer(user)) {
            return orderID + " not found, no such order.";
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
            System.out.println("Address for server " + server + ": " + servers.get(server).getAddress());
        }
    }

    private static void addNextServerFrom(Scanner scanner) {
        String[] serverInformation = scanner.nextLine().split(":");
        String IPAddress = serverInformation[0];
        int portNumber = Integer.parseInt(serverInformation[1]);
        servers.add(new InetSocketAddress(IPAddress, portNumber));
    }

    private static void instantiateServerMessages() {
        for (int server = 0; server < serverInstances; server++) {
            serverMessages[server] = 0;
        }
    }

    private static User findUserThroughName(String userName) {
        for (User user : clients) {
            if (user.getUsername().equals(userName)) {
                return user;
            }
        }
        return null;
    }

    private static User findUserThroughID(int orderID) {
        for (User user : clients) {
            for (Order order : user.getOrderHistory()) {
                if (order.getID() == orderID) {
                    return user;
                }
            }
        }
        return null;
    }    

    private static boolean isNewCustomer(User user) {
        return user == null;
    }

    private static void removeItemFrom(Order cancelledOrder) {
        for (Item item : inventory) {
            if (item.getItemName().equals(cancelledOrder.getProductName())) {
                item.returnQuantityOf(cancelledOrder.getQuantity());
            }
        }
    }
    
    private static Item findItem(String productName) {
        for (Item item : inventory) {
            if (item.getItemName().equals(productName)) {
                return item;
            }
        }
        return null;
    }

    private static boolean inventoryHasEnoughOf(String productName, int desiredQuantity) {
        for (Item item : inventory) {
            if (item.getItemName().equals(productName) && 
                item.getCurrentQuantity() < desiredQuantity) 
            {
                return false;
            }
        }
        return true;
    }

    private static boolean existsInInventory(String productName) {
        for (Item item : inventory) {
            if (item.getItemName().equals(productName)) {
                return true;
            }
        }
        return false;
    }

    public static class ServerListener implements Runnable {
        public void run() {
            submitNewServerProcess();
        }
    }

    private static void submitNewServerProcess() {    
        try {
            ServerSocket serverSocket = new ServerSocket(servers.get(serverID).getPort());
            while (true) {
                Socket socket = serverSocket.accept();
                executorService.submit(new ClientTask(socket));
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }           
    }

    public static class ClientListener implements Runnable {
        public void run() {
            submitNewClientProcess();
        }
    }   

    private static void submitNewClientProcess() {
        try {
            ServerSocket serverSocket = new ServerSocket(servers.get(serverID).getPort());
            while (true) {
                Socket socket = serverSocket.accept();
                executorService.submit(new ServerTask(socket));
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }        

    public static class ServerTask implements Runnable {
        private Socket serverSocket;

        ServerTask(Socket serverSocket) {
            this.serverSocket = serverSocket;
        }

        public void run() {
            serviceServerTask(serverSocket);
        }
    }

    public static class ClientTask implements Runnable {
        private Socket clientSocket;

        ClientTask(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        public void run() {
            serviceClientTask(clientSocket);
        }
    }

    private static void serviceServerTask(Socket socket) {
        String command;
        String response;
        PrintStream printStream;
        Scanner scanner;

        try {
            scanner = new Scanner(socket.getInputStream());
            printStream = new PrintStream(socket.getOutputStream());
            command = scanner.nextLine();
            String[] tokens = command.split(":");

            if (tokens[0].equals("Request")) {
                int processID = Integer.parseInt(tokens[1]);
                if (serverMessages[processID] > 0) {
                    serverMessages[processID]--;
                }
                else {
                    pendingQueue.add(processID);
                }
            }
            else if (tokens[0].equals("Release")) {
                response = execute(tokens[1]);
                Integer processID = Integer.parseInt(tokens[2]);
                if (!pendingQueue.contains(processID)) {
                    serverMessages[processID]++;
                }
                else {
                    pendingQueue.poll();
                }
            }                   
        }   
        catch (IOException e) {
            e.printStackTrace();
        }                 
    }

    private static void serviceClientTask(Socket socket) {
        String command;
        String response;
        PrintStream printStream;
        Scanner scanner;

        try {
            scanner = new Scanner(socket.getInputStream());
            printStream = new PrintStream(socket.getOutputStream());
            command = scanner.nextLine();
            requestCriticalSection();
            response = execute(command);
            releaseCriticalSection(command);
            printStream.println(response);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }    
}

