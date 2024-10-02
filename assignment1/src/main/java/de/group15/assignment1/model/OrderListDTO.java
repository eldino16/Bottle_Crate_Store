package de.group15.assignment1.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
/*
This DTO is used to be sent for the pdf gen function
we don't need other OrderItem's attributes
 */
public class OrderListDTO {
    private double price;
    private int quantity;
    private String beverageName;
    private String beveragePic;
    private Long beverageId;
}
