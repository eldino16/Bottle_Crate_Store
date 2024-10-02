package de.group15.assignment1.repository;

import de.group15.assignment1.model.Crate;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CrateRepository extends JpaRepository<Crate, Long> {

    @Override
    @EntityGraph(value = "Beverage.Crate")
    List<Crate> findAll();

    @Override
    @EntityGraph(value = "Beverage.Crate")
    Optional<Crate> findById(Long id);
}
