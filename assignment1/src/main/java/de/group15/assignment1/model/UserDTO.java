package de.group15.assignment1.model;

import de.group15.assignment1.validator.Since;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.Column;
import javax.validation.constraints.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
/*
This DTO is used to update user's address.
 */
public class UserDTO {

    @NotNull(message = "Password must be set")
    @NotEmpty(message = "Please enter password")
    private String password;

    @NotNull(message = "Please enter birth date")
    @Past(message = "Birth date should be less than current date!!")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Since("01.01.1900")
    private LocalDate birthday;

    @Email(message = "Enter valid e-mail" )
    private String email;

    @NotNull(message = "Username must be set")
    @NotEmpty(message = "Please enter Username")
    @Column(unique = true)
    private String username;

    @Setter
    private List<Address> deliveryAddresses;

    public Set<Address> getDeliveryAddressesAsSet() {
        return new HashSet<>(deliveryAddresses);
    }


    @Setter
    private List<Address> billingAddresses;

    public Set<Address> getBillingAddressesAsSet() {
        return new HashSet<>(billingAddresses);
    }

    public User toUser() {
        return new User(username, password, "",birthday,email,null,getBillingAddressesAsSet(),getDeliveryAddressesAsSet());
    }
}
