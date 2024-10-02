package de.group15.assignment1.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.group15.assignment1.model.*;
import de.group15.assignment1.repository.OrderRepository;
import de.group15.assignment1.service.BeverageService;
import de.group15.assignment1.service.ShoppingCartService;
import de.group15.assignment1.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.naming.AuthenticationException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

import java.sql.Timestamp;
import java.util.Date;

@Slf4j
@Controller
@RequestMapping(value = "/shoppingcart/checkout")
public class CheckOutController {
    //get " show a review of the order with all the items selected + the address"
    //post " save the items as list of orderItems inside an order object into the database then redirect to success message

    private final OrderRepository orderRepository;
    @Autowired
    ShoppingCartService shoppingCartService;
    @Autowired
    UserService userService;
    @Autowired
    BeverageService beverageService;
    @Autowired
    RestTemplate restTemplate;

    public CheckOutController(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @GetMapping(value = "/{orderid}")
    @PreAuthorize("isAuthenticated()")
    public String successPurchaseSummary(@PathVariable Long orderid, Model model) {
        log.info("User finished purchase");
        Order selectedOrder = new Order();
        if (this.orderRepository.findById(orderid).isPresent()) {
            selectedOrder = this.orderRepository.findById(orderid).get();

            try {
                Authentication user = userService.getCurrentUser();
                // Only the user itself can checkout its order
                if (user.getName().equals(selectedOrder.getCustomer().getUsername())) {
                    model.addAttribute("orderID", orderid);
                    model.addAttribute("orderPrice", selectedOrder.getPrice());
                    model.addAttribute("orderCustomer", selectedOrder.getCustomer().getUsername());
                    model.addAttribute("orderItems", selectedOrder.getItems());

                    return "checkout";
                }
                else {
                    model.addAttribute("message", "The requested order doesn't belong the logged in user");
                    return "error";
                }
            } catch (AuthenticationException e) {
                e.printStackTrace();
            }
        }
        model.addAttribute("message", "The requested order does not exist.");
        return "error";
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public String completePurchase(Model model) {
        log.info("User confirmed the order");
        Order newOrder = new Order();
        try {
            Authentication auth = userService.getCurrentUser();
            String id = auth.getName();
            User orderingUser = userService.getUser(id);
            List<OrderItem> listOfItems = shoppingCartService.getItemsInCart();

            log.info("System saving the order of the user .. ");
            newOrder.setCustomer(orderingUser);
            listOfItems.stream().forEach(item -> newOrder.addOrderItem(item));
            double totalPrice = newOrder.priceTotal(listOfItems);
            newOrder.setPrice(totalPrice);
            this.orderRepository.save(newOrder);
            log.info("Saving complete!");

            /******* Updating the quantity of beverages in stock after completing purchase *******/
            int orderedQuantity;
            int beverageQuantity;
            Long beverageID;
            for (int i = 0; i < listOfItems.size(); i++) {
                log.info("updating quantity in of beverage iteration number: " + i + " ");
                orderedQuantity = listOfItems.get(i).getQuantity();
                beverageQuantity = listOfItems.get(i).getBeverage().getInStock();
                beverageID = listOfItems.get(i).getBeverage().getId();
                beverageService.updateBeverageQuantity(beverageID, (beverageQuantity - orderedQuantity));
            }
            /************************************************************************************/

            /******* Invoking the pdf gen Faas ***************/
            log.info("creating orderDTO .. ");
            OrderDTO orderDTO = new OrderDTO();
            List<OrderListDTO> orderListDTO = new ArrayList<>();
            for (int i = 0; i<listOfItems.size(); i++) {
                orderListDTO.add(new OrderListDTO(listOfItems.get(i).getPrice(), listOfItems.get(i).getQuantity(), listOfItems.get(i).getBeverage().getName(), listOfItems.get(i).getBeverage().getPic(), listOfItems.get(i).getBeverage().getId()));
            }
            orderDTO.setId(newOrder.getId());
            orderDTO.setTotalPrice(totalPrice);
            orderDTO.setListOfItems(orderListDTO);
            orderDTO.setUserEmail(orderingUser.getEmail());
            orderDTO.setPostalCode(orderingUser.getDeliveryaddresses().iterator().next().getPostalcode());
            orderDTO.setTimeStamp(String.valueOf(new Timestamp(new Date().getTime())));
            log.info("orderDTO creation complete!");
            //*******
            log.info("converting orderDTO to json . . .");
            String endPoint = "http://localhost:8081/";
            ObjectMapper mapper = new ObjectMapper();
            String orderJson = null;
            try {
                orderJson = mapper.writeValueAsString(orderDTO);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            log.info("Order converted to Json!");
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> request = new HttpEntity<>(orderJson, headers);
            log.info(orderJson);
            ResponseEntity<String> response = restTemplate.exchange(endPoint, HttpMethod.POST, request, String.class);
            if(response.getStatusCode() == HttpStatus.OK) {
                log.info("pdf Function invoked successfully from CheckOut Controller");
            } else {
                log.info("pdf Function did not get invoked");
            }
            //*****************************************

            shoppingCartService.clearAllItems();
            log.info("shopping cart clear of the items");
            return "redirect:/shoppingcart/checkout/" + newOrder.getId();

        } catch (AuthenticationException e) {
            e.printStackTrace();
        }
        model.addAttribute("message", "Please log in.");
        return "error";
    }

}


