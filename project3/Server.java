/**
 * Julian Domingo : jad5348
 * Alec Bargas : apb973
 */

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.lang.StringBuilder;

public class Server {
	private static ExecutorService es;
	private static ArrayList<Item> inventory;
	private static ArrayList<User> clients;
  
  	public Server() {
  		inventory = new ArrayList<Item>();
  		clients = new ArrayList<User>();
  		es = Executors.newCachedThreadPool(); 
  	} 

  	// Input arguments are as follows:
  	// 1. TCP Port Number
  	// 2. UDP Port Number
  	// 3. Input File
	public static void main (String[] args) {
		int portNumberTCP
		int portNumberUDP;

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
			DatagramSocket dataSocket = new DatagramSocket(portNumber);
			byte[] receivingBuffer = new byte[1024];
			byte[] sendingBuffer = new byte[1024];
			while (true) {
				receivingPacket = new DatagramPacket(receivingBuffer, receivingBuffer.length);
				dataSocket.receive(receivingPacket);
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
				dataSocket.send(sendingPacket);
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
  
	public static String execute(String command) {
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
				throw new IllegalArgumentException("Invalid command '" + arguments[0] + "'.");
		}
	}
  
	private static String purchase(String userName, String productName, int quantity) {
		User user = findUserThrough(userName);

		if (isNewCustomer(user)) {
			clients.add(new User(userName));
			user = clients.get(clients.size());
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

		int orderID = obtainOrderID(user);

		return "Your order has been placed, " + orderID + " " + userName + " " + productName + " " + quantity + ".";
	}
  
	private static String cancel(int orderID) {
		User user = findUserThrough(orderID);
		if (user == null) {
			return orderID + " not found, no such order.";
		}

		Order cancelledOrder = user.getOrder(orderID);

		removeItemFrom(cancelledOrder);
		user.removeOrder(orderID);

		return "Order " + orderID + " is cancelled.";
	}

	private static String search(String userName) {
		User user = findUserThrough(userName);

		if (!user.hasPlacedOrders()) { 
			return "No order found for " + userName + ".";
		}

		StringBuilder orderList = new StringBuilder("");

		for (int order = 0; order < user.getOrderHistory().size(); order++) {
			orderList.append(Integer.toString(user.getOrderHistory().get(order).getId()));
			orderList.append(", ");
			orderList.append(user.getOrderHistory().get(order).getProductName())
			orderList.append(", ");
			orderList.append(Integer.toString(user.getOrderHistory().get(order).getQuantity()));
			orderList.append("\n");
		}

		return orderList.toString();
	}
  
	private static String list() {
		StringBuilder inventoryString = new StringBuilder("");

		for (Item item : inventory) {
			inventoryString.append(item.getItemName());
			inventoryString.append(" ");
			inventoryString.append(Integer.toString(item.getCurrentQuantity()));
			inventoryString.append("\n");
		}

		return inventoryString.toString();
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

	private void addItemsToInventoryFrom(String fileName) {
		BufferedReader bufferedReader = new BufferedReader(new FileReader(fileName));
		String inputLine = bufferedReader.readLine();
      
		while (inputLine != null) {
	    	String[] arguments = fileRead.split(" ");
	    	Item item = formItemFrom(arguments);
	    	inventory.add(item);
	    	inputLine = bufferedReader.readLine();
		}

		bufferedReader.close();
	}

	private Item findItem(String productName) {
		for (Item item : inventory) {
			if (item.getItemName().equals(productName)) {
				return item;
			}
		}
		return null;
	}

	private int obtainOrderID(User user) {
		Order order = user.getOrderHistory().get(user.getOrderHistory().size());
		return order.getID();
	}

	private boolean isNewCustomer(String userName) {
		return userName == null;
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

	private void removeItemFrom(Order cancelledOrder) {
		for (Item item : inventory) {
			if (item.getItemName().equals(cancelledOrder.getProductName())) {
				item.returnQuantityOf(cancelledOrder.getQuantity());
			}
		}
	}

	private User findUserThrough(String userName) {
		for (User user : clients) {
			if (user.getusername.equals(userName)) {
				return user;
			}
		}
		throw new Exception("User does not exist.");
	}

	private Item formItemFrom(String[] arguments) {
		return new Item(arguments[0], Integer.parseInt(arguments[1]));
	}

	private Thread makeTCPThread(portNumber) {
		return new Thread() {
			public void run() {
				instantiateTCPServer(portNumber);
			}
		}
	}

	private Thread makeUDPThread(portNumber) {
		return new Thread() {
			public void run() {
				instantiateUDPServer(portNumber);
			}
		}
	}

	private void instantiateTCPServer(int portNumber) {
		try {
			TCPServer(portNumber);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void instantiateUDPServer(int portNumber) {
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