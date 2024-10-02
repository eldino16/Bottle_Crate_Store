package de.group15.assignment1.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
/*
This DTO is used to be sent for the pdf gen function
we don't need other Order's attributes
 */
public class OrderDTO {
    private Long id;
    private double totalPrice;
    private List<OrderListDTO> listOfItems;
    private String userEmail;
    private String postalCode;
    private String timeStamp;
}
