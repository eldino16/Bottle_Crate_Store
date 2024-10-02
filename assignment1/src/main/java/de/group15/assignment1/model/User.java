package de.group15.assignment1.model;

import de.group15.assignment1.validator.Since;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "user_table")
@Data
@AllArgsConstructor
@NoArgsConstructor
@NamedEntityGraph(name = "User.User", attributeNodes = {
        @NamedAttributeNode("billingaddresses"),
        @NamedAttributeNode("deliveryaddresses"),
        @NamedAttributeNode("orders")}
        )
public class User implements UserDetails {

    @Id
    @NotNull(message = "Username must be set")
    @NotEmpty(message = "Please enter Username")
    @Column(unique = true)
    private String username;

    @NotNull(message = "Password must be set")
    @NotEmpty(message = "Please enter password")
    private String password;

    private String role;

    @NotNull(message = "Please enter birth date")
    @Past(message = "Birth date should be less than current date!!")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Since("01.01.1900")
    private LocalDate birthday;

    @Email(message = "Enter valid e-mail" )
    private String email;

    @OneToMany(cascade = CascadeType.PERSIST, mappedBy = "customer")
    private List<Order> orders;

    @ManyToMany(cascade = CascadeType.ALL)
    private Set<Address> billingaddresses;

    @ManyToMany(cascade = CascadeType.ALL)
    private Set<Address> deliveryaddresses;


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Arrays.asList(new SimpleGrantedAuthority("ROLE_" +this.role));
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String toString() {
        return String.format("User: Username: %s, Role: %s, Birthday: %s", this.username, this.role, this.birthday);
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof User) {
            if (this.getUsername() == null && ((User) object).getUsername() == null) {
                return true;
            }
            return this.getUsername().equals(((User) object).getUsername());
        }
        return false;
    }
}
