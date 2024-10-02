package de.group15.assignment1.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@NamedEntityGraph(name = "Order.OrderItem", attributeNodes = {
        @NamedAttributeNode("beverage"),
        @NamedAttributeNode("order")
})
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Pattern(regexp = "^[0-9]*$", message = "Only digits are allowed.")
    private String position;

    @DecimalMin(value = "0.01", message = "Order Item Price must be higher than 0")
    private double price;

    @Min(value = 1, message = "Order Item Quantity must be at least 1")
    private int quantity;

    @ManyToOne(fetch = FetchType.LAZY)
    private Beverage beverage;

    @ManyToOne(fetch = FetchType.LAZY)
    private Order order;

    public OrderItem(Beverage beverage, int quantity) {
        this.beverage = beverage;
        setQuantity(quantity);
    }

    public void increaseQuantityBy(int quantity) {
        setQuantity(this.quantity + quantity);
    }

    /**
     * Sets the quantity to @quantity and calculates the new price
     * @param quantity
     */
    public void setQuantity(int quantity) {
        this.quantity = quantity;
        setPrice(beverage.getPrice() * this.quantity);
    }

    /**
     * Checks if two Orderitems contains the same beverages
     * @param object
     * @return true, if the beverage of @object is the same beverage
     */
    @Override
    public boolean equals(Object object) {
        if (object instanceof OrderItem) {
            return this.getBeverage().getId().equals(((OrderItem) object).getBeverage().getId());
        }
        return false;
    }

    @Override
    public String toString() {
        return String.format("OrderItem: Id: %d, Position: %s, Quantity: %d, Price: %f, Beverage: %s", this.id, this.position, this.quantity, this.price, this.beverage.getName());
    }
}
