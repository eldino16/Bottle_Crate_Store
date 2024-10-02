package de.group15.assignment1.service;

import de.group15.assignment1.model.*;
import de.group15.assignment1.repository.BeverageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ActiveProfiles("test")
@SpringBootTest
public class BeverageServiceTest {

    @MockBean
    private BeverageRepository beverageRepository;

    @Autowired
    private BeverageService beverageService;

    private Beverage beverage;

    @BeforeEach
    public void initCommonUsedData() {
        Bottle schlenkerla = new Bottle();
        schlenkerla.setId(1L);
        schlenkerla.setName("Schlenkerla");
        schlenkerla.setPic("https://www.getraenkewelt-weiser.de/images/product/01/85/40/18546-0-p.jpg");
        schlenkerla.setVolume(0.5);
        schlenkerla.setVolumePercent(5.1);
        schlenkerla.setPrice(0.89);
        schlenkerla.setSupplier("Rauchbierbrauerei Schlenkerla");
        schlenkerla.setInStock(438);

        beverage = schlenkerla;
    }

    @Test
    public void updateBeverageQuantity_ShouldSuccess() {
        when(this.beverageRepository.findById(beverage.getId())).thenReturn(Optional.ofNullable(beverage));

        beverageService.updateBeverageQuantity(beverage.getId(), 100);

        verify(this.beverageRepository, times(2)).findById(beverage.getId());
        verify(this.beverageRepository, times(1)).save(beverage);
        assertEquals(beverage.getInStock(), 100);
    }


    @Test
    public void updateBeverageQuantity_ShouldFailWhenBeverageDoesntExists() {
        when(this.beverageRepository.findById(2L)).thenReturn(Optional.empty());

        beverageService.updateBeverageQuantity(2L, 100);

        verify(this.beverageRepository, times(1)).findById(2L);
    }
}
