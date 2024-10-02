package de.group15.assignment1.controller;

import de.group15.assignment1.model.Beverage;
import de.group15.assignment1.model.Bottle;
import de.group15.assignment1.model.Crate;
import de.group15.assignment1.model.OrderItemDTO;
import de.group15.assignment1.repository.BeverageRepository;
import de.group15.assignment1.repository.BottleRepository;
import de.group15.assignment1.repository.CrateRepository;
import de.group15.assignment1.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;


import javax.validation.Valid;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Controller
@RequestMapping({"/", "/beverages"})
public class BeverageController {

    private final ShoppingCartService shoppingCartService;

    @Autowired
    BeverageRepository beverageRepository;
    @Autowired
    BottleRepository bottleRepository;
    @Autowired
    CrateRepository crateRepository;

    public BeverageController(ShoppingCartService shoppingCartService) {
        this.shoppingCartService = shoppingCartService;
    }

    Comparator<Beverage> beverageidComarator = new Comparator<Beverage>() {
        @Override
        public int compare(Beverage b1, Beverage
                b2) {
            return b1.getId().compareTo(b2.getId());
        }
    };


    @GetMapping
    public String home(Model model) {

        log.info("** Client requested all beverages");
        List<Beverage> beverages = this.beverageRepository.findAll();
        Collections.sort(beverages, beverageidComarator);

        model.addAttribute("beverages", beverages);
        model.addAttribute("item", new OrderItemDTO());
        model.addAttribute("listofitems", shoppingCartService.getItemsInCart().size());
        return "beverages";
    }

    @GetMapping("/addnewbottle")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String addNewBottle(Model model) {
        log.info("** Client wants to add new bottle");
        model.addAttribute("bottle", new Bottle());
        return "addBottle";
    }

    @PostMapping("/addnewbottle")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String addBottle(@Valid Bottle newBottle, Errors errors, Model model) {
        log.info("** Client added a new Bottle: " + newBottle);
        if (errors.hasErrors()) {
            log.info("...but there are errors : " + newBottle);
            model.addAttribute("bottle", newBottle);
            return "addBottle";
        }

        newBottle.setAlcoholic(newBottle.getVolumePercent() > 0.0);

        this.bottleRepository.save(newBottle);
        return home(model);
    }

    @GetMapping("/addnewcrate")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String addNewCrate(Model model) {
        log.info("** Client wants to add new crate");
        model.addAttribute("crate", new Crate());
        model.addAttribute("bottles", bottleRepository.findAll());
        return "addCrate";
    }

    @PostMapping("/addnewcrate")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String addCrate(@Valid Crate newCrate, Errors errors, Model model) {
        log.info("** Client added a new Crate: " + newCrate);
        if (errors.hasErrors()) {
            log.info("...but there are errors : " + newCrate);
            model.addAttribute("crate", newCrate);
            model.addAttribute("bottles", bottleRepository.findAll());
            return "addCrate";
        }
        this.crateRepository.save(newCrate);
        return home(model);
    }
}
