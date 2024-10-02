package de.group15.assignment1.model;

import lombok.*;

import javax.persistence.Entity;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Bottle extends Beverage {
    @DecimalMin(value = "0.0", inclusive = false, message = "Volume must be higher than 0")
    private double volume;

    private boolean isAlcoholic = false;

    private double volumePercent;

    @NotNull(message = "Supplier must be set")
    @NotEmpty(message = "Please enter Supplier")
    private String supplier;

    /**
     * Sets the @volumePercent to the parameter percent.
     * If @percent is greater than 0 @isAlcoholic will be set to true
     * @param percent the alcohol by volume
     */
    public void setVolumePercent(double percent) {
        isAlcoholic = percent > 0;
        volumePercent = percent;
    }

}