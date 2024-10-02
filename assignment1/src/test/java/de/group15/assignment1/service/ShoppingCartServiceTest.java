package de.group15.assignment1.service;

import de.group15.assignment1.model.*;
import de.group15.assignment1.repository.UserRepository;
import de.group15.assignment1.service.impl.ShoppingCartServiceImpl;
import org.aspectj.weaver.ast.Or;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;

import javax.naming.AuthenticationException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@SpringBootTest
public class ShoppingCartServiceTest {

    @Autowired
    private ShoppingCartServiceImpl shoppingCartService;

    @Mock
    private List<OrderItem> items;

    private OrderItem sampleItem;

    @BeforeEach
    public void initCommonUsedData() {
        items = shoppingCartService.getItemsInCart();
        //Schlenkerla
        Bottle schlenkerla = new Bottle();
        schlenkerla.setId(1L);
        schlenkerla.setName("Schlenkerla");
        schlenkerla.setPic("https://www.getraenkewelt-weiser.de/images/product/01/85/40/18546-0-p.jpg");
        schlenkerla.setVolume(0.5);
        schlenkerla.setVolumePercent(5.1);
        schlenkerla.setPrice(0.89);
        schlenkerla.setSupplier("Rauchbierbrauerei Schlenkerla");
        schlenkerla.setInStock(438);

        sampleItem = new OrderItem(schlenkerla, 34);
    }

    @Test
    public void addItem_ShouldSuccessOnNewItem() {
        shoppingCartService.addItem(sampleItem);

        assertTrue(this.items.contains(sampleItem));
    }


    @Test
    public void addItem_ShouldSuccessOnExistingItem() {
        //given
        shoppingCartService.addItem(sampleItem);

        //when
        shoppingCartService.addItem(sampleItem);

        //then
        assertTrue(this.items.contains(sampleItem));
        assertEquals(34 * 2, this.items.get(0).getQuantity());
    }

    @Test
    public void updateItem_ShouldUpdateQuantity() {
        //given
        shoppingCartService.addItem(sampleItem);
        sampleItem.setQuantity(1);

        //when
        shoppingCartService.updateItem(sampleItem);

        //then
        assertTrue(this.items.contains(sampleItem));
        assertEquals(1, this.items.get(0).getQuantity());
    }


    @Test
    public void updateItem_ShouldRemoveItem() {
        //given
        shoppingCartService.addItem(sampleItem);
        sampleItem.setQuantity(0);

        //when
        shoppingCartService.updateItem(sampleItem);

        //then
        assertFalse(this.items.contains(sampleItem));
    }

    @Test
    public void getItemsInCart_ShouldSuccess() {
        //when
        List<OrderItem> actual = shoppingCartService.getItemsInCart();

        //then
        assertEquals(this.items, actual);
    }


    @Test
    public void getTotal_ShouldSuccess() {
        //given
        shoppingCartService.addItem(sampleItem);

        //when
        double actual = shoppingCartService.getTotal();

        //then
        assertEquals(sampleItem.getQuantity(), actual);
    }

    @Test
    public void clearAllItems_ShouldSuccess() {
        //given
        shoppingCartService.addItem(sampleItem);

        //when
        shoppingCartService.clearAllItems();

        //then
        assertEquals(0, items.size());
    }
}
