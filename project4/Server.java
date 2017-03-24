/*
 * Julian Domingo : jad5348
 * Alec Bargas : apb973
 */

import java.util.*;
import java.util.concurrent.*;
import java.net.*;
import java.io.*;

public class Server {
    private ArrayList<InetSocketAddress> servers;
    private ArrayList<Integer> timedOutServers;
    private ExecutorService executorService;
    
    private ArrayList<Item> inventory;
    private ArrayList<User> clients;
    
    private int serverID;

    private int serverInstances;

    private TimeStamp myTimeStamp;
    private ArrayList<TimeStamp> pendingQueue = new ArrayList<TimeStamp>();

    private PriorityQueue<ClientInformation> clientCommands = new PriorityQueue<ClientInformation>();
 
    public static void main (String[] args) throws IOException {
    	Server server = new Server();
        Scanner scanner = new Scanner(System.in);
        server.parseServerID(scanner);
        server.myTimeStamp = new TimeStamp(1,server.serverID);
        server.serverInstances = scanner.nextInt();
        String inventoryPath = scanner.next();
        scanner.nextLine();
        server.inventory = new ArrayList<Item>();
        server.clients = new ArrayList<User>();
        server.timedOutServers = new ArrayList<Integer>();    
        server.servers = new ArrayList<InetSocketAddress>(server.serverInstances);

        server.executorService = Executors.newCachedThreadPool();

        System.out.println("[DEBUG] my ID: " + server.serverID);
        System.out.println("[DEBUG] serverInstances: " + server.serverInstances);
        System.out.println("[DEBUG] inventory path: " + inventoryPath);
        
        server.addServers(scanner);   

        server.parse(inventoryPath);

        server.submitNewServerProcess();
    }  

    private void requestCriticalSection() {
        pendingQueue.add(new TimeStamp(myTimeStamp.getLogicalClock(), myTimeStamp.getPID()));
        String request = "Request:" + myTimeStamp.getLogicalClock() + ":" + serverID;
        send(request);
    }

    private void releaseCriticalSection() {
        String release = "Release:" + myTimeStamp.getLogicalClock() + ":" + serverID;
        send(release);
    }

    //TODO: add StillAlive to check for crash between closing of last and opening of next socket
    private void send(String message) {
        for (int server = 1; server < serverInstances + 1; server++) {
            if (server != serverID && !timedOutServers.contains(server)) {
                try {
                    Socket clientSocket = new Socket(servers.get(server - 1).getHostName(),servers.get(server - 1).getPort());
                    clientSocket.setSoTimeout(100);
                    PrintStream printStream = new PrintStream(clientSocket.getOutputStream());
                    Scanner scanner = new Scanner(clientSocket.getInputStream());
                    myTimeStamp.setLogicalClockSend();
                    printStream.println(message);
                    printStream.flush();
	                while(scanner.nextLine().equals("alive"));
                    printStream.close();
                    scanner.close();
                    clientSocket.close();
                }
                catch(NoSuchElementException e)
                {
                	deprecateServer(server);
                }
                catch(SocketTimeoutException e)
                {
                	deprecateServer(server);
                }
                catch(ConnectException e)
                {
                	deprecateServer(server);
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }            
        }
    }

    private void submitNewServerProcess() {    
        try {
            ServerSocket serverSocket = new ServerSocket(servers.get(serverID - 1).getPort());
            while(true)
            {
                Socket socket = serverSocket.accept();
                executorService.submit(new StillAlive(socket));
                executorService.submit(new ServerTask(socket));
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }           
    }
    
    public class StillAlive implements Runnable {
        private Socket serverSocket;

        StillAlive(Socket serverSocket) {
            this.serverSocket = serverSocket;
        }

        public void run() {
        	try {
        		PrintStream printer = new PrintStream(serverSocket.getOutputStream());
				while(serverSocket.isClosed() == false)
				{
					printer.println("alive");
					printer.flush();
					TimeUnit.MILLISECONDS.sleep(50);
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
        }
    }
    
    public class ServerTask implements Runnable {
        private Socket serverSocket;

        ServerTask(Socket serverSocket) {
            this.serverSocket = serverSocket;
        }

        public void run() {
            serviceServerTask(serverSocket);
        }
    }

    private void serviceServerTask(Socket socket) {
        String command;
        try {
            Scanner scanner = new Scanner(socket.getInputStream());
            command = scanner.nextLine();
            String[] tokens = command.split(":");
            if (tokens[0].equals("Request")) {
                String acknowledgement = "Acknowledgement";
                myTimeStamp.setLogicalClockReceive(Integer.parseInt(tokens[1]));
                PrintStream printer = new PrintStream(socket.getOutputStream());
                myTimeStamp.setLogicalClockSend();
                printer.println(acknowledgement);
                printer.flush();
                pendingQueue.add(new TimeStamp(Integer.parseInt(tokens[1]), Integer.parseInt(tokens[2])));
            }
            else if (tokens[0].equals("Release")) {
                PrintStream printer = new PrintStream(socket.getOutputStream());
                printer.println("recieved");
                printer.flush();
                myTimeStamp.setLogicalClockReceive(Integer.parseInt(tokens[1]));
                TimeStamp senderTimeStamp = search(Integer.parseInt(tokens[2]));
                pendingQueue.remove(senderTimeStamp);
                checkCriticalSection();
            }
            else if(tokens[0].equals("Update")){
                PrintStream printer = new PrintStream(socket.getOutputStream());
                printer.println("recieved");
                printer.flush();
            	myTimeStamp.setLogicalClockReceive(Integer.parseInt(tokens[1]));
            	execute(tokens[3]);
            }
            else {
                // Handle client command.
                myTimeStamp.setLogicalClockInternal();
                clientCommands.add(new ClientInformation(command, socket));
                requestCriticalSection();
                checkCriticalSection();
            }
        	scanner.close();
        }
        catch (IOException e) {

        }
    }
    
    private void checkCriticalSection(){
    	String response;
        if (isSmallest()) {
        	ClientInformation client = clientCommands.poll();
            response = execute(client.getClientCommand());
            String update = "Update:" + myTimeStamp.getLogicalClock()  + ":" + serverID + ":" + client.getClientCommand();
            send(update);
            finishExecute(client.getClientSocket(), response);
        }
    }

    private void finishExecute(Socket clientSocket, String response) {
        releaseCriticalSection();
		try {
			PrintStream printStream = new PrintStream(clientSocket.getOutputStream());
	        printStream.println(response);
	        printStream.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    
    
    
    
    
    
    
    private boolean isSmallest() {
        TimeStamp timeStamp = search(serverID);
        if(timeStamp == null)
        {
        	return false;
        }
        for (TimeStamp other : pendingQueue) {
            if (timeStamp.compare(other) == -1) {
                return false;
            }
        }
        return true;
    }
    
    private TimeStamp search(int serverID) {
        for (TimeStamp timeStamp : pendingQueue) {
            if (timeStamp.getPID() == serverID) {
                return timeStamp;
            }
        }
        return null;
    }
    
    private void deprecateServer(int serverID) {   
        timedOutServers.add(serverID);
        remove(serverID);
    }
    
    private void remove(int serverID) {
        TimeStamp timeStampToRemove = search(serverID);
        while (timeStampToRemove != null) {
            pendingQueue.remove(timeStampToRemove);
            timeStampToRemove = search(serverID);
        }
    }
    
    private void parseServerID(Scanner scanner) {
        serverID = scanner.nextInt();
    }  

    /***************** PARSING AND SAME AS PROJECT 3 **************/

    private String execute(String command) {
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

    private String purchase(String userName, String productName, int quantity) {
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

    private String cancel(int orderID) {
        User user = findUserThroughID(orderID);
            
        if (isNewCustomer(user)) {
            return orderID + " not found, no such order.";
        }

        Order cancelledOrder = user.getOrder(orderID);

        removeItemFrom(cancelledOrder);
        user.removeOrder(orderID);

        return "Order " + orderID + " is cancelled.";        
    }

    private String list() {
        String inventoryString = "";

        for (Item item : inventory) {
            inventoryString += item.getItemName();
            inventoryString += " ";
            inventoryString += Integer.toString(item.getCurrentQuantity());
            inventoryString += "$";
        }

        return inventoryString;
    }

    private String search(String userName) {
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

    private void parse(String path) {
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

    private void addItemsToInventoryFrom(String path) throws IOException {
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

    private Item formItemFrom(String[] arguments) {
        return new Item(arguments[0], Integer.parseInt(arguments[1]));
    }        

    private void addServers(Scanner scanner) {
        for (int server = 1; server < serverInstances + 1; server++) {
            addNextServerFrom(scanner);
            System.out.println("Address for server " + server + ": " + servers.get(server - 1).getAddress());
        }
    }

    private void addNextServerFrom(Scanner scanner) {
        String[] serverInformation = scanner.nextLine().split(":");
        String IPAddress = serverInformation[0];
        int portNumber = Integer.parseInt(serverInformation[1]);
        servers.add(new InetSocketAddress(IPAddress, portNumber));
    }

    private User findUserThroughName(String userName) {
        for (User user : clients) {
            if (user.getUsername().equals(userName)) {
                return user;
            }
        }
        return null;
    }

    private User findUserThroughID(int orderID) {
        for (User user : clients) {
            for (Order order : user.getOrderHistory()) {
                if (order.getID() == orderID) {
                    return user;
                }
            }
        }
        return null;
    }    

    private boolean isNewCustomer(User user) {
        return user == null;
    }

    private void removeItemFrom(Order cancelledOrder) {
        for (Item item : inventory) {
            if (item.getItemName().equals(cancelledOrder.getProductName())) {
                item.returnQuantityOf(cancelledOrder.getQuantity());
            }
        }
    }
    
    private Item findItem(String productName) {
        for (Item item : inventory) {
            if (item.getItemName().equals(productName)) {
                return item;
            }
        }
        return null;
    }

    private boolean inventoryHasEnoughOf(String productName, int desiredQuantity) {
        for (Item item : inventory) {
            if (item.getItemName().equals(productName) && 
                item.getCurrentQuantity() < desiredQuantity) 
            {
                return false;
            }
        }
        return true;
    }

    private boolean existsInInventory(String productName) {
        for (Item item : inventory) {
            if (item.getItemName().equals(productName)) {
                return true;
            }
        }
        return false;
    }

    public class ClientInformation {
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
}
