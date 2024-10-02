package de.group15.assignment1.service.impl;

import de.group15.assignment1.model.User;
import de.group15.assignment1.model.UserDTO;
import de.group15.assignment1.repository.UserRepository;
import de.group15.assignment1.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.naming.AuthenticationException;
import java.util.Optional;


@Service
public class UserServiceImpl implements UserService, UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    public User registerUser(User user) {
        if (!userRepository.existsByUsername(user.getUsername())) {
            user.setPassword(new BCryptPasswordEncoder(11).encode(user.getPassword()));
            return userRepository.save(user);
        }
        return new User();
    }


    @Override
    public User updateUser(String userid, UserDTO userUpdateDTO) {
        if (userRepository.findById(userid).isPresent()) {
            User existingUser = userRepository.findById(userid).get();

            existingUser.setBillingaddresses(userUpdateDTO.getBillingAddressesAsSet());
            existingUser.setDeliveryaddresses(userUpdateDTO.getDeliveryAddressesAsSet());
            return userRepository.save(existingUser);
        }
        return null;
    }

    @Override
    public User getUser(String userid) {
        if (userRepository.findById(userid).isPresent()) {
            return userRepository.getUserWithEntitiesByUsername(userid).get();
        }
        return null;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> user = userRepository.getUserWithEntitiesByUsername(username);

        if (user.isPresent()) {
            UserDetails details = user.get();
            return details;
        }

        throw new UsernameNotFoundException("User '" + username + "' not found!");
    }

    @Override
    public Authentication getCurrentUser() throws AuthenticationException{
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication == null || authentication instanceof AnonymousAuthenticationToken)) {
            return authentication;
        }
        throw new AuthenticationException("No user logged in");
    }
}
