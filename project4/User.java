/*
 * Julian Domingo : jad5348
 * Alec Bargas : apb973
 */

import java.util.*;

public class User {
    private String name;
    private ArrayList<Order> orderHistory;

    public User(String name) {
        this.name = name;
        orderHistory = new ArrayList<Order>();
    }

    public Order getOrder(int orderToObtain) {
        for (Order order : orderHistory) {
            if (order.getID() == orderToObtain) {
                return order;
            }
        }
        return null;
    }
    
    public void addOrder(Order order) {
        orderHistory.add(order);
    }
        
    public boolean removeOrder(int orderToRemove) {
        for (Order order : orderHistory) {
            if (order.getID() == orderToRemove) {
                orderHistory.remove(orderHistory.indexOf(order));
                return true;
            }
        }
        return false;
    }

    public String getUsername() {
        return name;
    }

    public ArrayList<Order> getOrderHistory() {
        return orderHistory;
    }

    public boolean hasPlacedOrders() {
        return orderHistory.size() != 0;
    }                       
}
