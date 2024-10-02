package de.group15.assignment1.service.impl;

import de.group15.assignment1.model.Beverage;
import de.group15.assignment1.repository.BeverageRepository;
import de.group15.assignment1.service.BeverageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class BeverageServiceImpl implements BeverageService {
    @Autowired
    private BeverageRepository beverageRepository;

    @Override
    public void updateBeverageQuantity(Long beverageID, int newQuantity) {
        if (beverageRepository.findById(beverageID).isPresent()) {
            log.info("beverage id is found in repository");
            Beverage existingBeverage = beverageRepository.findById(beverageID).get();
            existingBeverage.setInStock(newQuantity);
            beverageRepository.save(existingBeverage);
        } else {
            log.info("beverage id is not found the save function did not execute");
        }
    }
}
