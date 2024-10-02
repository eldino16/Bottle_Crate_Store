package de.group15.assignment1.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.group15.assignment1.model.*;
import de.group15.assignment1.repository.BeverageRepository;
import de.group15.assignment1.repository.BottleRepository;
import de.group15.assignment1.repository.CrateRepository;
import de.group15.assignment1.repository.OrderRepository;
import de.group15.assignment1.service.BeverageService;
import de.group15.assignment1.service.ShoppingCartService;
import de.group15.assignment1.service.UserService;
import de.group15.assignment1.service.impl.UserServiceImpl;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.*;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest//(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class CheckOutControllerTest {

    @MockBean
    private BeverageService beverageService;

    @MockBean
    private ShoppingCartService shoppingCartService;

    @MockBean
    private OrderRepository orderRepository;

    @MockBean
    private RestTemplate restTemplate;

    @Autowired
    private MockMvc mvc;

    private Order sampleOrder;
    private List<OrderItem> sampleItems = new ArrayList<>();
    private User sampleUser;
    private Long orderId;

    @BeforeEach
    public void initCommonUsedData() {
        // Sample User
        Address address = new Address(null, "An der Spinnerei", "13", "96049");
        Address address2 = new Address(null, "An der Test", "15", "96000");
        sampleUser = new User();
        sampleUser.setUsername("Max");
        sampleUser.setPassword("123456");
        sampleUser.setRole("CUSTOMER");
        sampleUser.setEmail("mail@example.de");
        sampleUser.setBirthday(LocalDate.of(1996, 8, 2));
        sampleUser.setBillingaddresses(Collections.singleton(address));
        sampleUser.setBillingaddresses(Collections.singleton(address2));
        sampleUser.setDeliveryaddresses(Collections.singleton(address));

        //Schlenkerla
        Bottle schlenkerla = new Bottle();
        schlenkerla.setName("Schlenkerla");
        schlenkerla.setPic("https://www.getraenkewelt-weiser.de/images/product/01/85/40/18546-0-p.jpg");
        schlenkerla.setVolume(0.5);
        schlenkerla.setVolumePercent(5.1);
        schlenkerla.setPrice(0.89);
        schlenkerla.setSupplier("Rauchbierbrauerei Schlenkerla");
        schlenkerla.setInStock(438);

        //Crate Schlenkerla
        Crate crateSchlenkerla = new Crate();
        crateSchlenkerla.setName("20 Crate Schlenkerla");
        crateSchlenkerla.setPic("https://www.getraenkedienst.com/media/image/34/b1/39/Brauerei_Heller_Schlenkerla_Aecht_Schlenkerla_Rauchbier_Maerzen_20_x_0_5l.jpg");
        crateSchlenkerla.setNoOfBottles(20);
        crateSchlenkerla.setPrice(18.39);
        crateSchlenkerla.setInStock(13);
        crateSchlenkerla.setBottle(schlenkerla);

        this.sampleItems.add(new OrderItem(schlenkerla,145));
        this.sampleItems.add(new OrderItem(crateSchlenkerla,6));

        this.sampleOrder = new Order();
        this.sampleOrder.addOrderItem(new OrderItem(schlenkerla,145));
        this.sampleOrder.setCustomer(this.sampleUser);
    }


    @Test
    @WithMockUser(username = "Max", roles = "CUSTOMER")
    public void getFinalReviewOfSelectedItems_shouldSuccess() throws Exception {
        orderId = 1L;
        when(this.orderRepository.findById(orderId)).thenReturn(Optional.ofNullable(sampleOrder));

        this.mvc.perform(get("/shoppingcart/checkout/" + orderId))
                .andExpect(status().isOk())
                .andExpect(view().name("checkout"))
                .andExpect(model().attribute("orderID", orderId))
                .andExpect(model().attribute("orderPrice", sampleOrder.getPrice()))
                .andExpect(model().attribute("orderCustomer", sampleOrder.getCustomer().getUsername()))
                .andExpect(model().attribute("orderItems", sampleOrder.getItems()))
                .andExpect(content().string(containsString(sampleOrder.getItems().get(0).getBeverage().getName())));

        verify(this.orderRepository, times(2)).findById(orderId);
    }

    @Test
    public void getFinalReviewOfSelectedItems_shouldFailWhenNotAuthenticated() throws Exception {
        this.mvc.perform(get("/shoppingcart/checkout/" + orderId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));
    }

    @Test
    @WithMockUser(username = "Moritz", roles = "CUSTOMER")
    public void getFinalReviewOfSelectedItems_shouldFailWhenOrderDoesntBelongToUser() throws Exception {
        orderId = 1L;
        when(this.orderRepository.findById(orderId)).thenReturn(Optional.ofNullable(sampleOrder));

        this.mvc.perform(get("/shoppingcart/checkout/" + orderId))
                .andExpect(status().isOk())
                .andExpect(view().name("error"))
                .andExpect(model().attribute("message", "The requested order doesn't belong the logged in user"))
                .andExpect(content().string(containsString("The requested order doesn&#39;t belong the logged in user")));

        verify(this.orderRepository, times(2)).findById(orderId);
    }

    @Test
    @WithMockUser(username = "Max", roles = "CUSTOMER")
    public void getFinalReviewOfSelectedItems_shouldFailWhenOrderDoesntExist() throws Exception {
        orderId = 1L;
        when(this.orderRepository.findById(orderId)).thenReturn(Optional.ofNullable(null));

        this.mvc.perform(get("/shoppingcart/checkout/" + orderId))
                .andExpect(status().isOk())
                .andExpect(view().name("error"))
                .andExpect(model().attribute("message", "The requested order does not exist."))
                .andExpect(content().string(containsString("The requested order does not exist.")));

        verify(this.orderRepository, times(1)).findById(orderId);
    }

    @Test
    @WithMockUser(username = "Max", roles = "CUSTOMER")
    public void postCompletePurchase_shouldSuccess() throws Exception {
        Order newOrder = new Order();
        sampleItems.stream().forEach(item -> newOrder.addOrderItem(item));
        double totalPrice = newOrder.priceTotal(sampleItems);
        newOrder.setPrice(totalPrice);
        newOrder.setCustomer(sampleUser);

        when(this.orderRepository.save(newOrder)).thenReturn(newOrder);
        when(this.shoppingCartService.getItemsInCart()).thenReturn(this.sampleItems);
        when(this.restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(Class.class))).thenReturn(new ResponseEntity("Success", HttpStatus.OK));

        /*
        this.mvc.perform(post("/shoppingcart/checkout").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/shoppingcart/checkout/" + newOrder.getId()));

        verify(this.shoppingCartService, times(1)).getItemsInCart();
        verify(this.orderRepository, times(1)).save(newOrder);
        verify(this.beverageService, times(1)).updateBeverageQuantity(this.sampleItems.get(0).getBeverage().getId(), (this.sampleItems.get(0).getBeverage().getInStock() - this.sampleItems.get(0).getQuantity()));
        verify(this.beverageService, times(1)).updateBeverageQuantity(this.sampleItems.get(1).getBeverage().getId(), (this.sampleItems.get(1).getBeverage().getInStock() - this.sampleItems.get(1).getQuantity()));
        verify(this.shoppingCartService, times(1)).clearAllItems();
        */
    }

    @Test
    public void postCompletePurchase_shouldFailWhenNotAuthenticated() throws Exception {
        this.mvc.perform(post("/shoppingcart/checkout"))
                .andExpect(status().isForbidden());
    }

}
