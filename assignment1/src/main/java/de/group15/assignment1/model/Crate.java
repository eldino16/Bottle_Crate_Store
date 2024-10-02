package de.group15.assignment1.model;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@NamedEntityGraph(name = "Beverage.Crate", attributeNodes = {
        @NamedAttributeNode("bottle")
})
public class Crate extends Beverage {
    @Min(value = 1, message = "The number of bottles must be higher than 0")
    private int noOfBottles;

    @ManyToOne(fetch = FetchType.LAZY)
    @NotNull
    private Bottle bottle;
}