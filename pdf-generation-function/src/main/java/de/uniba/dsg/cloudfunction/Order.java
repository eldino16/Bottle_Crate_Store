package de.uniba.dsg.cloudfunction;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class Order {
    private Long id;
    private double totalPrice;
    private List<OrderItem> listOfItems;
    private String userEmail;
    private String postalCode;
    private String timeStamp;
}
