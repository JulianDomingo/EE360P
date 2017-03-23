/*
 * Julian Domingo : jad5348
 * Alec Bargas : apb973
 */

public class Item {
    private int quantity;

    private String itemName;

    public Item(String itemName, int quantity) {
        this.itemName = itemName;
        this.quantity = quantity;
    }

    public String getItemName() {
        return itemName;
    }

    public int getCurrentQuantity() {
        return quantity;
    }

    public void purchaseQuantityOf(int amount) {
        quantity -= amount;
    }

    public void returnQuantityOf(int amount) {
        quantity += amount;
    }
}    
