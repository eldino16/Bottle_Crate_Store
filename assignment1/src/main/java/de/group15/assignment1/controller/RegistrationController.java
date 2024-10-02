package de.group15.assignment1.controller;

import javax.validation.Valid;

import de.group15.assignment1.model.Address;
import de.group15.assignment1.model.User;
import de.group15.assignment1.model.UserDTO;
import de.group15.assignment1.service.UserService;
import de.group15.assignment1.service.impl.UserServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import de.group15.assignment1.repository.UserRepository;

import java.util.Collections;

@Slf4j
@Controller
@RequestMapping(value = "/register")
public class RegistrationController {

    private UserServiceImpl userService;
    private UserRepository userRepo;
    private AuthenticationManager authenticationManager;

    @Autowired
    public RegistrationController(UserRepository userRepo, UserServiceImpl userService, AuthenticationManager authenticationManager) {
        this.userRepo = userRepo;
        this.userService = userService;
        this.authenticationManager = authenticationManager;
    }

    @GetMapping
    public String getRegistrationForm(Model model){

        log.info("Show registration page");
        UserDTO blankUser = new UserDTO();
        blankUser.setBillingAddresses(Collections.singletonList(new Address()));
        blankUser.setDeliveryAddresses(Collections.singletonList(new Address()));
        model.addAttribute("registrationForm", blankUser);

        return "register";
    }

    @PostMapping
    public String createUser(Model model, @Valid UserDTO registrationForm, Errors errors) {

        if(errors.hasErrors()) {
            log.info("User registration contained errors: " + registrationForm.toString());
            UserDTO blankUser = new UserDTO();
            blankUser.setBillingAddresses(Collections.singletonList(new Address()));
            blankUser.setDeliveryAddresses(Collections.singletonList(new Address()));
            model.addAttribute("registrationForm", blankUser);
            return "register";
        }
        User user = registrationForm.toUser();
        // first user is admin
        Object lol = userRepo.count();
        if(userRepo.count() == 0) {
            user.setRole("ADMIN");
        } else {
            user.setRole("CUSTOMER");
        }

        user = userService.registerUser(user);
        if(user.getUsername()==null) {
            log.info("User registration contained errors: " + registrationForm.toString());
            UserDTO blankUser = new UserDTO();
            blankUser.setBillingAddresses(Collections.singletonList(new Address()));
            blankUser.setDeliveryAddresses(Collections.singletonList(new Address()));
            model.addAttribute("registrationForm", blankUser);
            model.addAttribute("error", "This username already exists");
            return "register";
        }
        log.info("New user created: " + user.getUsername() + ".");

        String username = user.getUsername();
        String password = registrationForm.getPassword();
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(username, password);
        Authentication authenticatedUser = authenticationManager.authenticate(token);
        SecurityContextHolder.getContext().setAuthentication(authenticatedUser);
        return "redirect:/beverages";
    }
}
