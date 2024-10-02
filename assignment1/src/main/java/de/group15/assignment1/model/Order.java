package de.group15.assignment1.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "order_table")
@Data
@AllArgsConstructor
@NoArgsConstructor
@NamedEntityGraph(name = "Order.Order", attributeNodes = {
        @NamedAttributeNode("customer"),
        @NamedAttributeNode("items")
})
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @DecimalMin(value = "0.01", message = "Price must be higher than 0")
    private double price;

    @ManyToOne(fetch = FetchType.LAZY)
    private User customer;

    @OneToMany(cascade = CascadeType.PERSIST)
    @Size(min = 1)
    private List<OrderItem> items;

    public void addOrderItem(OrderItem item) {
        if (items == null) {
            items = new ArrayList<>();
        }
        item.setOrder(this);
        String pos = String.valueOf(items.size() + 1);
        item.setPosition(pos);
        items.add(item);
    }

    public double priceTotal(List<OrderItem> orderItems) {
        return orderItems.stream().mapToDouble(OrderItem::getPrice).sum();
    }
}
