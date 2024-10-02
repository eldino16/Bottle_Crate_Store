package de.group15.assignment1.service.impl;

import de.group15.assignment1.model.OrderItem;
import de.group15.assignment1.repository.BottleRepository;
import de.group15.assignment1.service.ShoppingCartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.*;

@Service
@Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
@Transactional
public class ShoppingCartServiceImpl implements ShoppingCartService {

    private ArrayList<OrderItem> items = new ArrayList<>();

    @Override
    public void addItem(OrderItem item) {
        if (items.contains(item)) {
            OrderItem existing = items.get(items.indexOf(item));
            existing.increaseQuantityBy(item.getQuantity());
        } else {
            items.add(item);
        }
    }

    @Override
    public void updateItem(OrderItem item) {
        if (items.contains(item)) {
            OrderItem existing = items.get(items.indexOf(item));
            if (item.getQuantity() == 0) {
                items.remove(item);
            } else {
                existing.setQuantity(item.getQuantity());
            }
        }
    }

    @Override
    public List<OrderItem> getItemsInCart() {
        return items;
    }

    @Override
    public double getTotal() {
        double total = items.stream().mapToDouble(OrderItem::getQuantity).sum();
        return total;
    }

    @Override
    public void clearAllItems() {
        items.clear();
    }
}
