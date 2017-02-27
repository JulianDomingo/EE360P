/**
 * Julian Domingo : jad5348
 * Alec Bargas : apb973
 */

public class Item {
    private int quantity;

    private String itemName;

    Item(String itemName, int quantity) {
        this.itemName = itemName;
        this.quantity = quantity;
    }

    synchronized String getItemName() {
        return itemName;
    }

    synchronized int getCurrentQuantity() {
        return quantity;
    }

    synchronized void purchaseQuantityOf(int amount) {
        quantity -= amount;
    }

    synchronized void returnQuantityOf(int amount) {
        quantity += amount;
    }
}