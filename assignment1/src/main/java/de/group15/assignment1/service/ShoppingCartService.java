package de.group15.assignment1.service;

import de.group15.assignment1.model.Order;

import de.group15.assignment1.model.OrderItem;

import java.util.List;


public interface ShoppingCartService {
    void addItem(OrderItem item);

    void updateItem(OrderItem item);

    List<OrderItem> getItemsInCart();

    double getTotal();

    void clearAllItems();
}
