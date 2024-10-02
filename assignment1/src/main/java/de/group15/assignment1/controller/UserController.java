package de.group15.assignment1.controller;

import de.group15.assignment1.model.User;
import de.group15.assignment1.model.UserDTO;
import de.group15.assignment1.repository.UserRepository;
import de.group15.assignment1.service.ShoppingCartService;
import de.group15.assignment1.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.naming.AuthenticationException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Controller
//This controller is used to work with User data
public class UserController {

    @Autowired
    UserRepository userRepository;
    @Autowired
    UserService userService;
    private final ShoppingCartService shoppingCartService;

    public UserController(ShoppingCartService shoppingCartService) {
        this.shoppingCartService = shoppingCartService;
    }

    @GetMapping("/usersinfo")
    @PreAuthorize("isAuthenticated()")
    public String getUsers(Model model, @RequestParam String user_id) {
        log.info("Show user info");
        try {
            Authentication userauth = userService.getCurrentUser();
            // Only the user itself or an admin can view the user info
            if (userauth.getName().equals(user_id) || userauth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                List<String> user_ids = new ArrayList<String>() {{
                    add(user_id);
                }};
                List<User> usersinfo = this.userRepository.findAllById(user_ids);
                model.addAttribute("usersinfo", usersinfo);
                return "userInfo";
            }
            else {
                model.addAttribute("message", "The requested user isn't the logged in user");
                return "error";
            }
        } catch (AuthenticationException e) {
            e.printStackTrace();
        }
        model.addAttribute("message", "Please log in.");
        return "error";
    }

    @GetMapping("/usersinfo/address")
    @PreAuthorize("isAuthenticated()")
    public String getUsers(Model model) {
        log.info("Show user info");
        try {
            Authentication userauth = userService.getCurrentUser();
            String user_id = userauth.getName();
            List<String> user_ids = new ArrayList<String>() {{
                add(user_id);
            }};
            List<User> usersinfo = this.userRepository.findAllById(user_ids);
            model.addAttribute("usersinfo", usersinfo);
            return "userInfo";
        } catch (AuthenticationException e) {
            e.printStackTrace();
        }
        model.addAttribute("message", "Please log in.");
        return "error";
    }

    @PostMapping("/usersinfo/updateaddress/{user_id}")
    @PreAuthorize("isAuthenticated()")
    public String updateAddress(Model model, @PathVariable(value = "user_id") String user_id, UserDTO userdto) {
        try {
            Authentication user = userService.getCurrentUser();
            // Only the user itself or an admin can edit the user info
            if (user.getName().equals(user_id) || user.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                userService.updateUser(user_id, userdto);
                return "redirect:/usersinfo?user_id="+user_id;
            }
            else {
                model.addAttribute("message", "The requested user isn't the logged in user");
                return "error";
            }
        } catch (AuthenticationException e) {
            e.printStackTrace();
        }
        model.addAttribute("message", "Please log in.");
        return "error";
    }

    @GetMapping("/userProfile")
    @PreAuthorize("isAuthenticated()")
    public String getUserProfile(Model model){

        log.info("Show User Profile");
        model.addAttribute("userprofile", getUsers(model));

        return "userProfile";
    }

    @PostMapping("/usersinfo/updateProfile/{user_id}")
    @PreAuthorize("isAuthenticated()")
    public String updateProfile(Model model, @PathVariable(value = "user_id") String user_id, UserDTO userdto) {
        try {
            Authentication user = userService.getCurrentUser();
            // Only the user itself or an admin can edit the user info
            if (user.getName().equals(user_id) || user.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                userService.updateUser(user_id, userdto);
                log.info("Profile updated successfully.");
                return "redirect:/userProfile";
            }
            else {
                model.addAttribute("message", "The requested user isn't the logged in user");
                return "error";
            }
        } catch (AuthenticationException e) {
            e.printStackTrace();
        }
        model.addAttribute("message", "Please log in.");
        return "error";
    }
}
