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

    private static TimeStamp myTimeStamp;
    private static ArrayList<TimeStamp> pendingQueue = new ArrayList<TimeStamp>();

    private static PriorityQueue<ClientInformation> clientCommands = new PriorityQueue<ClientInformation>();

    private static int acknowledgements = 0;
  
    public static void main (String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter server ID: ");
        serverID = scanner.nextInt();
        System.out.print("Enter total number of servers: ");
        serverInstances = scanner.nextInt();
        System.out.print("Enter inventory.txt file path: ");
        String inventoryPath = scanner.next();
        System.out.println("");

        inventory = new ArrayList<Item>();
        clients = new ArrayList<User>();
        timedOutServers = new ArrayList<Integer>();
        servers = new ArrayList<InetSocketAddress>(serverInstances);

        executorService = Executors.newCachedThreadPool();

        System.out.println("[DEBUG] my ID: " + serverID);
        System.out.println("[DEBUG] serverInstances: " + serverInstances);
        System.out.println("[DEBUG] inventory path: " + inventoryPath);

        System.out.println("");

        addServers(scanner);

        parse(inventoryPath);

        executorService.submit(new ServerListener());

        int serverInstancePort = servers.get(serverID - 1).getPort();
        System.out.println("Is server listening to port " + serverInstancePort + "?: " + isServerListening("127.0.0.1", serverInstancePort));
    }

    public static class ServerCommunication implements Runnable {
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

    private static void deprecateServer(int serverID) {   
        timedOutServers.add(serverID);
        remove(serverID);
        serverInstances--;
    }

    private static void remove(int serverID) {
        TimeStamp timeStampToRemove = search(serverID);
        if (timeStampToRemove != null) {
            pendingQueue.remove(timeStampToRemove);
        }
    }

    private static void requestCriticalSection() {
        TimeStamp timeStamp = search(serverID);
        timeStamp.setLogicalClockSend();

        String request = "Request:" + myTimeStamp.getLogicalClock() + ":" + serverID;
        send(request);
    }

    private static TimeStamp search(int serverID) {
        for (TimeStamp timeStamp : pendingQueue) {
            if (timeStamp.getPID() == serverID) {
                return timeStamp;
            }
        }
        return null;
    }

    private static void releaseCriticalSection() {
        acknowledgements = 0;
        String release = "Release: " + myTimeStamp.getLogicalClock() + ":" + serverID;
        send(release);
    }

    private static void send(String message) {
        for (int server = 0; server < serverInstances; server++) {
            if (server != serverID && !timedOutServers.contains(server)) {
                executorService.submit(new ServerCommunication(server, message));
            }
        }
    }

    public static class ServerListener implements Runnable {
        public void run() {
            submitNewServerProcess();
        }
    }

    private static void submitNewServerProcess() {    
        try {
            // serverID - 1 since serverID's start from 1.
            ServerSocket serverSocket = new ServerSocket(servers.get(serverID - 1).getPort());
            while (true) {
                Socket socket = serverSocket.accept();
                executorService.submit(new ServerTask(socket));
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
            ServerSocket serverSocket = new ServerSocket(servers.get(serverID - 1).getPort());
            while (true) {
                Socket socket = serverSocket.accept();
                executorService.submit(new ClientTask(socket));
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
            //command = scanner.nextLine();
            command = scanner.next();
            String[] tokens = command.split(":");

            if (tokens[0].equals("Request")) {
                String acknowledgement = "Acknowledgement";
                myTimeStamp.setLogicalClockReceive(Integer.parseInt(tokens[1]));
                pendingQueue.add(new TimeStamp(Integer.parseInt(tokens[1]), Integer.parseInt(tokens[2])));
                send(acknowledgement);
            }
            else if (tokens[0].equals("Release")) {
                myTimeStamp.setLogicalClockReceive(Integer.parseInt(tokens[1]));
                TimeStamp senderTimeStamp = search(Integer.parseInt(tokens[2]));
                pendingQueue.remove(senderTimeStamp);

                if (acknowledgements == serverInstances - 1 && isSmallest()) {
                    response = execute(clientCommands.poll().getClientCommand());
                    finishExecute(clientCommands.poll().getClientSocket(), response);                   
                }
            }
            else if (tokens[0].equals("Acknowledgement")) {
                myTimeStamp.setLogicalClockReceive(Integer.parseInt(tokens[1]));               
                acknowledgements++;

                if (acknowledgements == serverInstances - 1 && isSmallest()) {
                    response = execute(clientCommands.poll().getClientCommand());
                    finishExecute(clientCommands.poll().getClientSocket(), response);
                }
            }
            else {
                // Handle client command.
                myTimeStamp = new TimeStamp(0, serverID);
                myTimeStamp.setLogicalClockInternal();
                executorService.submit(new ClientListener());
                clientCommands.add(new ClientInformation(command, socket));
            }                   
        }   
        catch (IOException e) {
            e.printStackTrace();
        }                 
    }

    private static boolean isSmallest() {
        TimeStamp timeStamp = search(serverID);
        for (TimeStamp other : pendingQueue) {
            if (timeStamp.compare(other) == 1) {
                return true;
            }
        }
        return false;
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
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }   

    private static void finishExecute(Socket clientSocket, String response) {
        releaseCriticalSection();
        try {
            PrintStream printStream = new PrintStream(clientSocket.getOutputStream());
            printStream.println(response);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    /***************** PARSING AND SAME AS PROJECT 3 **************/

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
            System.out.println("Address for server " + (server + 1) + ": " + servers.get(server).getAddress());
        }
    }

    private static void addNextServerFrom(Scanner scanner) {
        System.out.print("Enter a server in the form '<IP Address>:<Port Number>': ");
        String nextServer = scanner.next();
        String[] serverInformation = nextServer.split(":");
        String IPAddress = serverInformation[0];
        int portNumber = Integer.parseInt(serverInformation[1]);
        servers.add(new InetSocketAddress(IPAddress, portNumber));
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

    public static class ClientInformation {
        private String clientCommand;
        private Socket clientSocket;

        public ClientInformation(String clientCommand, Socket clientSocket) {
            this.clientCommand = clientCommand;
            this.clientSocket = clientSocket;
        }

        public String getClientCommand() {
            return clientCommand;
        }

        public Socket getClientSocket() {
            return clientSocket;
        }
    } 

    public static boolean isServerListening(String host, int port) {
        Socket s = null;
        try {
            s = new Socket(host, port);
            return true;
        }
        catch (Exception e) {
            return false;
        }
        finally {
            if(s != null) {
                try {s.close();}
                catch(Exception e){}
            }
        }
    }
}

