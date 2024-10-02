package de.group15.assignment1.controller;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.group15.assignment1.model.*;
import de.group15.assignment1.repository.BeverageRepository;
import de.group15.assignment1.repository.OrderRepository;
import de.group15.assignment1.repository.UserRepository;
import de.group15.assignment1.service.ShoppingCartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest//(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class ShoppingCartControllerTest {

    @MockBean
    private ShoppingCartService shoppingCartService;

    @MockBean
    private BeverageRepository beverageRepository;

    @Autowired
    private MockMvc mvc;

    private List<OrderItem> sampleItems = new ArrayList<>();
    private OrderItemDTO sampleItem;
    private Beverage sampleBeverage;

    @BeforeEach
    public void initCommonUsedData() {
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
        crateSchlenkerla.setId(1L);

        this.sampleItems.add(new OrderItem(schlenkerla,145));
        this.sampleItems.add(new OrderItem(crateSchlenkerla,6));

        this.sampleBeverage = crateSchlenkerla;
        this.sampleItem = new OrderItemDTO(2, 1L);
    }


    @Test
    @WithMockUser(username = "Max", roles = "CUSTOMER")
    public void getShoppingCart_shouldSuccess() throws Exception {
        when(this.shoppingCartService.getItemsInCart()).thenReturn(this.sampleItems);
        when(this.shoppingCartService.getTotal()).thenReturn(this.sampleItems.stream().mapToDouble(OrderItem::getPrice).sum());

        this.mvc.perform(get("/shoppingcart"))
                .andExpect(status().isOk())
                .andExpect(view().name("reviewCart"))
                .andExpect(model().attribute("update", new OrderItemDTO()))
                .andExpect(model().attribute("items", this.sampleItems))
                .andExpect(model().attribute("total", this.sampleItems.stream().mapToDouble(OrderItem::getPrice).sum()))
                .andExpect(content().string(containsString(String.valueOf(this.sampleItems.get(0).getBeverage().getName()))));

        verify(this.shoppingCartService, times(1)).getItemsInCart();
        verify(this.shoppingCartService, times(1)).getTotal();
    }

    @Test
    public void getShoppingCart_shouldFailWhenNotAuthenticated() throws Exception {
        this.mvc.perform(get("/shoppingcart"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));
    }

    @Test
    @WithMockUser(username = "Max", roles = "CUSTOMER")
    public void getAddItem_shouldSuccess() throws Exception {
        when(this.beverageRepository.findById(this.sampleItem.getBeverageId())).thenReturn(Optional.ofNullable(this.sampleBeverage));

        this.mvc.perform(post("/shoppingcart/add")
                    .params(convert(this.sampleItem))
                        .with(csrf())
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/beverages"));

        verify(this.shoppingCartService, times(1)).addItem(new OrderItem(this.sampleBeverage, this.sampleItem.getQuantity()));
        verify(this.beverageRepository, times(1)).findById(this.sampleItem.getBeverageId());
    }

    @Test
    public void getAddItem_shouldFailWhenNotAuthenticated() throws Exception {
        this.mvc.perform(post("/shoppingcart/add")
                    .params(convert(this.sampleItem))
                        .with(csrf())
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));
    }

    @Test
    @WithMockUser(username = "Max", roles = "CUSTOMER")
    public void getUpdateItem_shouldSuccess() throws Exception {
        when(this.beverageRepository.findById(this.sampleItem.getBeverageId())).thenReturn(Optional.ofNullable(this.sampleBeverage));
        when(this.shoppingCartService.getItemsInCart()).thenReturn(this.sampleItems);

        this.mvc.perform(post("/shoppingcart/update")
                    .params(convert(this.sampleItem))
                        .with(csrf())
                )
                .andExpect(status().isOk())
                .andExpect(view().name("reviewCart"))
                .andExpect(model().attribute("items", this.sampleItems))
                .andExpect(content().string(containsString(String.valueOf(this.sampleItems.get(0).getBeverage().getName()))));

        verify(this.beverageRepository, times(1)).findById(this.sampleItem.getBeverageId());
        verify(this.shoppingCartService, times(1)).updateItem(new OrderItem(this.sampleBeverage, this.sampleItem.getQuantity()));
        verify(this.shoppingCartService, times(1)).getItemsInCart();
    }

    @Test
    public void getUpdateItem_shouldFailWhenNotAuthenticated() throws Exception {
        this.mvc.perform(post("/shoppingcart/update")
                    .params(convert(this.sampleItem))
                        .with(csrf())
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));
    }


    @Test
    @WithMockUser(username = "Max", roles = "CUSTOMER")
    public void getDeleteItem_shouldSuccess() throws Exception {
        Long itemId = 1L;

        when(this.beverageRepository.findById(itemId)).thenReturn(Optional.ofNullable(this.sampleBeverage));
        when(this.shoppingCartService.getTotal()).thenReturn(this.sampleItems.stream().mapToDouble(OrderItem::getPrice).sum());
        when(this.shoppingCartService.getItemsInCart()).thenReturn(this.sampleItems);


        this.mvc.perform(get("/shoppingcart/delete/" + itemId))
                .andExpect(status().isOk())
                .andExpect(view().name("reviewCart"))
                .andExpect(model().attribute("update", new OrderItemDTO()))
                .andExpect(model().attribute("items", this.sampleItems))
                .andExpect(model().attribute("total", this.sampleItems.stream().mapToDouble(OrderItem::getPrice).sum()))
                .andExpect(content().string(containsString(String.valueOf(this.sampleItems.get(0).getBeverage().getName()))));

        verify(this.shoppingCartService, times(1)).updateItem(new OrderItem(this.sampleBeverage, 0));
        verify(this.shoppingCartService, times(1)).getItemsInCart();
        verify(this.shoppingCartService, times(1)).getTotal();
    }

    @Test
    public void getDeleteItem_shouldFailWhenNotAuthenticated() throws Exception {
        Long itemId = 1L;
        this.mvc.perform(get("/shoppingcart/delete/" + itemId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));
    }


    private static MultiValueMap<String, String> convert(Object obj) {
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        Map<String, String> maps = new ObjectMapper().convertValue(obj, new TypeReference<Map<String, String>>() {});
        parameters.setAll(maps);

        return parameters;
    }
}
