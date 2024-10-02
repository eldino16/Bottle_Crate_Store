package de.group15.assignment1.service;

import de.group15.assignment1.model.User;
import de.group15.assignment1.model.UserDTO;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import javax.naming.AuthenticationException;

public interface UserService {

    User registerUser(User user);

    User updateUser(String userid, UserDTO userUpdateDTO);

    User getUser(String userid);

    UserDetails loadUserByUsername(String username) throws UsernameNotFoundException;

    Authentication getCurrentUser() throws AuthenticationException;
}
