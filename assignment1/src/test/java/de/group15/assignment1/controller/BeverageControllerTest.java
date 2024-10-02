package de.group15.assignment1.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.group15.assignment1.model.Beverage;
import de.group15.assignment1.model.Bottle;
import de.group15.assignment1.model.Crate;
import de.group15.assignment1.model.OrderItem;
import de.group15.assignment1.repository.BeverageRepository;
import de.group15.assignment1.repository.BottleRepository;
import de.group15.assignment1.repository.CrateRepository;
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

import java.util.*;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.containsString;

@ActiveProfiles("test")
@SpringBootTest//(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class BeverageControllerTest {

    @MockBean
    private BeverageRepository beverageRepository;

    @MockBean
    private ShoppingCartService shoppingCartService;

    @MockBean
    private BottleRepository bottleRepository;

    @MockBean
    private CrateRepository crateRepository;

    @Autowired
    private MockMvc mvc;


    private List<Beverage> beverages = new ArrayList<>();
    private List<OrderItem> orderItems = new ArrayList<>();
    private Bottle exampleBottle;
    private Crate exampleCrate;


    @BeforeEach
    public void initCommonUsedData() {
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

        //Crate Schlenkerla
        Crate crateSchlenkerla = new Crate();
        crateSchlenkerla.setId(2L);
        crateSchlenkerla.setName("20 Crate Schlenkerla");
        crateSchlenkerla.setPic("https://www.getraenkedienst.com/media/image/34/b1/39/Brauerei_Heller_Schlenkerla_Aecht_Schlenkerla_Rauchbier_Maerzen_20_x_0_5l.jpg");
        crateSchlenkerla.setNoOfBottles(20);
        crateSchlenkerla.setPrice(18.39);
        crateSchlenkerla.setInStock(13);
        crateSchlenkerla.setBottle(schlenkerla);

        this.exampleBottle = schlenkerla;
        this.exampleCrate = crateSchlenkerla;
        this.beverages.add(schlenkerla);
        this.beverages.add(crateSchlenkerla);
    }


    @Test
    public void getHome_shouldSuccess() throws Exception {
        when(this.beverageRepository.findAll()).thenReturn(beverages);
        when(this.shoppingCartService.getItemsInCart()).thenReturn(orderItems);

        this.mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("beverages"))
                .andExpect(model().attribute("beverages", beverages))
                .andExpect(model().attribute("listofitems", orderItems.size()))
                .andExpect(content().string(containsString(this.beverages.get(0).getName())));

        verify(this.beverageRepository, times(1)).findAll();
        verify(this.shoppingCartService, times(1)).getItemsInCart();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void getAddNewBottleForm_shouldSuccess() throws Exception {

        this.mvc.perform(get("/addnewbottle"))
                .andExpect(status().isOk())
                .andExpect(view().name("addBottle"))
                .andExpect(model().attribute("bottle", new Bottle()))
                .andExpect(content().string(containsString("Add New Bottle")));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    public void getAddNewBottleForm_shouldFailOnWrongRole() throws Exception {

        this.mvc.perform(get("/addnewbottle"))
                .andExpect(status().isForbidden());
    }


    @Test
    @WithMockUser(roles = "ADMIN")
    public void postAddBottle_shouldSuccess() throws Exception {

        this.mvc.perform(
                post("/addnewbottle")
                    .params(convert(this.exampleBottle))
                    .with(csrf())
                )
                .andExpect(status().isOk())
                .andExpect(view().name("beverages"));

        verify(this.bottleRepository, times(1)).save(this.exampleBottle);
    }


    @Test
    @WithMockUser(roles = "ADMIN")
    public void postAddBottle_shouldFailOnInvalidObject() throws Exception {
        this.exampleBottle.setName("");

        this.mvc.perform(
                post("/addnewbottle")
                    .params(convert(this.exampleBottle))
                    .with(csrf())
                )
                .andExpect(status().isOk())
                .andExpect(model().attribute("bottle", this.exampleBottle))
                .andExpect(view().name("addBottle"));

        verify(this.bottleRepository, times(0)).save(this.exampleBottle);
    }


    @Test
    @WithMockUser(roles = "CUSTOMER")
    public void postAddBottle_shouldFailOnWrongRole() throws Exception {

        this.mvc.perform(
                post("/addnewbottle")
                        .params(convert(this.exampleBottle))
                        .with(csrf())
                ).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void getAddNewCrateForm_shouldSuccess() throws Exception {
        Crate crate = new Crate();
        crate.setBottle(new Bottle());

        when(this.bottleRepository.findAll()).thenReturn(Collections.singletonList(this.exampleBottle));

        this.mvc.perform(get("/addnewcrate"))
                .andExpect(status().isOk())
                .andExpect(view().name("addCrate"))
                .andExpect(model().attribute("crate", crate))
                .andExpect(model().attribute("bottles", Collections.singletonList(this.exampleBottle)))
                .andExpect(content().string(containsString("Add New Crate")))
                .andExpect(content().string(containsString(this.exampleBottle.getName())));

        verify(this.bottleRepository, times(1)).findAll();
    }


    @Test
    @WithMockUser(roles = "CUSTOMER")
    public void getAddNewCrateForm_shouldFailOnWrongRole() throws Exception {

        this.mvc.perform(get("/addnewcrate"))
                .andExpect(status().isForbidden());
    }


    @Test
    @WithMockUser(roles = "ADMIN")
    public void postAddCrate_shouldSuccess() throws Exception {
        this.mvc.perform(
                post("/addnewcrate")
                    .params(convert(this.exampleCrate))
                    .with(csrf())
                )
                .andExpect(status().isOk())
                .andExpect(view().name("beverages"));

        verify(this.crateRepository, times(1)).save(this.exampleCrate);
    }


    @Test
    @WithMockUser(roles = "ADMIN")
    public void postAddCrate_shouldFailOnInvalidObject() throws Exception {
        this.exampleCrate.setName("");

        when(this.bottleRepository.findAll()).thenReturn(Collections.singletonList(this.exampleBottle));

        this.mvc.perform(
                post("/addnewcrate")
                    .params(convert(this.exampleCrate))
                    .with(csrf())
                )
                .andExpect(status().isOk())
                .andExpect(model().attribute("crate", this.exampleCrate))
                .andExpect(model().attribute("bottles", Collections.singletonList(this.exampleBottle)))
                .andExpect(view().name("addCrate"));

        verify(this.bottleRepository, times(0)).save(this.exampleBottle);
        verify(this.bottleRepository, times(1)).findAll();
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    public void postAddCrate_shouldFailOnWrongRole() throws Exception {

        this.mvc.perform(
                post("/addnewcrate")
                        .params(convert(this.exampleCrate))
                        .with(csrf())
                ).andExpect(status().isForbidden());
    }

    private static MultiValueMap<String, String> convert(Object obj) {
        Bottle b = null;
        if (obj instanceof Crate) {
            b = ((Crate) obj).getBottle();
            ((Crate) obj).setBottle(null);
        }
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        Map<String, String> maps = new ObjectMapper().convertValue(obj, new TypeReference<Map<String, String>>() {});
        parameters.setAll(maps);

        if (obj instanceof Crate) {
            maps = new ObjectMapper().convertValue(b, new TypeReference<Map<String, String>>() {});
            Map<String, String> newMap = new HashMap<>();
            for (Map.Entry<String, String> entry : maps.entrySet()) {
                newMap.put("bottle."+ entry.getKey(), maps.get(entry.getKey()));
            }
            parameters.setAll(newMap);
            ((Crate) obj).setBottle(b);
        }

        return parameters;
    }
}
