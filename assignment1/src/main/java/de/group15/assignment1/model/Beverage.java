package de.group15.assignment1.model;

import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.*;

@Data
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public abstract class Beverage {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be higher than 0")
    private double price;

    @NotNull(message = "Name must be set")
    @NotEmpty(message = "Please enter Name")
    @Pattern(regexp = "^[a-zA-Z0-9\\s]*$", message = "Only letters and numbers are allowed")
    private String name;

    @Pattern(regexp = "(https:\\/\\/).*\\.(?:jpg|gif|png)", message = "Must be a valid URL to a picture.")
    private String pic;

    @Min(value = 0, message = "The stock supply can not be negative")
    private int inStock = 0;
}