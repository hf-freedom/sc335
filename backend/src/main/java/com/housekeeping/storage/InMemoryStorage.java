package com.housekeeping.storage;

import com.housekeeping.entity.Aunt;
import com.housekeeping.entity.Order;
import com.housekeeping.entity.User;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryStorage {
    private final Map<String, User> users = new ConcurrentHashMap<>();
    private final Map<String, Aunt> aunts = new ConcurrentHashMap<>();
    private final Map<String, Order> orders = new ConcurrentHashMap<>();

    public void saveUser(User user) {
        users.put(user.getId(), user);
    }

    public User getUser(String id) {
        return users.get(id);
    }

    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    public void saveAunt(Aunt aunt) {
        aunts.put(aunt.getId(), aunt);
    }

    public Aunt getAunt(String id) {
        return aunts.get(id);
    }

    public List<Aunt> getAllAunts() {
        return new ArrayList<>(aunts.values());
    }

    public void saveOrder(Order order) {
        orders.put(order.getId(), order);
    }

    public Order getOrder(String id) {
        return orders.get(id);
    }

    public List<Order> getAllOrders() {
        return new ArrayList<>(orders.values());
    }

    public void removeOrder(String id) {
        orders.remove(id);
    }
}
