/**
 * Julian Domingo : jad5348
 * Alec Bargas : apb973
 */

public class Order {
	private int orderID;
	private int quantity;

	static int currentID = 0;

	private String productName;

	public Order(String productName, int quantity) {
		currentID++;
		this.orderID = currentID;
		this.productName = productName;
		this.quantity = quantity;
	}

	public int getID() {
		return orderID;
	}

	public String getProductName(){
		return productName;
	}

	public int getQuantity(){
		return quantity;
	}
}