package de.group15.assignment1.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotNull(message = "Street must be set")
    @NotEmpty(message = "Street not there")
    private String street;

    @NotNull(message = "Number must be set")
    @NotEmpty(message = "Number not there")
    private String number;

    @NotNull(message = "Postal code must be set")
    @NotEmpty(message = "Postal code not there")
    @Size(max = 5)
    private String postalcode;
}
