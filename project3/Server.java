/**
 * Julian Domingo : jad5348
 * Alec Bargas : apb973
 */

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class Server {
    private static ExecutorService es;
    private static ArrayList<Item> inventory;
    private static ArrayList<User> clients;

    // Input arguments are as follows:
    // args[0] : TCP Port Number
    // args[1] : UDP Port Number
    // args[2] : Input File
    public static void main(String[] args) {
        int portNumberTCP;
        int portNumberUDP;
        inventory = new ArrayList<Item>();
        clients = new ArrayList<User>();
        es = Executors.newCachedThreadPool();

        portNumberTCP = Integer.parseInt(args[0]);
        portNumberUDP = Integer.parseInt(args[1]);
        String fileName = args[2];

        parse(fileName);

        Thread threadForTCP = makeTCPThread(portNumberTCP);
        Thread threadForUDP = makeUDPThread(portNumberUDP);

        threadForTCP.start();
        threadForUDP.start();
    }

    private static void TCPServer(int portNumber) throws IOException {
        String command;
        Scanner scanner;
        PrintStream printStream;

        try {
            ServerSocket serverSocket = new ServerSocket(portNumber);
            while (true) {
                Socket connectionSocket = serverSocket.accept();
                scanner = new Scanner(connectionSocket.getInputStream());
                printStream = new PrintStream(connectionSocket.getOutputStream());
                command = scanner.nextLine();
                Command callableCommand = new Command(command);
                Future<String> result = es.submit(callableCommand);
                String futureResult = result.get();
                printStream.println(futureResult);
            }
        }
        catch(IOException e) {
            e.printStackTrace();
        }
        catch(InterruptedException e) {
            e.printStackTrace();
        }
        catch(ExecutionException e) {
            e.printStackTrace();
        }
    }

    private static void UDPServer(int portNumber) throws IOException {
        String command;
        DatagramPacket receivingPacket;
        DatagramPacket sendingPacket;

        try {
            DatagramSocket datagramSocket = new DatagramSocket(portNumber);
            byte[] receivingBuffer = new byte[1024];
            byte[] sendingBuffer = new byte[1024];
            while (true) {
                receivingPacket = new DatagramPacket(receivingBuffer, receivingBuffer.length);
                datagramSocket.receive(receivingPacket);
                command = new String(receivingPacket.getData(), 0, receivingPacket.getLength());
                Command callableCommand = new Command(command);
                Future<String> result = es.submit(callableCommand);
                String futureResult = result.get();
                sendingBuffer = new byte[futureResult.length()];
                sendingBuffer = futureResult.getBytes();
                sendingPacket = new DatagramPacket(sendingBuffer,
                        sendingBuffer.length,
                        receivingPacket.getAddress(),
                        receivingPacket.getPort());
                datagramSocket.send(sendingPacket);
            }
        }
        catch (SocketException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    public static synchronized String execute(String command) {
        String arguments[] = command.split(" ");

        switch (arguments[0]) {
            case "purchase":
                return purchase(arguments[1], arguments[2], Integer.parseInt(arguments[3]));
            case "cancel":
                return cancel(Integer.parseInt(arguments[1]));
            case "search":
                return search(arguments[1]);
            case "list":
                return list();
            default:
                return "Invalid command: '" + arguments[0] + "'.";
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

        int orderID = newOrder.getID();

        return "Your order has been placed, " + orderID + " " + userName + " " + productName + " " + quantity + ".";
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

    public static void parse(String fileName) {
        try {
            addItemsToInventoryFrom(fileName);
        }
        catch (FileNotFoundException e) {
            System.out.println("File does not exist.");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void addItemsToInventoryFrom(String fileName) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new FileReader(fileName));
        String inputLine = bufferedReader.readLine();

        while (inputLine != null) {
            String[] arguments = inputLine.split(" ");
            Item item = formItemFrom(arguments);
            inventory.add(item);
            inputLine = bufferedReader.readLine();
        }

        bufferedReader.close();
    }

    private static Item findItem(String productName) {
        for (Item item : inventory) {
            if (item.getItemName().equals(productName)) {
                return item;
            }
        }
        return null;
    }

    private static int obtainOrderID(User user) {
        Order order = user.getOrderHistory().get(user.getOrderHistory().size());
        return order.getID();
    }

    private static boolean isNewCustomer(User user) {
        return user == null;
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

    private static void removeItemFrom(Order cancelledOrder) {
        for (Item item : inventory) {
            if (item.getItemName().equals(cancelledOrder.getProductName())) {
                item.returnQuantityOf(cancelledOrder.getQuantity());
            }
        }
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

    private static User findUserThroughName(String userName) {
        for (User user : clients) {
            if (user.getUsername().equals(userName)) {
                return user;
            }
        }
        return null;
    }

    private static Item formItemFrom(String[] arguments) {
        return new Item(arguments[0], Integer.parseInt(arguments[1]));
    }

    private static Thread makeTCPThread(int portNumber) {
        return new Thread() {
            public void run() {
                beginTCPServer(portNumber);
            }
        };
    }

    private static Thread makeUDPThread(int portNumber) {
        return new Thread() {
            public void run() {
                beginUDPServer(portNumber);
            }
        };
    }

    private static void beginTCPServer(int portNumber) {
        try {
            TCPServer(portNumber);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void beginUDPServer(int portNumber) {
        try {
            UDPServer(portNumber);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    static public class Command implements Callable<String> {
        private String command;

        public Command(String command) {
            this.command = command;
        }

        public String getString() {
            return command;
        }

        @Override
        public String call() throws Exception {
            return execute(command);
        }
    }
}
