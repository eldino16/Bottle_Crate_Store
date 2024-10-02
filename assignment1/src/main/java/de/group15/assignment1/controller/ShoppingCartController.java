package de.group15.assignment1.controller;

import de.group15.assignment1.model.Beverage;
import de.group15.assignment1.model.OrderItem;
import de.group15.assignment1.model.OrderItemDTO;
import de.group15.assignment1.repository.BeverageRepository;
import de.group15.assignment1.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.Optional;

@RequestMapping("/shoppingcart")
@Slf4j
@Controller
public class ShoppingCartController {

    private final ShoppingCartService shoppingCartService;
    @Autowired
    BeverageRepository beverageRepository;

    public ShoppingCartController(ShoppingCartService shoppingCartService) {
        this.shoppingCartService = shoppingCartService;
    }

    //Show items in the cart
    @GetMapping
    public ModelAndView shoppingCart() {
        ModelAndView modelAndview = new ModelAndView("reviewCart");
        log.info("Review Shopping Cart");
        modelAndview.addObject("update", new OrderItemDTO());
        modelAndview.addObject("items", shoppingCartService.getItemsInCart());
        modelAndview.addObject("total", shoppingCartService.getTotal());
        return modelAndview;
    }

    //Add OrderItem to the Cart
    @PostMapping("/add")
    public String addItem(OrderItemDTO item) {
        log.info("Add OrderItem to the Cart");
        Optional<Beverage> beverage = beverageRepository.findById(item.getBeverageId());
        beverage.ifPresent(b -> shoppingCartService.addItem(new OrderItem(b, item.getQuantity())));

        return "redirect:/beverages";
    }

    //Update OrderItem in cart
    @PostMapping("/update")
    public String updateItem(OrderItemDTO item, Model model) {
        log.info("Update OrderItem in cart");

        Optional<Beverage> beverage = beverageRepository.findById(item.getBeverageId());
        beverage.ifPresent(b -> shoppingCartService.updateItem(new OrderItem(b, item.getQuantity())));
        model.addAttribute("items", shoppingCartService.getItemsInCart());
        return "reviewCart";
    }

    @GetMapping("/delete/{id}")
    public String deleteItem(@PathVariable Long id, Model model) {
        log.info("Deleting OrderItem from cart");

        Optional<Beverage> beverage = beverageRepository.findById(id);
        beverage.ifPresent(b -> shoppingCartService.updateItem(new OrderItem(b, 0)));

        model.addAttribute("update", new OrderItemDTO());
        model.addAttribute("items", shoppingCartService.getItemsInCart());
        model.addAttribute("total", shoppingCartService.getTotal());
        return "reviewCart";
    }
}
