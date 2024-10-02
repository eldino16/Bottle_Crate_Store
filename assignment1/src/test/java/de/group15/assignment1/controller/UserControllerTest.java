package de.group15.assignment1.controller;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.group15.assignment1.model.*;
import de.group15.assignment1.repository.OrderRepository;
import de.group15.assignment1.repository.UserRepository;
import de.group15.assignment1.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.time.LocalDate;
import java.util.*;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest//(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class UserControllerTest {

    @MockBean
    private UserRepository userRepository;

    @Autowired
    private MockMvc mvc;


    private List<Order> allOrders = new ArrayList<>();
    private List<Order> maxOrders = new ArrayList<>();
    private User max;
    private User other;
    private UserDTO userDto;


    @BeforeEach
    public void initCommonUsedData() {
        // Max User
        Address address = new Address(null, "An der Spinnerei", "13", "96049");
        Address address2 = new Address(null, "An der Test", "15", "96000");
        max = new User();
        max.setUsername("Max");
        max.setPassword("123456");
        max.setRole("CUSTOMER");
        max.setBirthday(LocalDate.of(1996, 8, 2));
        max.setBillingaddresses(Collections.singleton(address2));
        max.setDeliveryaddresses(Collections.singleton(address));

        // Other User
        other = new User();
        other.setUsername("Moritz");
        other.setPassword("123456");
        other.setRole("CUSTOMER");
        other.setBirthday(LocalDate.of(1995, 12, 2));
        other.setBillingaddresses(Collections.singleton(address2));
        other.setDeliveryaddresses(Collections.singleton(address));

        userDto = new UserDTO("password", LocalDate.now(), "mail@mail.de", "Username", Collections.singletonList(address), Collections.singletonList(address2));

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

        Order maxOrder = new Order();
        maxOrder.addOrderItem(new OrderItem(crateSchlenkerla,145));
        maxOrder.setCustomer(this.max);
        maxOrder.setPrice(maxOrder.priceTotal(maxOrder.getItems()));
        maxOrder.setId(1L);


        Order moritzOrder = new Order();
        moritzOrder.addOrderItem(new OrderItem(schlenkerla,145));
        moritzOrder.setCustomer(other);
        moritzOrder.setPrice(moritzOrder.priceTotal(moritzOrder.getItems()));
        moritzOrder.setId(2L);

        maxOrders.add(maxOrder);

        allOrders.add(maxOrder);
        allOrders.add(moritzOrder);

        this.max.setOrders(maxOrders);
    }


    @Test
    @WithMockUser(username = "Max", roles = "CUSTOMER")
    public void getGetUser_shouldSuccess() throws Exception {
        when(this.userRepository.findAllById(Collections.singletonList("Max"))).thenReturn(Collections.singletonList(this.max));

        this.mvc.perform(get("/usersinfo?user_id=" + "Max"))
                .andExpect(status().isOk())
                .andExpect(view().name("userInfo"))
                .andExpect(model().attribute("usersinfo", Collections.singletonList(this.max)))
                .andExpect(content().string(containsString(this.max.getUsername())));

        verify(this.userRepository, times(1)).findAllById(Collections.singletonList("Max"));
    }

    @Test
    @WithMockUser(username = "Admin", roles = "ADMIN")
    public void getGetUser_shouldSuccessWithAdminOnOtherUser() throws Exception {
        when(this.userRepository.findAllById(Collections.singletonList("Moritz"))).thenReturn(Collections.singletonList(this.other));

        this.mvc.perform(get("/usersinfo?user_id=" + "Moritz"))
                .andExpect(status().isOk())
                .andExpect(view().name("userInfo"))
                .andExpect(model().attribute("usersinfo", Collections.singletonList(this.other)))
                .andExpect(content().string(containsString(this.other.getUsername())));

        verify(this.userRepository, times(1)).findAllById(Collections.singletonList("Moritz"));
    }


    @Test
    @WithMockUser(username = "Max", roles = "CUSTOMER")
    public void getGetUser_shouldFailOnWrongUser() throws Exception {
        when(this.userRepository.findAllById(Collections.singletonList("Moritz"))).thenReturn(Collections.singletonList(this.other));

        this.mvc.perform(get("/usersinfo?user_id=" + "Moritz"))
                .andExpect(status().isOk())
                .andExpect(view().name("error"))
                .andExpect(model().attribute("message", "The requested user isn't the logged in user"))
                .andExpect(content().string(containsString("The requested user isn&#39;t the logged in user")));
    }


    @Test
    public void getGetUser_shouldFailWhenNotAuthenticated() throws Exception {
        this.mvc.perform(get("/usersinfo?user_id=" + "Max"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));
    }

    @Test
    @WithMockUser(username = "Max", roles = "CUSTOMER")
    public void getGetUserAddress_shouldSuccess() throws Exception {
        when(this.userRepository.findAllById(Collections.singletonList("Max"))).thenReturn(Collections.singletonList(this.max));

        this.mvc.perform(get("/usersinfo/address"))
                .andExpect(status().isOk())
                .andExpect(view().name("userInfo"))
                .andExpect(model().attribute("usersinfo", Collections.singletonList(this.max)))
                .andExpect(content().string(containsString(this.max.getUsername())));

        verify(this.userRepository, times(1)).findAllById(Collections.singletonList("Max"));
    }

    @Test
    public void getGetUserAddress_shouldFailWhenNotAuthenticated() throws Exception {
        this.mvc.perform(get("/usersinfo/address"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));
    }

    @Test
    @WithMockUser(username = "Max", roles = "CUSTOMER")
    public void getUpdateAddress_shouldSuccess() throws Exception {
        String userId = "Max";

        this.mvc.perform(post("/usersinfo/updateaddress/" + userId)
                .params(convert(this.userDto))
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/usersinfo?user_id="+ userId));
    }

    @Test
    @WithMockUser(username = "Moritz", roles = "CUSTOMER")
    public void getUpdateAddress_shouldFailOnWrongUser() throws Exception {
        String userId = "Max";

        this.mvc.perform(post("/usersinfo/updateaddress/" + userId)
                .params(convert(this.userDto))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("error"))
                .andExpect(model().attribute("message", "The requested user isn't the logged in user"))
                .andExpect(content().string(containsString("The requested user isn&#39;t the logged in user")));
    }

    @Test
    public void getUpdateAddress_shouldFailWhenNotAuthenticated() throws Exception {
        this.mvc.perform(post("/usersinfo/updateaddress/" + "Max")
                .with(csrf())
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));
    }


    @Test
    @WithMockUser(username = "Max", roles = "CUSTOMER")
    public void getUpdateProfile_shouldSuccess() throws Exception {
        String userId = "Max";

        this.mvc.perform(post("/usersinfo/updateProfile/" + userId)
                .params(convert(this.userDto))
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/userProfile"));
    }

    @Test
    @WithMockUser(username = "Moritz", roles = "CUSTOMER")
    public void getUpdateProfile_shouldFailOnWrongUser() throws Exception {
        String userId = "Max";

        this.mvc.perform(post("/usersinfo/updateProfile/" + userId)
                .params(convert(this.userDto))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("error"))
                .andExpect(model().attribute("message", "The requested user isn't the logged in user"))
                .andExpect(content().string(containsString("The requested user isn&#39;t the logged in user")));
    }

    @Test
    public void getUpdateProfile_shouldFailWhenNotAuthenticated() throws Exception {
        this.mvc.perform(post("/usersinfo/updateProfile/" + "Max")
                .with(csrf())
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));
    }


    @Test
    @WithMockUser(username = "Max", roles = "CUSTOMER")
    public void getGetUserProfile_shouldSuccess() throws Exception {
        this.mvc.perform(get("/userProfile"))
                .andExpect(status().isOk())
                .andExpect(view().name("userProfile"))
                .andExpect(content().string(containsString(this.max.getUsername())));
    }


    @Test
    public void getGetUserProfile_shouldFailWhenNotAuthenticated() throws Exception {
        this.mvc.perform(get("/userProfile"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));
    }


    private static MultiValueMap<String, String> convert(UserDTO dto) {
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();

        for (int i = 0; i< dto.getBillingAddresses().size(); i++) {
            Address a = dto.getBillingAddresses().get(i);
            Map<String, String> maps = new ObjectMapper().convertValue(a, new TypeReference<Map<String, String>>() {});
            Map<String, String> newMap = new HashMap<>();
            for (Map.Entry<String, String> entry : maps.entrySet()) {
                newMap.put("billingAddresses["+i+"]."+ entry.getKey(), maps.get(entry.getKey()));
            }
            parameters.setAll(newMap);
        }

        for (int i = 0; i< dto.getDeliveryAddresses().size(); i++) {
            Address a = dto.getDeliveryAddresses().get(i);
            Map<String, String> maps = new ObjectMapper().convertValue(a, new TypeReference<Map<String, String>>() {});
            Map<String, String> newMap = new HashMap<>();
            for (Map.Entry<String, String> entry : maps.entrySet()) {
                newMap.put("deliveryAddresses["+i+"]."+ entry.getKey(), maps.get(entry.getKey()));
            }
            parameters.setAll(newMap);
        }

        return parameters;
    }

}
