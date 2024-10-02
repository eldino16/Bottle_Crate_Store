package de.group15.assignment1.controller;

import de.group15.assignment1.model.Order;
import de.group15.assignment1.model.User;
import de.group15.assignment1.repository.OrderRepository;
import de.group15.assignment1.repository.UserRepository;
import de.group15.assignment1.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.naming.AuthenticationException;
import java.util.List;
import java.util.Optional;

@Slf4j
@Controller
@RequestMapping(value = "/orders")
public class OrdersController {
    @Autowired
    OrderRepository orderRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserService userService;

    //view list of all orders in database
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public String viewListOfOrders(Model model) {
        log.info("User viewing the List of All Orders");

        try {
            Authentication auth = userService.getCurrentUser();
            if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                List<Order> orderList = orderRepository.findAll();
                model.addAttribute("orders", orderList);
                return "ordersList";
            }
            else {
                Optional<User> user = userRepository.getUserWithEntitiesByUsername(auth.getName());
                if (user.isPresent()) {
                    List<Order> orderList = user.get().getOrders();
                    model.addAttribute("orders", orderList);
                    return "ordersList";
                }
            }
        } catch (AuthenticationException e) {
            e.printStackTrace();
        }

        model.addAttribute("message", "Please log in");
        return "error";
    }

    @GetMapping(value = "/{orderid}")
    @PreAuthorize("isAuthenticated()")
    public String viewSpecificOrder(@PathVariable(value = "orderid") long orderID, Model model) {

        Optional<Order> selectedOrder = orderRepository.findById(orderID);
        if (selectedOrder.isPresent()) {
            try {
                Authentication user = userService.getCurrentUser();
                // Only the user itself or an admin can view the users order
                if (user.getName().equals(selectedOrder.get().getCustomer().getUsername()) || user.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                    model.addAttribute("orderID", orderID);
                    model.addAttribute("orderPrice", selectedOrder.get().getPrice());
                    model.addAttribute("orderCustomer", selectedOrder.get().getCustomer().getUsername());
                    model.addAttribute("orderItems", selectedOrder.get().getItems());
                    return "orderDetails";
                }
                else {
                    model.addAttribute("message", "The requested order doesn't belong the logged in user");
                    return "error";
                }
            } catch (AuthenticationException e) {
                e.printStackTrace();
            }
        }

        model.addAttribute("message", "The requested order doesn't exist");
        return "error";
    }
}
