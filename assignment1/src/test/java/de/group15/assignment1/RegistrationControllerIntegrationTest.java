package de.group15.assignment1;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.group15.assignment1.model.Address;
import de.group15.assignment1.model.UserDTO;
import de.group15.assignment1.repository.UserRepository;
import de.group15.assignment1.service.UserService;
import de.group15.assignment1.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import javax.transaction.Transactional;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
public class RegistrationControllerIntegrationTest {

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private MockMvc mvc;

    private UserDTO firstRegistration;
    private UserDTO secondRegistration;

    @BeforeEach
    public void initCommonUsedData() {
        Address address = new Address(null, "An der Spinnerei", "13", "96049");
        Address address2 = new Address(null, "An der Test", "15", "96000");

        firstRegistration = new UserDTO("Password123", LocalDate.of(1996, 8, 2), "mail@mail.de", "Admin", Collections.singletonList(address), Collections.singletonList(address2));
        secondRegistration = new UserDTO("Password123", LocalDate.of(1995, 2, 12), "example@mail.de", "Max", Collections.singletonList(address), Collections.singletonList(address2));
    }

    @Test
    @Transactional
    public void postRegistrationRequest_createAdminUserAsFirstUser() throws Exception {
        // test storing admin user
        this.mvc.perform(post("/register")
                .params(convert(firstRegistration))
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/beverages"));

        //confirm that stored user has role admin
        assertTrue(this.userService.loadUserByUsername("Admin")
                .getAuthorities()
                .contains(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }


    @Test
    @Transactional
    public void postRegistrationRequest_createCustomerUserAsNotFirstUser() throws Exception {
        // given
        this.mvc.perform(post("/register")
                .params(convert(firstRegistration))
                .with(csrf()));

        //when
        this.mvc.perform(post("/register")
                .params(convert(secondRegistration))
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/beverages"));

        //confirm that stored user has role customer
        assertTrue(this.userService.loadUserByUsername("Max")
                .getAuthorities()
                .contains(new SimpleGrantedAuthority("ROLE_CUSTOMER")));
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
        parameters.add("password", dto.getPassword());
        if (dto.getBirthday() != null) {
            parameters.add("birthday", dto.getBirthday().toString());
        }
        parameters.add("email", dto.getEmail());
        parameters.add("username", dto.getUsername());
        return parameters;
    }



}
