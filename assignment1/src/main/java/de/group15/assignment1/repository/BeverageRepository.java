package de.group15.assignment1.repository;

import de.group15.assignment1.model.Beverage;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BeverageRepository extends JpaRepository<Beverage, Long> {

    @Override
    @EntityGraph(value = "Beverage.Crate")
    List<Beverage> findAll();

    @Override
    @EntityGraph(value = "Beverage.Crate")
    Optional<Beverage> findById(Long id);


}
