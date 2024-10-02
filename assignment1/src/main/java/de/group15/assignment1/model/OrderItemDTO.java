package de.group15.assignment1.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
/*
This DTO is used to update shopping Cart
in shopping Cart, we don't need other OrderItem's attributes
 */
public class OrderItemDTO {

    private int quantity;
    private Long beverageId;

}
