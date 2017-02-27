/**
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

    synchronized public Order getOrder(int orderToObtain) {
        for (Order order : orderHistory) {
            if (order.getID() == orderToObtain) {
                return order;
            }
        }
        return null;
    }

    synchronized public void addOrder(Order order) {
        orderHistory.add(order);
    }

    synchronized public boolean removeOrder(int orderToRemove) {
        for (Order order : orderHistory) {
            if (order.getID() == orderToRemove) {
                orderHistory.remove(orderToRemove);
                return true;
            }
        }

        return false;
    }

    synchronized public String getUsername() {
        return name;
    }

    synchronized public ArrayList<Order> getOrderHistory() {
        return orderHistory;
    }

    synchronized public boolean hasPlacedOrders() {
        return orderHistory.size() != 0;
    }
}